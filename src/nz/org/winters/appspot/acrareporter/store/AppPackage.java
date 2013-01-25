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

// App package information.
import nz.org.winters.appspot.acrareporter.server.ServerOnlyUtils;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.Counts;

import com.google.gson.annotations.Expose;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;

@Entity
@Index
public class AppPackage
{

  @Expose
  @Id
  public Long   id;
  
  @Expose
  @Index
  public Long   Owner;

  @Expose
  @Index
  public String PACKAGE_NAME;

  public String EMailAddress;
  public String EMailSubject;
  public String EMailTemplate;

  public String AuthString;

  @Expose
  public String AppName;

  @Expose
  @Serialize
  @Unindex
  public Counts Totals = new Counts();

  public AppPackage()
  {
  };

  public AppPackage(String apppackage)
  {
    this.PACKAGE_NAME = apppackage;
  }

  public AppPackageShared toShared()
  {
    AppPackageShared shared = new AppPackageShared();
    shared.id = id;
    shared.PACKAGE_NAME = PACKAGE_NAME;

    shared.EMailAddress = EMailAddress;
    shared.EMailSubject = EMailSubject;
    shared.EMailTemplate = EMailTemplate;
    shared.AuthString = AuthString;
    shared.AppName = AppName;
    shared.Owner = Owner;
    shared.AuthUsername = "";
    shared.AuthPassword = "";
    shared.Totals.copy(Totals);

    String[] auths = ServerOnlyUtils.decodeAuthString(AuthString);
    if (auths != null)
    {
      shared.AuthUsername = auths[0];
      shared.AuthPassword = auths[1];
    }

    return shared;
  }

  public void fromShared(AppPackageShared shared)
  {
    EMailAddress = shared.EMailAddress;
    EMailSubject = shared.EMailSubject;
    EMailTemplate = shared.EMailTemplate;

    AppName = shared.AppName;
    // / Owner = shared.Owner;
    PACKAGE_NAME = shared.PACKAGE_NAME;

    shared.AuthString = ServerOnlyUtils.encodeAuthString(shared.AuthUsername,shared.AuthPassword);
    AuthString = shared.AuthString;

  }
  
  @Override
  public String toString()
  {
    return "AppPackage [id=" + id + ", Owner=" + Owner + ", PACKAGE_NAME=" + PACKAGE_NAME + ", EMailAddress=" + EMailAddress + ", EMailSubject=" + EMailSubject + ", EMailTemplate=" + EMailTemplate + ", AuthString=" + AuthString + ", AppName=" + AppName + ", Totals=" + Totals.toString() + "]";
  }
  public void save()
  {
    ObjectifyService.ofy().save().entity(this);
  }

}
