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
import java.util.Date;

import nz.org.winters.appspot.acrareporter.shared.Counts;

import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.shared.DateTimeFormat;
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

 
//  public void save()
//  {
//    ObjectifyService.ofy().save().entity(this);
//  }

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
    DateTimeFormat fmt = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT);
    return fmt.format(date);
  }
  
  @Override
  public String toString()
  {
    return "DailyCounts [id=" + id + ", Owner=" + Owner + ", PACKAGE_NAME=" + PACKAGE_NAME + ", date=" + date + ", Reports=" + Reports + ", Fixed=" + Fixed + ", LookedAt=" + LookedAt + ", Deleted=" + Deleted + "]";
  }

//  public DailyCountsShared toShared()
//  {
//    DailyCountsShared shared = new DailyCountsShared();
//    shared.id = id;
//    shared.date = date;
//    shared.dateString = dateString();
//    shared.Deleted = Deleted;
//    shared.Fixed = Fixed;
//    shared.LookedAt = LookedAt;
//    shared.Owner = Owner;
//    shared.PACKAGE_NAME = PACKAGE_NAME;
//    shared.Reports = Reports;
//    return shared;
//  }
  
}
