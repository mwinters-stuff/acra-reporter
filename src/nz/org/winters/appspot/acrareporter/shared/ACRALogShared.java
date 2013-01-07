package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;

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
