package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

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

// Serializable version of BasicErrorInfo
public class BasicErrorInfoShared implements Serializable, IsSerializable
{

  /**
   * 
   */
  private static final long serialVersionUID = -6385927522703227808L;

  public BasicErrorInfoShared()
  {
  };

  @Id
  public Long    id;
  public Long Owner;
  public Date    Timestamp;
  public String  APP_VERSION_NAME;
  public String  PACKAGE_NAME;
  public String  REPORT_ID;
  public String  ANDROID_VERSION;
  public String  USER_CRASH_DATE;

  public Boolean lookedAt;
  public Boolean fixed;
  public Boolean emailed;
  
  public String formatCrashDate;
  
  public Date crashDate;

  @Override
  public String toString()
  {
    return "BasicErrorInfoShared [id=" + id + ", Owner=" + Owner + ", Timestamp=" + Timestamp + ", APP_VERSION_NAME=" + APP_VERSION_NAME + ", PACKAGE_NAME=" + PACKAGE_NAME + ", REPORT_ID=" + REPORT_ID + ", ANDROID_VERSION=" + ANDROID_VERSION + ", USER_CRASH_DATE=" + USER_CRASH_DATE + ", lookedAt="
        + lookedAt + ", fixed=" + fixed + ", emailed=" + emailed + ", formatCrashDate=" + formatCrashDate + ", crashDate=" + crashDate + "]";
  }
  
}
