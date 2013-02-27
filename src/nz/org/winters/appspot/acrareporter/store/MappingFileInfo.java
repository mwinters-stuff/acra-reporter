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
import java.io.Serializable;
import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
@Index
public class MappingFileInfo implements Serializable
{

  /**
   * 
   */
  private static final long serialVersionUID = -8691879167151824926L;
  @Id
  public Long               id;
  @Index
  public Long               Owner;
  @Index
  public String             PACKAGE_NAME;
  @Index
  public String             version;
  @Index
  public Date               uploadDate;

  protected MappingFileInfo()
  {
  };

  public MappingFileInfo(AppUser appUser, String apppackage, String version)
  {
    this.Owner = appUser.id;
    this.PACKAGE_NAME = apppackage;
    this.version = version;
    this.uploadDate = new Date();
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
    return PACKAGE_NAME;
  }

  public void setApppackage(String apppackage)
  {
    this.PACKAGE_NAME = apppackage;
  }

  public String getVersion()
  {
    return version;
  }

  public void setVersion(String version)
  {
    this.version = version;
  }

  @Override
  public String toString()
  {
    return "MappingFile [id=" + id + ", Owner=" + Owner + ", apppackage=" + PACKAGE_NAME + ", version=" + version + ", uploadDate=" + uploadDate + "]";
  }

}
