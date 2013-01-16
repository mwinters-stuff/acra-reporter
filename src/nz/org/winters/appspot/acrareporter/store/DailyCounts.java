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

  public static DailyCounts getDate(Long owner, Date date)
  {
    date = removeTimeFromDate(date);

    DailyCounts counts = ObjectifyService.ofy().load().type(DailyCounts.class).filter("Owner", owner).filter("date", date).first().get();
    if (counts == null)
    {
      counts = new DailyCounts();
      counts.Owner = owner;
      counts.date = date;
    }
    return counts;
  }

  public static DailyCounts getToday(String package_name)
  {
    return getDate(package_name, null);

  }

  // want to remove any time zone informaiton,  just get day month year utc.
  public static Date removeTimeFromDate(Date date) 
  {
    if(date == null)
    {
      date = new Date();
    }
    DateFormat dfm = DateFormat.getDateInstance(DateFormat.SHORT);
    String df = dfm.format(date);
    Date newdate;
    try
    {
      newdate = dfm.parse(df);
    } catch (ParseException e)
    {
      
      newdate = date;
    }
    
//    Calendar today = GregorianCalendar.getInstance();
//    if (date != null)
//    {
//      Calendar input = GregorianCalendar.getInstance();
//      input.setTime(date);
//      
//      today.set(Calendar.YEAR,input.get(Calendar.YEAR));
//      today.set(Calendar.MONTH,input.get(Calendar.MONTH));
//      today.set(Calendar.DAY_OF_MONTH,input.get(Calendar.DAY_OF_MONTH));
//    }      
//     
//    today.set(Calendar.HOUR, 0);
//    today.set(Calendar.MINUTE, 0);
//    today.set(Calendar.SECOND, 0);
//    today.set(Calendar.MILLISECOND, 0);
    return newdate;//today.getTime();
  }
  
  public static DailyCounts getDate(String package_name, Date date)
  {
      
    date = removeTimeFromDate(date);
    
    DailyCounts counts = ObjectifyService.ofy().load().type(DailyCounts.class).filter("PACKAGE_NAME", package_name).filter("date", date).first().get();
    if (counts == null)
    {
      counts = new DailyCounts();
      counts.PACKAGE_NAME = package_name;
      counts.date = date;
    }
    return counts;
  }

  public static List<DailyCounts> getAllYesterday()
  {
    Calendar yesterday = GregorianCalendar.getInstance();
    yesterday.set(Calendar.HOUR, 0);
    yesterday.set(Calendar.MINUTE, 0);
    yesterday.set(Calendar.SECOND, 0);
    yesterday.set(Calendar.MILLISECOND, 0);

    yesterday.add(Calendar.DAY_OF_MONTH, -1);
    Date yd = yesterday.getTime();
    return ObjectifyService.ofy().load().type(DailyCounts.class).filter("date", yd).list();
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

  @Override
  public String toString()
  {
    return "DailyCounts [id=" + id + ", Owner=" + Owner + ", PACKAGE_NAME=" + PACKAGE_NAME + ", date=" + date + ", Reports=" + Reports + ", Fixed=" + Fixed + ", LookedAt=" + LookedAt + ", Deleted=" + Deleted + "]";
  }

}
