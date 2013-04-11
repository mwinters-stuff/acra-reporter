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

// basic information about an error, smaller class to be query friendly
// and improve performance..
import java.io.Serializable;
import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Index
public class BasicErrorInfo implements Serializable
{

  /**
   * 
   */
  private static final long serialVersionUID = 2177572067786785505L;

  public BasicErrorInfo()
  {
    lookedAt = false;
    fixed = false;
    emailed = false;
  };

  @Id
  public Long    id;
  @Index
  public Long    Owner;
  public Date    Timestamp;
  public String  APP_VERSION_NAME;
  public String  PACKAGE_NAME;
  public String  REPORT_ID;
  public String  ANDROID_VERSION;
  public String  USER_CRASH_DATE;

  public Boolean lookedAt;
  public Boolean fixed;
  public Boolean emailed;

//  public BasicErrorInfoShared toShared()
//  {
//    BasicErrorInfoShared shared = new BasicErrorInfoShared();
//    shared.id = id;
//    shared.Owner = Owner;
//    shared.Timestamp = Timestamp;
//    shared.APP_VERSION_NAME = APP_VERSION_NAME;
//    shared.PACKAGE_NAME = PACKAGE_NAME;
//    shared.REPORT_ID = REPORT_ID;
//    shared.ANDROID_VERSION = ANDROID_VERSION;
//    shared.USER_CRASH_DATE = USER_CRASH_DATE;
//    shared.lookedAt = lookedAt;
//    shared.fixed = fixed;
//
//    shared.emailed = emailed;
//  
// 
//
//    return shared;
//  }


  @Override
  public String toString()
  {
    return "BasicErrorInfo [id=" + id + ", Owner=" + Owner + ", Timestamp=" + Timestamp + ", APP_VERSION_NAME=" + APP_VERSION_NAME + ", PACKAGE_NAME=" + PACKAGE_NAME + ", REPORT_ID=" + REPORT_ID + ", ANDROID_VERSION=" + ANDROID_VERSION + ", USER_CRASH_DATE=" + USER_CRASH_DATE + ", lookedAt="
        + lookedAt + ", fixed=" + fixed + ", emailed=" + emailed + "]";
  }
//  public void save()
//  {
//    ObjectifyService.ofy().save().entity(this);
//  }

}
