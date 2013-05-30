package nz.org.winters.appspot.acrareporter.server;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import nz.org.winters.appspot.acrareporter.store.DailyCounts;

import com.googlecode.objectify.ObjectifyService;

public class DailyCountsGetters
{
  public static DailyCounts getToday(Long owner)
  {
    return getDate(owner, null);

  }

  public static DailyCounts getDate(Long owner, Date datein)
  {
    datein = removeTimeFromDate(datein);

    DailyCounts counts = ObjectifyService.ofy().load().type(DailyCounts.class).filter("Owner", owner).filter("date", datein).first().now();
    if (counts == null)
    {
      counts = new DailyCounts();
      counts.Owner = owner;
      counts.date = datein;
    }
    return counts;
  }

  public static DailyCounts getToday(String package_name)
  {
    return getDate(package_name, null);

  }

  // want to remove any time zone informaiton, just get day month year utc.
  public static Date removeTimeFromDate(Date datein)
  {
    if (datein == null)
    {
      datein = new Date();
    }
    DateFormat dfm = DateFormat.getDateInstance(DateFormat.SHORT);
    String df = dfm.format(datein);
    Date newdate;
    try
    {
      newdate = dfm.parse(df);
    } catch (ParseException e)
    {

      newdate = datein;
    }
    return newdate;
  }

  public static DailyCounts getDate(String package_name, Date datein)
  {

    datein = removeTimeFromDate(datein);

    DailyCounts counts = ObjectifyService.ofy().load().type(DailyCounts.class).filter("PACKAGE_NAME", package_name).filter("date", datein).first().now();
    if (counts == null)
    {
      counts = new DailyCounts();
      counts.PACKAGE_NAME = package_name;
      counts.date = datein;
    }
    return counts;
  }

  public static List<DailyCounts> getAllUsersDaysBack(int daysBack)
  {
    Calendar yesterday = GregorianCalendar.getInstance();
    yesterday.set(Calendar.HOUR, 0);
    yesterday.set(Calendar.MINUTE, 0);
    yesterday.set(Calendar.SECOND, 0);
    yesterday.set(Calendar.MILLISECOND, 0);

    yesterday.add(Calendar.DAY_OF_MONTH, -daysBack);
    Date yd = removeTimeFromDate(yesterday.getTime());
    return ObjectifyService.ofy().load().type(DailyCounts.class).filter("PACKAGE_NAME", null).filter("date >=", yd).list();
  }

  public static List<DailyCounts> getUserDaysBack(Long owner, int daysBack)
  {
    Calendar yesterday = GregorianCalendar.getInstance();
    yesterday.set(Calendar.HOUR, 0);
    yesterday.set(Calendar.MINUTE, 0);
    yesterday.set(Calendar.SECOND, 0);
    yesterday.set(Calendar.MILLISECOND, 0);

    yesterday.add(Calendar.DAY_OF_MONTH, -daysBack);
    Date yd = removeTimeFromDate(yesterday.getTime());
    return ObjectifyService.ofy().load().type(DailyCounts.class).filter("Owner",owner).filter("PACKAGE_NAME",null).filter("date >=", yd).list();
  }

  public static List<DailyCounts> getAllUsersYesterday()
  {
    return getAllUsersDaysBack(1);
  }

  public static List<DailyCounts> getAllUserPackagesYesterday(Long owner)
  {
    return getAllUserPackagesDaysBack(owner, 1);
  }

  public static List<DailyCounts> getAllUserPackagesDaysBack(Long owner, int daysBack)
  {
    Calendar yesterday = GregorianCalendar.getInstance();
    yesterday.set(Calendar.HOUR, 0);
    yesterday.set(Calendar.MINUTE, 0);
    yesterday.set(Calendar.SECOND, 0);
    yesterday.set(Calendar.MILLISECOND, 0);

    yesterday.add(Calendar.DAY_OF_MONTH, -daysBack);
    Date yd = removeTimeFromDate(yesterday.getTime());
    
    
    List<String> packageNames = ServerOnlyUtils.getPackageNames(owner);
    
    return ObjectifyService.ofy().load().type(DailyCounts.class).filter("PACKAGE_NAME IN", packageNames).filter("date >=", yd).list();
  }

  public static List<DailyCounts> getPackageDaysBack(String packageName, int daysBack)
  {
    Calendar yesterday = GregorianCalendar.getInstance();
    yesterday.set(Calendar.HOUR, 0);
    yesterday.set(Calendar.MINUTE, 0);
    yesterday.set(Calendar.SECOND, 0);
    yesterday.set(Calendar.MILLISECOND, 0);

    yesterday.add(Calendar.DAY_OF_MONTH, -daysBack);
    Date yd = removeTimeFromDate(yesterday.getTime());
    
    return ObjectifyService.ofy().load().type(DailyCounts.class).filter("PACKAGE_NAME", packageName).filter("date >=", yd).list();
  }  
  
}
