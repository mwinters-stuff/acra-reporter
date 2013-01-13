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

// ACRA Log - this stores the error report.
import java.util.Date;

import nz.org.winters.appspot.acrareporter.shared.ACRALogShared;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;

@Entity
@Index
public class ACRALog
{

  public ACRALog()
  {
  };

  @Id
  public Long   id;
  @Index
  public Long   Owner;
  public Date   Timestamp;
  public String REPORT_ID;
  public int    APP_VERSION_CODE;
  public String APP_VERSION_NAME;
  public String PACKAGE_NAME;
  @Unindex
  public String FILE_PATH;
  public String PHONE_MODEL;
  public String BRAND;
  public String PRODUCT;
  public String ANDROID_VERSION;
  @Unindex
  @Serialize(zip = true)
  public String BUILD;
  @Unindex
  public String TOTAL_MEM_SIZE;
  @Unindex
  public String AVAILABLE_MEM_SIZE;
  @Unindex
  @Serialize(zip = true)
  public String CUSTOM_DATA;
  @Unindex
  public String IS_SILENT;
  @Unindex
  @Serialize(zip = true)
  public String STACK_TRACE;
  @Unindex
  @Serialize(zip = true)
  public String INITIAL_CONFIGURATION;
  @Unindex
  @Serialize(zip = true)
  public String CRASH_CONFIGURATION;
  @Unindex
  @Serialize(zip = true)
  public String DISPLAY;
  @Unindex
  @Serialize(zip = true)
  public String USER_COMMENT;
  @Unindex
  public String USER_EMAIL;
  public String USER_APP_START_DATE;
  public String USER_CRASH_DATE;
  @Unindex
  @Serialize(zip = true)
  public String DUMPSYS_MEMINFO;
  @Unindex
  @Serialize(zip = true)
  public String LOGCAT;
  @Unindex
  public String INSTALLATION_ID;
  @Unindex
  @Serialize(zip = true)
  public String DEVICE_FEATURES;
  @Unindex
  @Serialize(zip = true)
  public String ENVIRONMENT;
  @Unindex
  @Serialize(zip = true)
  public String SHARED_PREFERENCES;
  @Unindex
  @Serialize(zip = true)
  public String SETTINGS_SYSTEM;
  @Unindex
  @Serialize(zip = true)
  public String SETTINGS_SECURE;

  @Unindex
  @Serialize(zip = true)
  public String MAPPED_STACK_TRACE;

  @Unindex
  @Serialize(zip = true)
  public String APPLICATION_LOG;
  
  public String DEVICE_ID;

  @Unindex
  @Serialize(zip = true)
  public String DROPBOX;

  @Unindex
  @Serialize(zip = true)
  public String EVENTSLOG;

  @Unindex
  @Serialize(zip = true)
  public String MEDIA_CODEC_LIST;

  @Unindex
  @Serialize(zip = true)
  public String RADIOLOG;

  @Unindex
  @Serialize(zip = true)
  public String SETTINGS_GLOBAL;

  @Unindex
  @Serialize(zip = true)
  public String THREAD_DETAILS;

  @Unindex
  public String USER_IP;

  public ACRALogShared toShared()
  {
    ACRALogShared shared = new ACRALogShared();

    shared.id = id;
    shared.Timestamp = Timestamp;
    shared.REPORT_ID = REPORT_ID;
    shared.APP_VERSION_CODE = APP_VERSION_CODE;
    shared.APP_VERSION_NAME = APP_VERSION_NAME;
    shared.PACKAGE_NAME = PACKAGE_NAME;
    shared.FILE_PATH = FILE_PATH;
    shared.PHONE_MODEL = PHONE_MODEL;
    shared.BRAND = BRAND;
    shared.PRODUCT = PRODUCT;
    shared.ANDROID_VERSION = ANDROID_VERSION;
    shared.BUILD = BUILD;
    shared.TOTAL_MEM_SIZE = TOTAL_MEM_SIZE;
    shared.AVAILABLE_MEM_SIZE = AVAILABLE_MEM_SIZE;
    shared.CUSTOM_DATA = CUSTOM_DATA;
    shared.IS_SILENT = IS_SILENT;
    shared.STACK_TRACE = STACK_TRACE;
    shared.INITIAL_CONFIGURATION = INITIAL_CONFIGURATION;
    shared.CRASH_CONFIGURATION = CRASH_CONFIGURATION;
    shared.DISPLAY = DISPLAY;
    shared.USER_COMMENT = USER_COMMENT;
    shared.USER_EMAIL = USER_EMAIL;
    shared.USER_APP_START_DATE = USER_APP_START_DATE;
    shared.USER_CRASH_DATE = USER_CRASH_DATE;
    shared.DUMPSYS_MEMINFO = DUMPSYS_MEMINFO;
    shared.LOGCAT = LOGCAT;
    shared.INSTALLATION_ID = INSTALLATION_ID;
    shared.DEVICE_FEATURES = DEVICE_FEATURES;
    shared.ENVIRONMENT = ENVIRONMENT;
    shared.SHARED_PREFERENCES = SHARED_PREFERENCES;
    shared.SETTINGS_SYSTEM = SETTINGS_SYSTEM;
    shared.SETTINGS_SECURE = SETTINGS_SECURE;
    shared.MAPPED_STACK_TRACE = MAPPED_STACK_TRACE;
    shared.Owner = Owner;
    
    shared.APPLICATION_LOG = APPLICATION_LOG;
    shared.DEVICE_ID = DEVICE_ID;
    shared.DROPBOX = DROPBOX;
    shared.EVENTSLOG = EVENTSLOG;
    shared.MEDIA_CODEC_LIST = MEDIA_CODEC_LIST;
    shared.RADIOLOG = RADIOLOG;
    shared.SETTINGS_GLOBAL = SETTINGS_GLOBAL;
    shared.THREAD_DETAILS = THREAD_DETAILS;
    shared.USER_IP = USER_IP;
    return shared;
  }

  @OnLoad
  void onLoad()
  {
    if (this.Owner == null)
    {
      this.Owner = 12038L;
    }
  }

  @Override
  public String toString()
  {
    return "ACRALog [id=" + id + ", Owner=" + Owner + ", Timestamp=" + Timestamp + ", REPORT_ID=" + REPORT_ID + ", APP_VERSION_NAME=" + APP_VERSION_NAME + ", PACKAGE_NAME=" + PACKAGE_NAME + "]";
  }

  
}
