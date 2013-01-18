package nz.org.winters.appspot.acrareporter.shared;

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

import java.io.Serializable;
// counters for daily reporting.
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DailyCountsShared extends Counts implements Serializable, IsSerializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 2839161856053953890L;
  public Long               id;
  public Long               Owner;
  public String             PACKAGE_NAME;
  public Date               date;
  public String             dateString; 

  public DailyCountsShared()
  {
    super();
  }

  @Override
  public String toString()
  {
    return "DailyCounts [id=" + id + ", Owner=" + Owner + ", PACKAGE_NAME=" + PACKAGE_NAME + ", date=" + date + ", Reports=" + Reports + ", Fixed=" + Fixed + ", LookedAt=" + LookedAt + ", Deleted=" + Deleted + "]";
  }

}
