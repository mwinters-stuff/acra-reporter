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
import nz.org.winters.appspot.acrareporter.shared.Configuration;
import nz.org.winters.appspot.acrareporter.shared.ErrorListFilter;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.shared.Utils;
import nz.org.winters.appspot.acrareporter.store.ACRALog;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.AppUser;
import nz.org.winters.appspot.acrareporter.store.BasicErrorInfo;
import nz.org.winters.appspot.acrareporter.store.DailyCounts;
import nz.org.winters.appspot.acrareporter.store.MappingFile;

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
  public List<AppPackage> getPackages(LoginInfo loginInfo) throws IllegalArgumentException
  {
    AppUser appUser = getAppUser(loginInfo);
    long ownerId = getOwnerId(appUser);

    List<AppPackage> list = ObjectifyService.ofy().load().type(AppPackage.class).filter("Owner", ownerId).order("PACKAGE_NAME").list();

    for (AppPackage p : list)
    {
      String[] auths = ServerOnlyUtils.decodeAuthString(p.AuthString);
      if (auths != null)
      {
        p.AuthUsername = auths[0];
        p.AuthPassword = auths[1];
      }
    }

    return new ArrayList<AppPackage>(list);
  }

  @Override
  public List<BasicErrorInfo> getBasicErrorInfo(String apppackage, ErrorListFilter elf) throws IllegalArgumentException
  {
    List<BasicErrorInfo> list;
    switch (elf)
    {
      case elfFixed:
        list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", apppackage).filter("fixed", true).list();
        break;
      case elfLookedAt:
        list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", apppackage).filter("lookedAt", true).list();
        break;
      case elfNew:
        list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", apppackage).filter("fixed", false).filter("lookedAt", false).list();
        break;
      case elfNotFixed:
        list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", apppackage).filter("fixed", false).list();
        break;
      case elfAll:
      default:
        list = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", apppackage).list();// .order("Timestamp desc").list();
        break;

    }

    return new ArrayList<BasicErrorInfo>(list);
  }

  @Override
  public ACRALog getACRALog(String REPORT_ID) throws IllegalArgumentException
  {
    ACRALog log = ObjectifyService.ofy().load().type(ACRALog.class).filter("REPORT_ID", REPORT_ID).first().get();
    return log;
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

    DailyCounts counts = DailyCountsGetters.getDate(getOwnerId(user), bei.Timestamp);

    counts.incDeleted();
    ap.Totals.incDeleted();
    user.Totals.incDeleted();

    ObjectifyService.ofy().save().entity(counts);
    ObjectifyService.ofy().save().entity(ap);
    ObjectifyService.ofy().save().entity(user);
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

        AppUser user = getAppUser(bei.Owner);
        AppPackage ap = getAppPackage(bei.PACKAGE_NAME);
        DailyCounts userCounts = DailyCountsGetters.getDate(getOwnerId(user), bei.Timestamp);
        DailyCounts packageCounts = DailyCountsGetters.getDate(bei.PACKAGE_NAME, bei.Timestamp);
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
        ObjectifyService.ofy().save().entity(bei);
        ObjectifyService.ofy().save().entity(ap);
        ObjectifyService.ofy().save().entity(user);
        ObjectifyService.ofy().save().entity(userCounts);
        ObjectifyService.ofy().save().entity(packageCounts);
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
        DailyCounts userCounts = DailyCountsGetters.getDate(getOwnerId(user), bei.Timestamp);
        DailyCounts packageCounts = DailyCountsGetters.getDate(bei.PACKAGE_NAME, bei.Timestamp);
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
        }

        ObjectifyService.ofy().save().entity(bei);
        ObjectifyService.ofy().save().entity(ap);
        ObjectifyService.ofy().save().entity(user);
        ObjectifyService.ofy().save().entity(userCounts);
        ObjectifyService.ofy().save().entity(packageCounts);

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
        ObjectifyService.ofy().save().entity(acraLog);
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
      ObjectifyService.ofy().save().entity(bei);

    }

  }

  @Override
  public AppPackage getPackage(String PACKAGE_NAME) throws IllegalArgumentException
  {
    AppPackage app = ObjectifyService.ofy().load().type(AppPackage.class).filter("PACKAGE_NAME", PACKAGE_NAME).first().get();
    if (app != null)
    {
      String[] auths = ServerOnlyUtils.decodeAuthString(app.AuthString);
      if (auths != null)
      {
        app.AuthUsername = auths[0];
        app.AuthPassword = auths[1];
      }
      return app;
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
  public void writeAppPackage(AppPackage appPackage) throws IllegalArgumentException
  {
    AppPackage app = ObjectifyService.ofy().load().type(AppPackage.class).id(appPackage.id).get();
    if (app != null)
    {
      appPackage.AuthString = ServerOnlyUtils.encodeAuthString(appPackage.AuthUsername,appPackage.AuthPassword);
      ObjectifyService.ofy().save().entity(appPackage);
    }

  }

  @Override
  public void addAppPackage(LoginInfo loginInfo, AppPackage appPackage) throws IllegalArgumentException
  {
    AppPackage app = ObjectifyService.ofy().load().type(AppPackage.class).filter("PACKAGE_NAME", appPackage.PACKAGE_NAME).first().get();
    if (app != null)
    {
      throw new IllegalArgumentException("Package already exists");
    }
    appPackage.Owner = getOwnerId(getAppUser(loginInfo));
    appPackage.AuthString = ServerOnlyUtils.encodeAuthString(appPackage.AuthUsername,appPackage.AuthPassword);
    ObjectifyService.ofy().save().entity(appPackage);

  }

  AppUser getAppUser(LoginInfo loginInfo)
  {
    return ObjectifyService.ofy().load().type(AppUser.class).id(loginInfo.getAppUserShared().id).get();
  }

  @Override
  public void writeAppUser(AppUser appUserIn) throws IllegalArgumentException
  {
    AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).id(appUserIn.id).get();
    if (appUser != null)
    {

      if (!Utils.isEmpty(appUserIn.AndroidKey))
      {
        AppUser other = ObjectifyService.ofy().load().type(AppUser.class).filter("AndroidKey", appUserIn.AndroidKey).first().get();
        if (other != null)
        {
          if (other.id != appUser.id)
          {
            throw new IllegalArgumentException("Android API Key is already used by another user!");
          }
        }
      }
      appUserIn.AuthString = ServerOnlyUtils.encodeAuthString(appUserIn.AuthUsername,appUserIn.AuthPassword);

      ObjectifyService.ofy().save().entity(appUserIn);
    }

  }

  @Override
  public void addAppUser(LoginInfo user, AppUser appUserIn) throws IllegalArgumentException
  {
    AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).filter("EMailAddress", user.getEmailAddress()).first().get();
    if (appUser != null)
    {
      throw new IllegalArgumentException(user.getEmailAddress() + " is already a user!");
    }

    appUserIn.isUser = true;
    appUserIn.isSubscriptionPaid = true;
    appUserIn.AuthString = ServerOnlyUtils.encodeAuthString(appUserIn.AuthUsername,appUserIn.AuthPassword);

    ObjectifyService.ofy().save().entity(appUserIn);

    try
    {
      Properties props = new Properties();
      Session session = Session.getInstance(props, null);
      Message msg = new MimeMessage(session);
      msg.setReplyTo(new InternetAddress[] { new InternetAddress(appUserIn.EMailAddress, appUserIn.FirstName + " " + appUserIn.LastName) });
      msg.setFrom(new InternetAddress(Configuration.defaultSenderEMailAddress, Configuration.defaultSenderName));
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(Configuration.sendNewUsersEMailAddress));
      msg.setSubject("New User Signed Up - " + appUserIn.EMailAddress);
      msg.setText("First Name: " + appUserIn.FirstName + "\r\n" + "Last Name: " + appUserIn.LastName + "\r\n" + "City: " + appUserIn.City + "\r\n" + "Country: " + appUserIn.Country + "\r\n");

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
  public List<MappingFile> getMappingFiles(String PACKAGE_NAME) throws IllegalArgumentException
  {
    List<MappingFile> list = ObjectifyService.ofy().load().type(MappingFile.class).filter("apppackage", PACKAGE_NAME).order("version").list();
    return new ArrayList<MappingFile>(list);
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
    ObjectifyService.ofy().save().entity(mf);

  }

  @Override
  public void deleteReports(List<String> reportIds) throws IllegalArgumentException
  {
    ArrayList<Long> idsBasic = new ArrayList<Long>();
    ArrayList<Long> idsACRA = new ArrayList<Long>();
    Long owner = 0L;
    String packageName = "";

    for (String report_id : reportIds)
    {
      BasicErrorInfo beo = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("REPORT_ID", report_id).first().get();
      ACRALog acra = ObjectifyService.ofy().load().type(ACRALog.class).filter("REPORT_ID", report_id).first().get();
      owner = beo.Owner;
      packageName = beo.PACKAGE_NAME;

      DailyCounts userCounts = DailyCountsGetters.getDate(owner, beo.Timestamp);
      DailyCounts packageCounts = DailyCountsGetters.getDate(beo.PACKAGE_NAME, beo.Timestamp);

      userCounts.incDeleted();
      packageCounts.incDeleted();

      ObjectifyService.ofy().save().entity(userCounts);
      ObjectifyService.ofy().save().entity(packageCounts);

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
      ObjectifyService.ofy().save().entity(appPackage);
      appUser.Totals.Deleted = appUser.Totals.Deleted + reportIds.size();
      ObjectifyService.ofy().save().entity(appUser);

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
  public void addAppUser(AppUser appUserIn) throws IllegalArgumentException
  {
    AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).filter("EMailAddress", appUserIn.EMailAddress).first().get();
    if (appUser != null)
    {
      throw new IllegalArgumentException(appUser.EMailAddress + " is already a user!");
    }

    appUserIn.isUser = true;
    appUserIn.isSubscriptionPaid = true;
    appUserIn.AuthString = ServerOnlyUtils.encodeAuthString(appUserIn.AuthUsername,appUserIn.AuthPassword);

    ObjectifyService.ofy().save().entity(appUserIn);

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

  // public long getOwnerId(AppUser appUser)
  // {
  // if (Configuration.appUserMode == Configuration.UserMode.umMultipleSameApps)
  // {
  // return appUser.adminAppUserId != null && appUser.adminAppUserId > 0 ?
  // appUser.adminAppUserId : appUser.id;
  //
  // }
  // return appUser.id;
  // }

  public long getOwnerId(AppUser appUser)
  {
    if (Configuration.appUserMode == Configuration.UserMode.umMultipleSameApps)
    {
      return appUser.adminAppUserId != null && appUser.adminAppUserId > 0 ? appUser.adminAppUserId : appUser.id;

    }
    return appUser.id;
  }

  @Override
  public List<AppPackage> getPackageGraphDataTotals(LoginInfo loginInfo) throws IllegalArgumentException
  {
    AppUser appUser = getAppUser(loginInfo);
    long ownerId = getOwnerId(appUser);

    List<AppPackage> list = ObjectifyService.ofy().load().type(AppPackage.class).filter("Owner", ownerId).order("PACKAGE_NAME").list();
    // AppPackage[] result = new AppPackage[list.size()];

    Collections.sort(list, new Comparator<AppPackage>()
    {

      @Override
      public int compare(AppPackage o1, AppPackage o2)
      {
        return (o1.Totals.NewReports() > o2.Totals.NewReports() ? -1 : (o1.Totals.NewReports() == o2.Totals.NewReports() ? 0 : 1));
      }
    });

    for (AppPackage p : list)
    {
      String[] auths = ServerOnlyUtils.decodeAuthString(p.AuthString);
      if (auths != null)
      {
        p.AuthUsername = auths[0];
        p.AuthPassword = auths[1];
      }
    }    
    return new ArrayList<AppPackage>(list);
  }

  @Override
  public List<DailyCounts> getLastMonthDailyCounts(LoginInfo loginInfo) throws IllegalArgumentException
  {
    AppUser user = getAppUser(loginInfo);
    Calendar monthback = GregorianCalendar.getInstance();
    monthback.set(Calendar.HOUR, 0);
    monthback.set(Calendar.MINUTE, 0);
    monthback.set(Calendar.SECOND, 0);
    monthback.set(Calendar.MILLISECOND, 0);

    monthback.add(Calendar.MONTH, -1);
    Date monthbackdate = DailyCountsGetters.removeTimeFromDate(monthback.getTime());

    List<DailyCounts> counts = ObjectifyService.ofy().load().type(DailyCounts.class).filter("Owner", user.id).filter("PACKAGE_NAME", null).filter("date >=", monthbackdate).list();

    return new ArrayList<DailyCounts>(counts);
  }

  @Override
  public List<DailyCounts> getPackageLastMonthDailyCounts(LoginInfo loginInfo, String PACKAGE_NAME) throws IllegalArgumentException
  {
    // AppUser user = getAppUser(loginInfo);
    Calendar monthback = GregorianCalendar.getInstance();
    monthback.set(Calendar.HOUR, 0);
    monthback.set(Calendar.MINUTE, 0);
    monthback.set(Calendar.SECOND, 0);
    monthback.set(Calendar.MILLISECOND, 0);

    monthback.add(Calendar.MONTH, -1);
    Date monthbackdate = DailyCountsGetters.removeTimeFromDate(monthback.getTime());

    List<DailyCounts> counts = ObjectifyService.ofy().load().type(DailyCounts.class).filter("PACKAGE_NAME", PACKAGE_NAME).filter("date >=", monthbackdate).list();

    return new ArrayList<DailyCounts>(counts);
  }

}
