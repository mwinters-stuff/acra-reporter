package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MappingFileShared implements Serializable, IsSerializable
{

  /**
   * 
   */
  private static final long serialVersionUID = 8751499931836325827L;
  public MappingFileShared(){
  
  }
  
  public Long id;
  public Long Owner;
  public String apppackage;
  public String version;
  public Date uploadDate;
  

}
