package nz.org.winters.appspot.acrareporter.server;

/*
 * Copyright 2013 Mathew Winters

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.org.winters.appspot.acrareporter.server.jgoogleanalytics.FocusPoint;
import nz.org.winters.appspot.acrareporter.server.jgoogleanalytics.JGoogleAnalyticsTracker;
import nz.org.winters.appspot.acrareporter.shared.Configuration;
import nz.org.winters.appspot.acrareporter.shared.Utils;
import nz.org.winters.appspot.acrareporter.store.ACRALog;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.AppUser;
import nz.org.winters.appspot.acrareporter.store.BasicErrorInfo;
import nz.org.winters.appspot.acrareporter.store.DailyCounts;
import nz.org.winters.appspot.acrareporter.store.MappingFile;
import nz.org.winters.appspot.acrareporter.store.RegisterDataStores;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy.OverQuotaException;
import com.googlecode.objectify.ObjectifyService;

public class ACRAReportHandler extends HttpServlet
{

  /**
   * 
   */
  private static final long serialVersionUID = -4033975352492009216L;

  static
  {
    RegisterDataStores.register();

  }

  public ACRAReportHandler()
  {
    super();
  }

  private static final Logger log = Logger.getLogger(ACRAReportHandler.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    doPost(request, response);
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    doPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {

    response.setContentType("text/plain");
    try
    {
      if (!request.getParameterNames().hasMoreElements())
      {
        response.getWriter().println("FAIL NO DATA\n" + request.toString());
        return;

      }

      BasicErrorInfo basicInfo = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("REPORT_ID", request.getParameter("REPORT_ID")).first().get();
      if (basicInfo == null)
      {
        ACRALog acraLog = populateACRA(request);

        // get package.
        AppPackage appPackage = ObjectifyService.ofy().load().type(AppPackage.class).filter("PACKAGE_NAME", acraLog.PACKAGE_NAME).first().get();
        if (appPackage == null)
        {
          response.getWriter().println("FAIL PACKAGE UNKNOWN");
          log.warning("package unknown " + acraLog.PACKAGE_NAME);
          return;
        }

        String auth = request.getHeader("Authorization");
        if (Utils.isEmpty(auth))
        {
          response.getWriter().println("FAIL AUTH");
          log.severe("Authentication Failed " + acraLog.PACKAGE_NAME);
          return;
        }
        auth = auth.replace("Basic ", "").trim();
        if (!auth.equals(appPackage.AuthString))
        {
          response.getWriter().println("FAIL AUTH");
          log.severe("Authentication Failed " + acraLog.PACKAGE_NAME);
          return;
        }

        // get user for package.
        AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).id(appPackage.Owner).get();

        if (appUser == null)
        {
          response.getWriter().println("FAIL USER UNKNOWN");
          log.severe("User not found " + acraLog.PACKAGE_NAME);
          return;

        }

        if (appUser.isUser && !appUser.isSubscriptionPaid)
        {
          response.getWriter().println("FAIL SUBSCRIPTION NOT PAID");
          log.warning("subscription unpaid " + appUser.EMailAddress + " - " + acraLog.PACKAGE_NAME);
          return;
        }

        acraLog.Owner = appUser.id;

        basicInfo = new BasicErrorInfo();
        basicInfo.Owner = appUser.id;
        basicInfo.ANDROID_VERSION = acraLog.ANDROID_VERSION;
        basicInfo.APP_VERSION_NAME = acraLog.APP_VERSION_NAME;
        basicInfo.PACKAGE_NAME = acraLog.PACKAGE_NAME;
        basicInfo.REPORT_ID = acraLog.REPORT_ID;
        basicInfo.USER_CRASH_DATE = acraLog.USER_CRASH_DATE;
        basicInfo.Timestamp = acraLog.Timestamp;

        basicInfo.save();

        // find mapping.
        MappingFile mapping = ObjectifyService.ofy().load().type(MappingFile.class).filter("apppackage", acraLog.PACKAGE_NAME).filter("version", acraLog.APP_VERSION_NAME).first().get();
        if (mapping != null)
        {
          acraLog.MAPPED_STACK_TRACE = StringReTrace.doReTrace(mapping.mapping, acraLog.STACK_TRACE);
        }

        acraLog.save();

        // incremenet counters.
        appUser.Totals.incReports();
        appUser.save();

        appPackage.Totals.incReports();
        appPackage.save();

        DailyCounts counts = DailyCounts.getToday(appUser.id);
        counts.incReports();
        counts.save();

        counts = DailyCounts.getToday(acraLog.PACKAGE_NAME);
        counts.incReports();
        counts.save();

        // analytics.
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production)
        {
          // The app is running on App Engine...
          FocusPoint focusApp = new FocusPoint(basicInfo.PACKAGE_NAME);
          FocusPoint focusPoint = new FocusPoint("ErrorReport", focusApp);
          FocusPoint focusVer = new FocusPoint(basicInfo.APP_VERSION_NAME, focusPoint);
          if (!Utils.isEmpty(appUser.AnalyticsTrackingId))
          {
            JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker("ACRA Reporter", "0.1", appUser.AnalyticsTrackingId);
            tracker.trackSynchronously(focusVer);
          }
          if (Configuration.gaTrackingID != null)
          {
            JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker("ACRA Reporter", "0.1", Configuration.gaTrackingID);
            tracker.trackSynchronously(focusVer);
          }
        }

        response.getWriter().println("OK");
      } else
      {
        response.getWriter().println("OK SKIPPED REPORT_ID MATCH");
      }

    } catch (OverQuotaException e)
    {
      response.getWriter().println("FAIL resource over quota, try again in a few hours.");
      log.severe(e.getMessage());
    } catch (Exception e)
    {
      response.getWriter().println("FAIL ERROR " + e.getMessage());
      log.severe(e.getMessage());
    }
  }

  ACRALog populateACRA(HttpServletRequest request)
  {
    ACRALog log = new ACRALog();
    log.Timestamp = new Date();

    log.REPORT_ID = request.getParameter("REPORT_ID");

    String apc = request.getParameter("APP_VERSION_CODE").replace(".0", "");

    log.APP_VERSION_CODE = Integer.parseInt(apc);
    log.APP_VERSION_NAME = request.getParameter("APP_VERSION_NAME");
    log.PACKAGE_NAME = request.getParameter("PACKAGE_NAME");
    log.FILE_PATH = request.getParameter("FILE_PATH");
    log.PHONE_MODEL = request.getParameter("PHONE_MODEL");
    log.BRAND = request.getParameter("BRAND");
    log.PRODUCT = request.getParameter("PRODUCT");
    log.ANDROID_VERSION = request.getParameter("ANDROID_VERSION");
    log.BUILD = request.getParameter("BUILD");
    log.TOTAL_MEM_SIZE = request.getParameter("TOTAL_MEM_SIZE");
    log.AVAILABLE_MEM_SIZE = request.getParameter("AVAILABLE_MEM_SIZE");
    log.CUSTOM_DATA = request.getParameter("CUSTOM_DATA");
    log.IS_SILENT = request.getParameter("IS_SILENT");
    log.STACK_TRACE = request.getParameter("STACK_TRACE");
    log.INITIAL_CONFIGURATION = request.getParameter("INITIAL_CONFIGURATION");
    log.CRASH_CONFIGURATION = request.getParameter("CRASH_CONFIGURATION");
    log.DISPLAY = request.getParameter("DISPLAY");
    log.USER_COMMENT = request.getParameter("USER_COMMENT");
    log.USER_EMAIL = request.getParameter("USER_EMAIL");
    log.USER_APP_START_DATE = request.getParameter("USER_APP_START_DATE");
    log.USER_CRASH_DATE = request.getParameter("USER_CRASH_DATE");
    log.DUMPSYS_MEMINFO = request.getParameter("DUMPSYS_MEMINFO");
    log.LOGCAT = request.getParameter("LOGCAT");
    log.INSTALLATION_ID = request.getParameter("INSTALLATION_ID");
    log.DEVICE_FEATURES = request.getParameter("DEVICE_FEATURES");
    log.ENVIRONMENT = request.getParameter("ENVIRONMENT");
    log.SHARED_PREFERENCES = request.getParameter("SHARED_PREFERENCES");
    log.SETTINGS_SYSTEM = request.getParameter("SETTINGS_SYSTEM");
    log.SETTINGS_SECURE = request.getParameter("SETTINGS_SECURE");
    log.APPLICATION_LOG   = request.getParameter("APPLICATION_LOG");
    log.DEVICE_ID         = request.getParameter("DEVICE_ID");
    log.DROPBOX           = request.getParameter("DROPBOX");
    log.EVENTSLOG         = request.getParameter("EVENTSLOG");
    log.MEDIA_CODEC_LIST  = request.getParameter("MEDIA_CODEC_LIST");
    log.RADIOLOG          = request.getParameter("RADIOLOG");
    log.SETTINGS_GLOBAL   = request.getParameter("SETTINGS_GLOBAL");
    log.THREAD_DETAILS    = request.getParameter("THREAD_DETAILS");
    log.USER_IP           = request.getParameter("USER_IP");
    return log;
  }

}
