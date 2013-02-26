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
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.org.winters.appspot.acrareporter.shared.ErrorListFilter;
import nz.org.winters.appspot.acrareporter.shared.Utils;
import nz.org.winters.appspot.acrareporter.store.ACRALog;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.AppUser;
import nz.org.winters.appspot.acrareporter.store.BasicErrorInfo;
import nz.org.winters.appspot.acrareporter.store.DailyCounts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.ObjectifyService;

public class AndroidInterface extends HttpServlet
{

  private static final double GSON_VERSION     = 1.0;
  /**
   * 
   */
  private static final long   serialVersionUID = 461747578656770636L;
  private static final Logger log              = Logger.getLogger(MappingFileHandler.class.getName());

  static
  {
    RegisterDataStores.register();
  }

  // private static final Logger log =
  // Logger.getLogger(CronJobEMails.class.getName());
  public List<AppPackage> getPackages(Long owner)
  {
    return ObjectifyService.ofy().load().type(AppPackage.class).filter("Owner", owner).list();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
  {
    response.setContentType("text/plain");

    try
    {
      String apiKey = request.getParameter("apikey");
      if (Utils.isEmpty(apiKey))
      {
        response.getWriter().println("FAIL NO KEY");
        log.severe("no api key");
        return;
      }

      String function = request.getParameter("function");
      if (Utils.isEmpty(function))
      {
        response.getWriter().println("FAIL NO FUNCTION");
        log.severe("no function");
        return;
      }

      AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).filter("AndroidKey", apiKey).first().get();
      if (appUser == null)
      {
        response.getWriter().println("FAIL UNKNOWN API KEY: " + apiKey);
        log.severe("unknown apikey: " + apiKey);
        return;
      }

      if (function.equals("packages"))
      {
        getPackages(response, appUser);
      } else if (function.equals("totals"))
      {
        getTotals(response, appUser);
      } else if (function.equals("daily"))
      {
        String daysback = request.getParameter("daysback");
        if (Utils.isEmpty(daysback))
        {
          response.getWriter().println("FAIL NO DAYSBACK");
          log.severe("no daysback");
          return;
        }
        getDaily(response, appUser, Integer.parseInt(daysback));
      } else if (function.equals("package"))
      {
        String packageName = request.getParameter("package");
        if (Utils.isEmpty(packageName))
        {
          response.getWriter().println("FAIL NO PACKAGE");
          log.severe("no package");
          return;
        }
        String daysback = request.getParameter("daysback");
        if (Utils.isEmpty(daysback))
        {
          response.getWriter().println("FAIL NO DAYSBACK");
          log.severe("no daysback");
          return;
        }
        getPackageDaily(response, packageName, Integer.parseInt(daysback));
      } else if (function.equals("errors"))
      {
        String packageName = request.getParameter("package");
        if (Utils.isEmpty(packageName))
        {
          response.getWriter().println("FAIL NO PACKAGE");
          log.severe("no package");
          return;
        }
        String daysback = request.getParameter("filter");
        if (Utils.isEmpty(daysback))
        {
          response.getWriter().println("FAIL NO FILTER");
          log.severe("no filter");
          return;
        }
        getBasicErrorInfo(response, packageName, daysback);
      } else if (function.equals("error"))
      {
        String reportId = request.getParameter("reportid");
        if (Utils.isEmpty(reportId))
        {
          response.getWriter().println("FAIL NO reportid");
          log.severe("no reportid");
          return;
        }
        getError(response, reportId);
      } else
      {
        response.getWriter().println("FAIL UNKNOWN FUNCTION: " + function);
        log.severe("unknown function: " + function);
      }

    } catch (Exception e)
    {
      response.getWriter().println("ERROR: " + e.toString());
      e.printStackTrace();
      // log.warning("Exception " + e.getMessage());
    }

    // send each to each user.

  //  response.getWriter().println("DONE");

  }

  private void getDaily(HttpServletResponse response, AppUser appUser, int daysBack)  throws Exception
  {
    List<DailyCounts> dailyCounts = DailyCountsGetters.getUserDaysBack(appUser.id, daysBack);
    Gson gson = new GsonBuilder().setVersion(GSON_VERSION).create();
    response.getWriter().println(gson.toJson(dailyCounts));
    

  }


  private void getPackageDaily(HttpServletResponse response, String packageName, int daysBack)  throws Exception
  {
    List<DailyCounts> dailyCounts = DailyCountsGetters.getPackageDaysBack(packageName, daysBack);
    Gson gson = new GsonBuilder().setVersion(GSON_VERSION).create();
    response.getWriter().println(gson.toJson(dailyCounts));
    

  }  
  
  private void getPackages(HttpServletResponse response, AppUser appUser) throws Exception
  {
  //  response.getWriter().println("PACKAGES");
    List<AppPackage> appPackages = ObjectifyService.ofy().load().type(AppPackage.class).filter("Owner", appUser.id).list();

    Gson gson = new GsonBuilder().setVersion(GSON_VERSION).excludeFieldsWithoutExposeAnnotation().create();
    response.getWriter().println(gson.toJson(appPackages));
  }
  
  private void getTotals(HttpServletResponse response, AppUser appUser) throws Exception
  {
    Gson gson = new GsonBuilder().setVersion(GSON_VERSION).create();
    response.getWriter().println(gson.toJson(appUser.Totals));
  }
  
  private void getBasicErrorInfo(HttpServletResponse response, String packageName, String filter) throws Exception
  {
  //  response.getWriter().println("PACKAGES");
    List<BasicErrorInfo> list;
    ErrorListFilter elf = ErrorListFilter.fromFilterString(filter);
    switch(elf)
    {
      case elfFixed:
        list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", packageName).filter("fixed",true).list();
        break;
      case elfLookedAt:
        list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", packageName).filter("lookedAt",true).list();
        break;
      case elfNew:
        list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", packageName).filter("fixed",false).filter("lookedAt",false).list();
        break;
      case elfNotFixed:
        list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", packageName).filter("fixed",false).list();
        break;
      case elfAll:
      default:
        list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", packageName).list();// .order("Timestamp desc").list();
        break;
      
    }

    Gson gson = new GsonBuilder().setVersion(GSON_VERSION).create();
    response.getWriter().println(gson.toJson(list));
  }
  

  private void getError(HttpServletResponse response, String reportId) throws Exception
  {
  //  response.getWriter().println("PACKAGES");
    ACRALog error = ObjectifyService.ofy().load().type(ACRALog.class).filter("REPORT_ID", reportId).first().get();

    Gson gson = new GsonBuilder().setVersion(GSON_VERSION).create();
    response.getWriter().println(gson.toJson(error));
  }
}
