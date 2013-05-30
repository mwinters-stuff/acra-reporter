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
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.org.winters.appspot.acrareporter.server.jgoogleanalytics.FocusPoint;
import nz.org.winters.appspot.acrareporter.server.jgoogleanalytics.JGoogleAnalyticsTracker;
import nz.org.winters.appspot.acrareporter.shared.Configuration;
import nz.org.winters.appspot.acrareporter.shared.Utils;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.AppUser;
import nz.org.winters.appspot.acrareporter.store.MappingFileData;
import nz.org.winters.appspot.acrareporter.store.MappingFileInfo;
import nz.org.winters.appspot.acrareporter.store.MemoryFileItemFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy.OverQuotaException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

public class MappingUploadHandler extends HttpServlet
{
  private static final long serialVersionUID = -3697911766174141253L;

  static
  {
    RegisterDataStores.register();

  }

  public MappingUploadHandler()
  {
    super();
  }

  private static final Logger log = Logger.getLogger(MappingUploadHandler.class.getName());

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

  public static String convertStreamToString(java.io.InputStream is)
  {
    java.util.Scanner s = new java.util.Scanner(is);
    s.useDelimiter("\\A");
    try
    {
      return s.hasNext() ? s.next() : "";
    } finally
    {
      s.close();
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {

    response.setContentType("text/plain");
  
    try
    {
      MemoryFileItemFactory filefactory = new MemoryFileItemFactory();
      

      ServletFileUpload upload = new ServletFileUpload(filefactory);

      List<FileItem> items = upload.parseRequest(request);

      InputStream fileinput = null;
      String apppackage = null;
      String version = null;

      Iterator<FileItem> iter = items.iterator();
      while (iter.hasNext())
      {
        FileItem item = iter.next();

        if (item.isFormField())
        {
          String name = item.getFieldName();
          String value = item.getString();

          if (name.equals("package"))
          {
            apppackage = value;
          } else if (name.equals("version"))
          {
            version = value;
          }
        } else
        {
          fileinput = item.getInputStream();
        }
      }

      if (apppackage == null || apppackage.isEmpty())
      {
        response.getWriter().println("FAIL NO PACKAGE");
        log.severe("no package");
        return;
      }

      if (version == null || apppackage.isEmpty())
      {
        response.getWriter().println("FAIL NO VERSION");
        log.severe("no version" + apppackage);
        return;
      }

      if (fileinput == null)
      {
        response.getWriter().println("FAIL NO FILE");
        log.severe("no version" + apppackage);
        return;
      }

      AppPackage appPackage = ObjectifyService.ofy().load().type(AppPackage.class).filter("PACKAGE_NAME", apppackage).first().get();
      if (appPackage == null)
      {
        response.getWriter().println("FAIL PACKAGE UNKNOWN: " + apppackage);
        log.severe("package unknown " + apppackage);
        return;
      }

      // get user for package.
      AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).id(appPackage.Owner).get();

      if (appUser == null)
      {
        response.getWriter().println("FAIL USER UNKNOWN");
        log.severe("User not found " + apppackage);
        return;

      }

      if (!appUser.isSubscriptionPaid)
      {
        response.getWriter().println("FAIL SUBSCRIPTION NOT PAID");
        log.severe("subscription unpaid " + appUser.EMailAddress + " - " + apppackage);
        return;
      }

      String data = convertStreamToString(fileinput);

      MappingFileInfo mfi = new MappingFileInfo(appUser, apppackage, version);

      Key<MappingFileInfo> resultkey = ObjectifyService.ofy().save().entity(mfi).now();

      MappingFileData mfd = new MappingFileData();
      mfd.add(data);
      mfd.mappingFileInfoId = resultkey.getId();
      ObjectifyService.ofy().save().entity(mfd);

      tidyMapList(appPackage);

      if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production)
      {
        // analytics.
        FocusPoint focusApp = new FocusPoint(apppackage);
        FocusPoint focusPoint = new FocusPoint("MappingAdded", focusApp);
        FocusPoint focusVer = new FocusPoint(version, focusPoint);
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

  private void tidyMapList(AppPackage appPackage)
  {
    if (appPackage.mappingsToKeep == 0)
      return;

    List<MappingFileInfo> allmaps = ObjectifyService.ofy().load().type(MappingFileInfo.class).filter("PACKAGE_NAME", appPackage.PACKAGE_NAME).order("-uploadDate").offset(appPackage.mappingsToKeep).list();

    if (allmaps.size() > 0)
    {
      for (MappingFileInfo map : allmaps)
      {
        MappingFileData mfd = ObjectifyService.ofy().load().type(MappingFileData.class).filter("mappingFileInfoId", map.id).first().get();
        if (mfd != null)
        {
          ObjectifyService.ofy().delete().entity(mfd);
        }
      }

      ObjectifyService.ofy().delete().entities(allmaps);

    }

  }

}