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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import nz.org.winters.appspot.acrareporter.client.RemoteDataService;
import nz.org.winters.appspot.acrareporter.server.jgoogleanalytics.FocusPoint;
import nz.org.winters.appspot.acrareporter.server.jgoogleanalytics.JGoogleAnalyticsTracker;
import nz.org.winters.appspot.acrareporter.shared.ACRALogShared;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.AppUserShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.Configuration;
import nz.org.winters.appspot.acrareporter.shared.DailyCountsShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.shared.MappingFileShared;
import nz.org.winters.appspot.acrareporter.shared.Utils;
import nz.org.winters.appspot.acrareporter.store.ACRALog;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.AppUser;
import nz.org.winters.appspot.acrareporter.store.BasicErrorInfo;
import nz.org.winters.appspot.acrareporter.store.DailyCounts;
import nz.org.winters.appspot.acrareporter.store.MappingFile;
import nz.org.winters.appspot.acrareporter.store.RegisterDataStores;

import com.google.appengine.api.utils.SystemProperty;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.ObjectifyService;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class RemoteDataServiceImpl extends RemoteServiceServlet implements RemoteDataService
{
  static
  {
    RegisterDataStores.register();
  }

  private static final Logger log = Logger.getLogger(RemoteDataServiceImpl.class.getName());

  @Override
  public String retrace(String mapping, String error) throws IllegalArgumentException
  {
    return StringReTrace.doReTrace(mapping, error);
  }

  @Override
  public Map<Long, String> getMaps(LoginInfo loginInfo) throws IllegalArgumentException
  {
    AppUser appUser = getAppUser(loginInfo);

    HashMap<Long, String> values = new HashMap<Long, String>();

    List<MappingFile> list = ObjectifyService.ofy().load().type(MappingFile.class).filter("Owner", appUser.id).list();

    for (MappingFile file : list)
    {
      values.put(file.getId(), file.getApppackage() + " - " + file.getVersion());
    }

    return values;
  }

  @Override
  public String retrace(Long mappingId, String error) throws IllegalArgumentException
  {
    MappingFile file = ObjectifyService.ofy().load().type(MappingFile.class).id(mappingId).get();
    if (file != null)
    {
      return StringReTrace.doReTrace(file.mapping, error);
    } else
    {
      return "ERROR IN RETRACE \n\n" + error;
    }
  }

  @Override
  public List<AppPackageShared> getPackages(LoginInfo loginInfo) throws IllegalArgumentException
  {
    AppUser appUser = getAppUser(loginInfo);
    long ownerId = getOwnerId(appUser);

    List<AppPackage> list = ObjectifyService.ofy().load().type(AppPackage.class).filter("Owner", ownerId).order("PACKAGE_NAME").list();
    // AppPackageShared[] result = new AppPackageShared[list.size()];
    ArrayList<AppPackageShared> result = new ArrayList<AppPackageShared>();

    for (AppPackage app : list)
    {
      result.add(app.toShared());
    }
    return result;
  }

  @Override
  public List<BasicErrorInfoShared> getBasicErrorInfo(String apppackage) throws IllegalArgumentException
  {
    List<BasicErrorInfo> list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", apppackage).list();// .order("Timestamp desc").list();
    ArrayList<BasicErrorInfoShared> result = new ArrayList<BasicErrorInfoShared>();

    for (BasicErrorInfo beo : list)
    {
      result.add(beo.toShared());
    }
    return result;
  }

  @Override
  public ACRALogShared getACRALog(String REPORT_ID) throws IllegalArgumentException
  {
    ACRALog log = ObjectifyService.ofy().load().type(ACRALog.class).filter("REPORT_ID", REPORT_ID).first().get();
    if (log != null)
    {
      return log.toShared();
    }
    return null;
  }

  @Override
  public void deleteReport(String REPORT_ID) throws IllegalArgumentException
  {
    ACRALog log = ObjectifyService.ofy().load().type(ACRALog.class).filter("REPORT_ID", REPORT_ID).first().get();
    if (log != null)
    {
      ObjectifyService.ofy().delete().entity(log);
    }

    BasicErrorInfo bei = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("REPORT_ID", REPORT_ID).first().get();
    if (bei != null)
    {
      ObjectifyService.ofy().delete().entity(bei);
    }

    AppUser user = getAppUser(bei.Owner);
    AppPackage ap = getAppPackage(bei.PACKAGE_NAME);

    ap.Totals.incDeleted();
    user.Totals.incDeleted();
    DailyCounts counts = DailyCounts.getToday(getOwnerId(user));
    counts.incDeleted();
    counts.save();

    if (bei.lookedAt)
    {
      ap.Totals.decLookedAt();
      user.Totals.decLookedAt();
    }
    if (bei.fixed)
    {
      ap.Totals.decFixed();
      user.Totals.decFixed();
    }

    ap.save();
    user.save();
  }

  @Override
  public void markReportLookedAt(String REPORT_ID, boolean state) throws IllegalArgumentException
  {
    BasicErrorInfo bei = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("REPORT_ID", REPORT_ID).first().get();
    if (bei != null)
    {
      if (bei.lookedAt != state)
      {
        bei.lookedAt = state;
        bei.save();
        AppUser user = getAppUser(bei.Owner);
        AppPackage ap = getAppPackage(bei.PACKAGE_NAME);
        DailyCounts userCounts = DailyCounts.getToday(getOwnerId(user));
        DailyCounts packageCounts = DailyCounts.getToday(bei.PACKAGE_NAME);
        if (state)
        {
          ap.Totals.incLookedAt();
          user.Totals.incLookedAt();
          userCounts.incLookedAt();
          packageCounts.incLookedAt();
        } else
        {
          ap.Totals.decLookedAt();
          user.Totals.decLookedAt();
          userCounts.decLookedAt();
          packageCounts.decLookedAt();
        }
        ap.save();
        user.save();
        userCounts.save();
        packageCounts.save();
      }
    }

  }

  @Override
  public void markReportFixed(String REPORT_ID, boolean state) throws IllegalArgumentException
  {
    BasicErrorInfo bei = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("REPORT_ID", REPORT_ID).first().get();
    if (bei != null)
    {
      if (bei.fixed != state)
      {
        bei.fixed = state;
        AppUser user = getAppUser(bei.Owner);
        AppPackage ap = getAppPackage(bei.PACKAGE_NAME);
        DailyCounts userCounts = DailyCounts.getToday(getOwnerId(user));
        DailyCounts packageCounts = DailyCounts.getToday(bei.PACKAGE_NAME);
        if (state)
        {
          ap.Totals.incFixed();
          user.Totals.incFixed();
          userCounts.incFixed();
          packageCounts.incFixed();
        } else
        {
          ap.Totals.decFixed();
          user.Totals.decFixed();
          userCounts.decFixed();
          packageCounts.decFixed();

        }

        if (state && !bei.lookedAt)
        {
          bei.lookedAt = true;
          ap.Totals.incLookedAt();
          user.Totals.incLookedAt();
          userCounts.incLookedAt();
          packageCounts.incLookedAt();
        } else if (!state && bei.lookedAt)
        {
          bei.lookedAt = false;
          ap.Totals.decLookedAt();
          user.Totals.decLookedAt();
          userCounts.decLookedAt();
          packageCounts.decLookedAt();
        }

        bei.save();
        ap.save();
        user.save();
        userCounts.save();
        packageCounts.save();

      }
    }

  }

  @Override
  public void retraceReport(String REPORT_ID) throws IllegalArgumentException
  {
    ACRALog acraLog = ObjectifyService.ofy().load().type(ACRALog.class).filter("REPORT_ID", REPORT_ID).first().get();

    if (acraLog != null)
    {

      MappingFile mapping = ObjectifyService.ofy().load().type(MappingFile.class).filter("apppackage", acraLog.PACKAGE_NAME).filter("version", acraLog.APP_VERSION_NAME).first().get();
      if (mapping != null)
      {
        acraLog.MAPPED_STACK_TRACE = StringReTrace.doReTrace(mapping.mapping, acraLog.STACK_TRACE);
        acraLog.save();
      } else
      {
        throw new IllegalArgumentException("No Maching Mapping");
      }
    } else
    {
      throw new IllegalArgumentException("Can not retrieve acra report " + REPORT_ID);
    }

  }

  @Override
  public void markReportEMailed(String REPORT_ID, boolean state) throws IllegalArgumentException
  {
    BasicErrorInfo bei = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("REPORT_ID", REPORT_ID).first().get();
    if (bei != null)
    {
      bei.emailed = state;
      bei.save();

    }

  }

  @Override
  public AppPackageShared getPackage(String PACKAGE_NAME) throws IllegalArgumentException
  {
    AppPackage app = ObjectifyService.ofy().load().type(AppPackage.class).filter("PACKAGE_NAME", PACKAGE_NAME).first().get();
    if (app != null)
    {
      return app.toShared();
    }
    throw new IllegalArgumentException("Package not found " + PACKAGE_NAME);
  }

  @Override
  public void markReportsLookedAt(List<String> reportIds, boolean state) throws IllegalArgumentException
  {
    Iterator<String> iter = reportIds.iterator();
    while (iter.hasNext())
    {
      markReportLookedAt(iter.next(), state);
    }
  }

  @Override
  public void markReportsFixed(List<String> reportIds, boolean state) throws IllegalArgumentException
  {
    Iterator<String> iter = reportIds.iterator();
    while (iter.hasNext())
    {
      markReportFixed(iter.next(), state);
    }
  }

  @Override
  public void markReportsEMailed(List<String> reportIds, boolean state) throws IllegalArgumentException
  {
    Iterator<String> iter = reportIds.iterator();
    while (iter.hasNext())
    {
      markReportEMailed(iter.next(), state);
    }

  }

  @Override
  public void writeAppPackageShared(AppPackageShared appPackageShared) throws IllegalArgumentException
  {
    AppPackage app = ObjectifyService.ofy().load().type(AppPackage.class).id(appPackageShared.id).get();
    if (app != null)
    {
      app.fromShared(appPackageShared);
      app.save();
    }

  }

  @Override
  public void addAppPackageShared(LoginInfo loginInfo, AppPackageShared appPackageShared) throws IllegalArgumentException
  {
    AppPackage app = new AppPackage();
    app.fromShared(appPackageShared);
    app.Owner = getOwnerId(getAppUser(loginInfo));
    app.save();

  }

  AppUser getAppUser(LoginInfo loginInfo)
  {
    return ObjectifyService.ofy().load().type(AppUser.class).id(loginInfo.getAppUserShared().id).get();
  }

  @Override
  public void writeAppUserShared(AppUserShared appUserShared) throws IllegalArgumentException
  {
    AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).id(appUserShared.id).get();
    if (appUser != null)
    {
      appUser.fromShared(appUserShared);
      appUser.save();
    }

  }

  @Override
  public void addAppUserShared(LoginInfo user, AppUserShared appUserShared) throws IllegalArgumentException
  {
    AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).filter("EMailAddress", user.getEmailAddress()).first().get();
    if (appUser != null)
    {
      throw new IllegalArgumentException(user.getEmailAddress() + " is already a user!");
    }

    AppUser ap = new AppUser();

    ap.fromShared(appUserShared);

    ap.isUser = true;
    ap.isSubscriptionPaid = true;

    ap.save();

    try
    {
      Properties props = new Properties();
      Session session = Session.getInstance(props, null);
      Message msg = new MimeMessage(session);
      msg.setReplyTo(new InternetAddress[] { new InternetAddress(ap.EMailAddress, ap.FirstName + " " + ap.LastName) });
      msg.setFrom(new InternetAddress(Configuration.defaultSenderEMailAddress, Configuration.defaultSenderName));
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(Configuration.sendNewUsersEMailAddress));
      msg.setSubject("New User Signed Up - " + ap.EMailAddress);
      msg.setText("First Name: " + ap.FirstName + "\r\n" + "Last Name: " + ap.LastName + "\r\n" + "City: " + ap.City + "\r\n" + "Country: " + ap.Country + "\r\n");

      Transport.send(msg);
    } catch (Exception e)
    {
      e.printStackTrace();
      log.warning("Exception " + e.getMessage());
    }

    // analytics.
    if (Configuration.gaTrackingID != null && SystemProperty.environment.value() == SystemProperty.Environment.Value.Production)
    {
      // The app is running on App Engine...
      FocusPoint focusApp = new FocusPoint("Admin");
      FocusPoint focusPoint = new FocusPoint("UserAdded", focusApp);
      JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker("ACRA Reporter", "0.1", Configuration.gaTrackingID);
      tracker.trackSynchronously(focusPoint);
    }
  }

  @Override
  public List<MappingFileShared> getMappingFiles(String PACKAGE_NAME) throws IllegalArgumentException
  {
    ArrayList<MappingFileShared> result = new ArrayList<MappingFileShared>();

    List<MappingFile> list = ObjectifyService.ofy().load().type(MappingFile.class).filter("apppackage", PACKAGE_NAME).order("version").list();

    for (MappingFile map : list)
    {
      result.add(map.toShared());
    }
    return result;
  }

  @Override
  public void deleteMappings(List<Long> ids) throws IllegalArgumentException
  {
    ObjectifyService.ofy().delete().type(MappingFile.class).ids(ids);
  }

  @Override
  public void editMappingVersion(Long id, String version) throws IllegalArgumentException
  {
    MappingFile mf = ObjectifyService.ofy().load().type(MappingFile.class).id(id).get();
    mf.setVersion(version);
    mf.save();

  }

  @Override
  public void deleteReports(List<String> reportIds) throws IllegalArgumentException
  {
    ArrayList<Long> idsBasic = new ArrayList<Long>();
    ArrayList<Long> idsACRA = new ArrayList<Long>();
    Long owner = 0L;
    String packageName = "";
    int fixed = 0;
    int lookedAt = 0;

    for (String report_id : reportIds)
    {
      BasicErrorInfo beo = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("REPORT_ID", report_id).first().get();
      ACRALog acra = ObjectifyService.ofy().load().type(ACRALog.class).filter("REPORT_ID", report_id).first().get();
      owner = beo.Owner;
      packageName = beo.PACKAGE_NAME;

      if (beo.fixed)
      {
        fixed = fixed + 1;
      }
      if (beo.lookedAt)
      {
        lookedAt = lookedAt + 1;
      }

      idsBasic.add(beo.id);
      idsACRA.add(acra.id);
    }

    ObjectifyService.ofy().delete().type(BasicErrorInfo.class).ids(idsBasic);
    ObjectifyService.ofy().delete().type(ACRALog.class).ids(idsACRA);

    if (owner != 0L)
    {
      AppPackage appPackage = getAppPackage(packageName);
      AppUser appUser = getAppUser(owner);

      appPackage.Totals.Deleted = appPackage.Totals.Deleted + reportIds.size();
      appPackage.Totals.Reports = appPackage.Totals.Deleted - reportIds.size();
      appPackage.Totals.Fixed = appPackage.Totals.Fixed + fixed;
      appPackage.Totals.LookedAt = appPackage.Totals.LookedAt + fixed;

      appPackage.save();

      appUser.Totals.Deleted = appUser.Totals.Deleted + reportIds.size();
      appUser.Totals.Reports = appUser.Totals.Deleted - reportIds.size();
      appUser.Totals.Fixed = appUser.Totals.Fixed + fixed;
      appUser.Totals.LookedAt = appUser.Totals.LookedAt + fixed;

      appUser.save();
      DailyCounts userCounts = DailyCounts.getToday(owner);
      DailyCounts packageCounts = DailyCounts.getToday(appPackage.PACKAGE_NAME);

      userCounts.incDeletedToday(reportIds.size());
      packageCounts.incDeletedToday(reportIds.size());

      userCounts.save();
      packageCounts.save();

    }

  }

  public AppPackage getAppPackage(String packageName)
  {
    return ObjectifyService.ofy().load().type(AppPackage.class).filter("PACKAGE_NAME", packageName).first().get();
  }

  public AppUser getAppUser(Long id)
  {
    return ObjectifyService.ofy().load().type(AppUser.class).id(id).get();
  }

  @Override
  public void sendFixedEMail(LoginInfo loginInfo, List<String> reportIds, String bcc, String subject, String body) throws IllegalArgumentException
  {

    AppUser appUser = getAppUser(loginInfo);

    String[] bccs = bcc.split("\n");

    try
    {

      Properties props = new Properties();
      Session session = Session.getInstance(props, null);
      Message msg = new MimeMessage(session);
      msg.setReplyTo(new InternetAddress[] { new InternetAddress(appUser.EMailAddress, appUser.FirstName + " " + appUser.LastName) });
      msg.setFrom(new InternetAddress(Configuration.defaultSenderEMailAddress, Configuration.defaultSenderName));

      for (String recipient : bccs)
      {
        msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(recipient));
      }

      msg.setSubject(subject);
      msg.setText(body);

      Transport.send(msg);
    } catch (Exception e)
    {
      log.warning("Exception " + e.getMessage());
      throw new IllegalArgumentException(e.toString());
    }

    markReportsEMailed(reportIds, true);

  }

  @Override
  public String findEMailAddresses(List<String> reportIds) throws IllegalArgumentException
  {
    List<ACRALog> logs = ObjectifyService.ofy().load().type(ACRALog.class).filter("REPORT_ID in", reportIds).list();
    String result = "";

    for (ACRALog log : logs)
    {
      String email = Utils.findEMail(log.USER_EMAIL, log.USER_COMMENT);
      if (!Utils.isEmpty(email))
      {
        result = result + email + "\n";
      }
    }

    return result;
  }

  @Override
  public void addAppUser(AppUserShared appUserShared) throws IllegalArgumentException
  {
    AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).filter("EMailAddress", appUserShared.EMailAddress).first().get();
    if (appUser != null)
    {
      throw new IllegalArgumentException(appUserShared.EMailAddress + " is already a user!");
    }

    AppUser ap = new AppUser();

    ap.fromShared(appUserShared);

    ap.isUser = true;
    ap.isSubscriptionPaid = true;

    ap.save();

    // analytics.
    if (Configuration.gaTrackingID != null && SystemProperty.environment.value() == SystemProperty.Environment.Value.Production)
    {
      // The app is running on App Engine...
      FocusPoint focusApp = new FocusPoint("Admin");
      FocusPoint focusPoint = new FocusPoint("UserAdded", focusApp);
      JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker("ACRA Reporter", "0.1", Configuration.gaTrackingID);
      tracker.trackSynchronously(focusPoint);
    }

  }

  public long getOwnerId(AppUser appUser)
  {
    if (Configuration.appUserMode == Configuration.UserMode.umMultipleSameApps)
    {
      return appUser.adminAppUserId != null && appUser.adminAppUserId > 0 ? appUser.adminAppUserId : appUser.id;

    }
    return appUser.id;
  }

  public long getOwnerId(AppUserShared appUser)
  {
    if (Configuration.appUserMode == Configuration.UserMode.umMultipleSameApps)
    {
      return appUser.adminAppUserId != null && appUser.adminAppUserId > 0 ? appUser.adminAppUserId : appUser.id;

    }
    return appUser.id;
  }

  @Override
  public List<AppPackageShared> getPackageGraphDataTotals(LoginInfo loginInfo) throws IllegalArgumentException
  {
    AppUser appUser = getAppUser(loginInfo);
    long ownerId = getOwnerId(appUser);

    List<AppPackage> list = ObjectifyService.ofy().load().type(AppPackage.class).filter("Owner", ownerId).order("PACKAGE_NAME").list();
    // AppPackageShared[] result = new AppPackageShared[list.size()];
    List<AppPackageShared> result = new ArrayList<AppPackageShared>();

    for (AppPackage app : list)
    {

      result.add(app.toShared());

    }
    
    Collections.sort(result, new Comparator<AppPackageShared>()
    {

      @Override
      public int compare(AppPackageShared o1, AppPackageShared o2)
      {
        return (o1.Totals.NewReports() >o2.Totals.NewReports() ? -1 : (o1.Totals.NewReports()==o2.Totals.NewReports() ? 0 : 1));
      }
    });

    return result;
  }

  @Override
  public List<DailyCountsShared> getLastMonthDailyCounts(LoginInfo loginInfo) throws IllegalArgumentException
  {
    List<DailyCountsShared> result = new ArrayList<DailyCountsShared>();
    AppUser user = getAppUser(loginInfo);
    Calendar monthback = GregorianCalendar.getInstance();
    monthback.set(Calendar.HOUR, 0);
    monthback.set(Calendar.MINUTE, 0);
    monthback.set(Calendar.SECOND, 0);
    monthback.set(Calendar.MILLISECOND, 0);

    monthback.add(Calendar.MONTH, -1);
    Date monthbackdate = DailyCounts.removeTimeFromDate(monthback.getTime());

    List<DailyCounts> counts = ObjectifyService.ofy().load().type(DailyCounts.class).filter("Owner", user.id).filter("PACKAGE_NAME", null).filter("date >=", monthbackdate).list();

    for (DailyCounts count : counts)
    {
      result.add(count.toShared());
    }
    return result;
  }

  @Override
  public List<DailyCountsShared> getPackageLastMonthDailyCounts(LoginInfo loginInfo, String PACKAGE_NAME) throws IllegalArgumentException
  {
    List<DailyCountsShared> result = new ArrayList<DailyCountsShared>();
    AppUser user = getAppUser(loginInfo);
    Calendar monthback = GregorianCalendar.getInstance();
    monthback.set(Calendar.HOUR, 0);
    monthback.set(Calendar.MINUTE, 0);
    monthback.set(Calendar.SECOND, 0);
    monthback.set(Calendar.MILLISECOND, 0);

    monthback.add(Calendar.MONTH, -1);
    Date monthbackdate = DailyCounts.removeTimeFromDate(monthback.getTime());

    List<DailyCounts> counts = ObjectifyService.ofy().load().type(DailyCounts.class).filter("PACKAGE_NAME", PACKAGE_NAME).filter("date >=", monthbackdate).list();

    for (DailyCounts count : counts)
    {
      result.add(count.toShared());
    }
    return result;
  }

}
