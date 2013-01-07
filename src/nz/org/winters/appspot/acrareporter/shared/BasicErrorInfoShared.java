package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;


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
  
}
