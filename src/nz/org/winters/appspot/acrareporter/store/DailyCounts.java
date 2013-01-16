package nz.org.winters.appspot.acrareporter.store;

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

// counters for daily reporting.
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import nz.org.winters.appspot.acrareporter.shared.Counts;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class DailyCounts extends Counts
{
  /**
   * 
   */
  private static final long serialVersionUID = 2839161856053953890L;
  @Id
  public Long               id;
  @Index
  public Long               Owner;
  @Index
  public String             PACKAGE_NAME;
  @Index
  public Date               date;

  public DailyCounts()
  {
    super();
  }

  public static DailyCounts getToday(Long owner)
  {
    return getDate(owner, null);

  }

  public static DailyCounts getDate(Long owner, Date datein)
  {
    datein = removeTimeFromDate(datein);

    DailyCounts counts = ObjectifyService.ofy().load().type(DailyCounts.class).filter("Owner", owner).filter("date", datein).first().get();
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

    DailyCounts counts = ObjectifyService.ofy().load().type(DailyCounts.class).filter("PACKAGE_NAME", package_name).filter("date", datein).first().get();
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
    return ObjectifyService.ofy().load().type(DailyCounts.class).filter("PACKAGE_NAME", null).filter("date", yd).list();
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
    return ObjectifyService.ofy().load().type(DailyCounts.class).filter("Owner", owner).filter("PACKAGE_NAME !=", null).filter("date", yd).list();
  }

  public void save()
  {
    ObjectifyService.ofy().save().entity(this);
  }

  public void incReportToday(int count)
  {
    Reports = Reports + count;
  }

  public void incFixedToday(int count)
  {
    Fixed = Fixed + count;
  }

  public void incLookedAtToday(int count)
  {
    LookedAt = LookedAt + count;
  }

  public void incDeletedToday(int count)
  {
    Deleted = Deleted + count;
  }

  public String dateString()
  {
    DateFormat dfm = DateFormat.getDateInstance(DateFormat.SHORT);
    return dfm.format(date);
  }
  
  @Override
  public String toString()
  {
    return "DailyCounts [id=" + id + ", Owner=" + Owner + ", PACKAGE_NAME=" + PACKAGE_NAME + ", date=" + date + ", Reports=" + Reports + ", Fixed=" + Fixed + ", LookedAt=" + LookedAt + ", Deleted=" + Deleted + "]";
  }

}
