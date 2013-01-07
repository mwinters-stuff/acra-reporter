package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

public class AppPackageShared implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 6338620817023591690L;
  public Long id;
  public String PACKAGE_NAME;
  public String EMailAddress;
  public String EMailSubject;
  public String EMailTemplate;
  
  public String AuthString;
  public String AuthUsername;
  public String AuthPassword;
  public String AppName;
  public Long Owner;
  public Counts Totals;
  
  public AppPackageShared()
  {
    Totals = new Counts();
  }
  
  public AppPackageShared(String apppackage)
  {
    Totals = new Counts();
    this.PACKAGE_NAME = apppackage;
  }

 
}
