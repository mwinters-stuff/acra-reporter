package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
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

// Serializable version of ACRALog
public class ACRALogShared implements Serializable, IsSerializable
{
  /**
   * 
   */
  private static final long serialVersionUID = -3747847425391947056L;

  public ACRALogShared(){};
  

  public Long          id;
  public Long Owner;

  public Date   Timestamp; 
  public String REPORT_ID; 
  public int    APP_VERSION_CODE;  
  public String APP_VERSION_NAME;  
  public String PACKAGE_NAME;  
  public String FILE_PATH; 
  public String PHONE_MODEL; 
  public String BRAND ;
  public String PRODUCT; 
  public String ANDROID_VERSION; 
  public String BUILD ;
  public String TOTAL_MEM_SIZE;  
  public String AVAILABLE_MEM_SIZE;  
  public String CUSTOM_DATA; 
  public String IS_SILENT ;
  public String STACK_TRACE ;
  public String INITIAL_CONFIGURATION; 
  public String CRASH_CONFIGURATION ;
  public String DISPLAY ;
  public String USER_COMMENT;  
  public String USER_EMAIL  ;
  public String USER_APP_START_DATE; 
  public String USER_CRASH_DATE ;
  public String DUMPSYS_MEMINFO ;
  public String LOGCAT  ;
  public String INSTALLATION_ID; 
  public String DEVICE_FEATURES ;
  public String ENVIRONMENT ;
  public String SHARED_PREFERENCES;  
  public String SETTINGS_SYSTEM ;
  public String SETTINGS_SECURE  ;
  public String MAPPED_STACK_TRACE ;
  
}
