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

// valid user information.
import java.io.Serializable;

import nz.org.winters.appspot.acrareporter.shared.Counts;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;

@Entity
@Index
public class AppUser implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 3382989747316823281L;
  @Id
  public Long    id;
  public String  EMailAddress;
  public String  FirstName;
  public String  LastName;
  public String  City;
  public String  Country;
  public String  AnalyticsTrackingId;
  public String  AuthString;

  public String  AndroidKey;

  public boolean isSuperDude        = false;
  public boolean isUser             = true;
  public boolean isSubscriptionPaid = false;

  public Long    adminAppUserId;

  @Serialize
  @Unindex
  public Counts  Totals             = new Counts();

  @Ignore
  public String  AuthUsername;
  @Ignore
  public String  AuthPassword;

  public AppUser()
  {
  }

//  @OnSave
//  void onSave()
//  {
//    AuthString = ServerOnlyUtils.encodeAuthString(AuthUsername,AuthPassword);
//  }
//
//  @OnLoad
//  void onLoad()
//  {
//    String[] auths = ServerOnlyUtils.decodeAuthString(AuthString);
//    if (auths != null)
//    {
//      AuthUsername = auths[0];
//      AuthPassword = auths[1];
//    }
//  }

  // public AppUserShared toShared()
  // {
  // AppUserShared shared= new AppUserShared();
  // shared.id = id;
  // shared.EMailAddress = EMailAddress;
  // shared.FirstName = FirstName;
  // shared.LastName = LastName;
  // shared.City = City;
  // shared.Country = Country;
  // shared.AnalyticsTrackingId = AnalyticsTrackingId;
  // shared.isSubscriptionPaid = isSubscriptionPaid;
  // shared.isUser = isUser;
  // shared.isSuperDude = isSuperDude;
  // shared.AuthString = AuthString;
  // shared.adminAppUserId = adminAppUserId;
  // shared.AndroidKey = AndroidKey;
  //
  // String[] auths = ServerOnlyUtils.decodeAuthString(AuthString);
  // if (auths != null)
  // {
  // shared.AuthUsername = auths[0];
  // shared.AuthPassword = auths[1];
  // }
  //
  //
  // shared.Totals.copy(Totals);
  //
  // return shared;
  // }
  //
  // public void fromShared(AppUserShared shared)
  // {
  // id = shared.id;
  // EMailAddress = shared.EMailAddress;
  // FirstName = shared.FirstName;
  // LastName = shared.LastName;
  // City = shared.City;
  // Country = shared.Country;
  // AuthString = shared.AuthString;
  // AnalyticsTrackingId = shared.AnalyticsTrackingId;
  // isSubscriptionPaid = shared.isSubscriptionPaid;
  // isUser = shared.isUser;
  // isSuperDude = shared.isSuperDude;
  // shared.AuthString = ServerOnlyUtils.encodeAuthString(shared.AuthUsername,
  // shared.AuthPassword);
  // AuthString = shared.AuthString;
  // adminAppUserId = shared.adminAppUserId;
  // AndroidKey = shared.AndroidKey;
  // }

  @Override
  public String toString()
  {
    return "AppUser [id=" + id + ", EMailAddress=" + EMailAddress + ", FirstName=" + FirstName + ", LastName=" + LastName + ", City=" + City + ", Country=" + Country + ", AnalyticsTrackingId=" + AnalyticsTrackingId + ", AuthString=" + AuthString + ", isSuperDude=" + isSuperDude + ", isUser="
        + isUser + ", isSubscriptionPaid=" + isSubscriptionPaid + ", adminAppUserId=" + adminAppUserId + ", Totals=" + Totals + "]";
  }
  // public void save()
  // {
  // ObjectifyService.ofy().save().entity(this);
  // }

}
