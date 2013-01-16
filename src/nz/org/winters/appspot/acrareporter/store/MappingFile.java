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

// MappingFile storage.
import java.util.Date;

import nz.org.winters.appspot.acrareporter.shared.MappingFileShared;

import com.googlecode.objectify.ObjectifyService;
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

  @Override
  public String toString()
  {
    return "MappingFile [id=" + id + ", Owner=" + Owner + ", apppackage=" + apppackage + ", version=" + version + ", uploadDate=" + uploadDate + "]";
  }
  public void save()
  {
    ObjectifyService.ofy().save().entity(this);
  }
  
}
