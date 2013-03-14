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
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
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
import nz.org.winters.appspot.acrareporter.store.MappingFileData;
import nz.org.winters.appspot.acrareporter.store.MappingFileInfo;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy.OverQuotaException;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
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
        System.out.println("NO DATA");
        log.severe("NO DATA");
        response.getWriter().println("FAIL NO DATA\n" + request.toString());
        return;
      }

      Objectify ofy = ObjectifyService.factory().begin();
      
      BasicErrorInfo basicInfo = ofy.load().type(BasicErrorInfo.class).filter("REPORT_ID", request.getParameter("REPORT_ID")).first().get();
      if (basicInfo == null)
      {
        String PACKAGE_NAME = request.getParameter("PACKAGE_NAME");
        // get package.
        log.warning("PACKAGE = " + PACKAGE_NAME);
        
        AppPackage appPackage = ofy.load().type(AppPackage.class).filter("PACKAGE_NAME", PACKAGE_NAME).first().get();
        if (appPackage == null)
        {
          System.out.println("package unknown " + PACKAGE_NAME);
          log.severe("package unknown " + PACKAGE_NAME);
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
//          response.getWriter().println("FAIL PACKAGE UNKNOWN");
          return;
        }
        
        if(!appPackage.enabled)
        {
          System.out.println("package disabled " + PACKAGE_NAME);
          log.severe("package disabled " + PACKAGE_NAME);
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
        
        ACRALog acraLog = populateACRA(request);
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
        AppUser appUser = ofy.load().type(AppUser.class).id(appPackage.Owner).get();

        if (appUser == null)
        {
          response.getWriter().println("FAIL USER UNKNOWN");
          log.severe("User not found " + acraLog.PACKAGE_NAME);
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;

        }

        if (appUser.isUser && !appUser.isSubscriptionPaid)
        {
          response.getWriter().println("FAIL SUBSCRIPTION NOT PAID");
          log.severe("subscription unpaid " + appUser.EMailAddress + " - " + acraLog.PACKAGE_NAME);
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
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

        // find mapping.
        MappingFileInfo mapping = ofy.load().type(MappingFileInfo.class).filter("PACKAGE_NAME", acraLog.PACKAGE_NAME).filter("version", acraLog.APP_VERSION_NAME).first().get();
        if (mapping != null)
        {
          MappingFileInfo mostRecentMapping = ofy.load().type(MappingFileInfo.class).filter("PACKAGE_NAME", acraLog.PACKAGE_NAME).order("uploadDate").limit(1).first().get();
          if (mostRecentMapping != null)
          {
            if (mostRecentMapping.getId() != mapping.getId())
            {
              // old version, lets write out message
              response.getWriter().println("OLD VERSION");
              if (appPackage.DiscardOldVersionReports)
              {
                System.out.println("Discarded Old version " + PACKAGE_NAME + "," + mostRecentMapping.version + "," + mapping.version);
                log.warning("Discarded Old version " + PACKAGE_NAME + "," + mostRecentMapping.version + "," + mapping.version);
                return;
              }
            }
          }

          MappingFileData mfd = ofy.load().type(MappingFileData.class).filter("mappingFileInfoId",mapping.id).first().get();

          acraLog.MAPPED_STACK_TRACE = StringReTrace.doReTrace(mfd.mapping, acraLog.STACK_TRACE);
        }else 
        {
          response.getWriter().println("OLD VERSION");
          if (appPackage.DiscardOldVersionReports)
          {
            System.out.println("Discarded Old version " + PACKAGE_NAME + " NO MAPPING");
            log.warning("Discarded Old version " + PACKAGE_NAME + " NO MAPPING");
            return;
          }
        }

        DailyCounts today = DailyCountsGetters.getToday(acraLog.PACKAGE_NAME);
        if(today.Reports > 200)
        {
          System.out.println("200 Reports in day " + PACKAGE_NAME + " DISCARDING NEW");
          log.severe("200 Reports in day " + PACKAGE_NAME + " DISCARDING NEW");
          appPackage.enabled = false;
          ofy.save().entity(appPackage).now();
          
          // email app owner.
          SendAppTooManyEMail(appUser,appPackage,today);
          
          
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
        }
        
        ofy.save().entity(basicInfo).now();
        ofy.save().entity(acraLog).now();

        // Increment counters.
        appUser.Totals.incReports();
        ofy.save().entity(appUser).now();

        appPackage.Totals.incReports();
        ofy.save().entity(appPackage).now();

        DailyCounts counts = DailyCountsGetters.getToday(appUser.id);
        counts.incReports();
        ofy.save().entity(counts).now();

        today.incReports();
        ofy.save().entity(today).now();

        ofy = null;

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
        

        System.out.println("OK " + acraLog.REPORT_ID);
        log.warning("OK " + acraLog.REPORT_ID);
        response.getWriter().println("OK");
        
        
      } else
      {
        System.out.println("OK SKIPPED REPORT_ID MATCH");
        log.warning("OK SKIPPED REPORT_ID MATCH");
        response.getWriter().println("OK SKIPPED REPORT_ID MATCH");
      }

    } catch (OverQuotaException e)
    {
//      response.getWriter().println("FAIL resource over quota, try again in a few hours.");
      log.severe(e.getMessage());
      System.out.println("OVER QUOTA");
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    } catch (Exception e)
    {
      response.getWriter().println("FAIL ERROR " + e.getMessage());
      System.out.println("Exception " + e.getMessage());
      log.severe(e.getMessage());
    }
  
  }

  private void SendAppTooManyEMail(AppUser appUser, AppPackage appPackage, DailyCounts today) throws Exception
  {
    Properties props = new Properties();
    Session session = Session.getInstance(props, null);
    Message msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(Configuration.defaultSenderEMailAddress, Configuration.defaultSenderName));
    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(appUser.EMailAddress, appUser.FirstName + " " + appUser.LastName));
    msg.setSubject("ACRA Reporter - App " + appPackage.PACKAGE_NAME + " creating too many error reports!");

    StringBuilder sb = new StringBuilder();
    Formatter formatter = new Formatter(sb, Locale.US);
    String bodyText;
    try
    {
      sb.append("This is a notice that the app has been disabled and will not accept any more error reports!\r\n ");
      sb.append("The app has created 200 errors since " + today.dateString() + " this limit has been implemented to ensure the app engine instance is aviable to all users as excessive error reports used to overload the daily free limits for the app engine instance.\r\n ");
      sb.append("\r\n\r\n");

      if(!appPackage.DiscardOldVersionReports)
      {
        sb.append("There is information on the wiki about modifying your application so it will alert the user if they are logging an error for a old app version which can be a good idea.\r\n");
      }
      
      bodyText = sb.toString();
    } finally
    {
      formatter.close();
    }

    MimeBodyPart messageBodyPart = new MimeBodyPart();

    messageBodyPart.setContent("<pre>" + bodyText + "</pre>", "text/html");

    MimeMultipart multipart = new MimeMultipart();

    MimeBodyPart messageBodyPartText = new MimeBodyPart();

    messageBodyPartText.setContent(bodyText, "text/plain");

    multipart.addBodyPart(messageBodyPart);
    multipart.addBodyPart(messageBodyPartText);
    msg.setContent(multipart);

    Transport.send(msg);
    
    
    
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
    log.APPLICATION_LOG = request.getParameter("APPLICATION_LOG");
    log.DEVICE_ID = request.getParameter("DEVICE_ID");
    log.DROPBOX = request.getParameter("DROPBOX");
    log.EVENTSLOG = request.getParameter("EVENTSLOG");
    log.MEDIA_CODEC_LIST = request.getParameter("MEDIA_CODEC_LIST");
    log.RADIOLOG = request.getParameter("RADIOLOG");
    log.SETTINGS_GLOBAL = request.getParameter("SETTINGS_GLOBAL");
    log.THREAD_DETAILS = request.getParameter("THREAD_DETAILS");
    log.USER_IP = request.getParameter("USER_IP");
    return log;
  }

}
