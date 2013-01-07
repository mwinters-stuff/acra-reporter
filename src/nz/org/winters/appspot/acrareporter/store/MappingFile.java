package nz.org.winters.appspot.acrareporter.store;

import java.util.Date;

import nz.org.winters.appspot.acrareporter.shared.MappingFileShared;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;

@Entity
@Index
public class MappingFile 
{

  @Id protected Long id;
  @Index public Long Owner;
  @Index protected String apppackage;
  @Index protected String version;
  @Unindex public Date uploadDate;
  
  @Unindex
  @Serialize(zip=true) public String mapping;

  protected MappingFile(){};

  public MappingFile(AppUser appUser, String apppackage,String version)
  {
    this.Owner = appUser.id;
    this.apppackage = apppackage;
    this.version = version;
    this.mapping = "";
    this.uploadDate = new Date();
  }

  public void add(String line)
  {
    mapping = mapping + "\n" + line;
  }

  public Long getId()
  {
    return id;
  }

  public void setId(Long id)
  {
    this.id = id;
  }

  public String getApppackage()
  {
    return apppackage;
  }

  public void setApppackage(String apppackage)
  {
    this.apppackage = apppackage;
  }

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }

  public String getMapping()
  {
    return mapping;
  }

  public void setMapping(String mapping)
  {
    this.mapping = mapping;
  }
  
  public MappingFileShared toShared()
  {
    MappingFileShared shared = new MappingFileShared();
    
    shared.id = id;
    shared.Owner = Owner;
    shared.version = version;
    shared.apppackage = apppackage;
    shared.uploadDate = uploadDate;
    return shared;
  }
  
  public void fromShared(MappingFileShared shared)
  {
    version = shared.version;
  }
  
}
