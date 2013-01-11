package nz.org.winters.appspot.acrareporter.shared;
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

// configuration items to affect how the app works.
public class Configuration
{
  // usermode is how the app behaves for users.
  // umMultipleSeperate - people can sign up and operate in their own independant space.
  // umSingle - app operates for only the administrator, there is no sign-ups or other users.
  // umMultipleSameApps - administrator can add users, they see the same as the admin..
  
  public enum UserMode {umMultipleSeperate,umSingle,umMultipleSameApps}
  //public static final UserMode appUserMode = UserMode.umMultipleSameApps;
  public static final UserMode appUserMode = UserMode.umMultipleSeperate;
  //public static final UserMode appUserMode = UserMode.umSingle;
  
  
  // e-mail address must be either something@applicationid.appspotmail.com or an administrators email.
  public static final String defaultSenderEMailAddress = "acra@wintersacrareporter.appspotmail.com";
  public static final String defaultSenderName = "ACRA Reporter";
  
  // google analytics tracking app wide all users. null to disable
  public static final String gaTrackingID = "UA-37231399-1";


  // emails when a new user is registered get sent to here.
  public static final String sendNewUsersEMailAddress = "acra@winters.org.nz";
  
  public static final String wikiURL = "http://www.winters.org.nz/acra-reporter";
}
