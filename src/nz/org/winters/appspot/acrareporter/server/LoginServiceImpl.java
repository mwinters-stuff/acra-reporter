package nz.org.winters.appspot.acrareporter.server;

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
import nz.org.winters.appspot.acrareporter.client.LoginService;
import nz.org.winters.appspot.acrareporter.shared.Configuration;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.store.AppUser;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.ObjectifyService;

///_ah/logout?continue=http%3A%2F%2F127.0.0.1%3A8888%2FACRAReporter.html%3Fgwt.codesvr%3D127.0.0.1%3A9997

public class LoginServiceImpl extends RemoteServiceServlet implements LoginService
{

  static
  {
    RegisterDataStores.register();

  }

  private static final long serialVersionUID = 4769899395757286696L;

  private void decodeAuthString(AppUser user)
  {
    if (user != null)
    {
      String[] auths = ServerOnlyUtils.decodeAuthString(user.AuthString);
      if (auths != null)
      {
        user.AuthUsername = auths[0];
        user.AuthPassword = auths[1];
      }
    }
  }

  @Override
  public LoginInfo login(String requestUri) throws IllegalArgumentException
  {
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    LoginInfo loginInfo = new LoginInfo();

    loginInfo.setLogoutUrl(userService.createLogoutURL(requestUri));
    loginInfo.setLoginUrl(userService.createLoginURL(requestUri));

    
    if (user != null)
    {
      loginInfo.setEmailAddress(user.getEmail());
      loginInfo.setNickname(user.getNickname());

      if (userService.isUserLoggedIn())
      { // && userService.isUserAdmin()
        loginInfo.setUserAdmin(userService.isUserAdmin());
        AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).filter("EMailAddress", user.getEmail()).first().get();
        loginInfo.setAppUserShared(appUser == null ? null : appUser);
        decodeAuthString(appUser);

        if (Configuration.appUserMode == Configuration.UserMode.umSingle)
        {
          if (userService.isUserAdmin())
          {
            if (appUser == null)
            {
              appUser = ObjectifyService.ofy().load().type(AppUser.class).first().get();
              if (appUser != null)
              {
                decodeAuthString(appUser);
                if (!appUser.EMailAddress.equals(user.getEmail()))
                {
                  // not the administator.
                  loginInfo.setLoggedIn(false);
                  return loginInfo;
                }
              }

              appUser = new AppUser();
              appUser.EMailAddress = user.getEmail();
              appUser.FirstName = user.getNickname();
              appUser.LastName = "UNKNOWN";
              appUser.City = "";
              appUser.Country = "";
              appUser.isSubscriptionPaid = true;
              appUser.isSuperDude = true;
              appUser.isUser = true;
              ObjectifyService.ofy().save().entity(appUser);
              loginInfo.setLoggedIn(true);
            } else
            {
              loginInfo.setLoggedIn(true);
            }

          } else
          {
            loginInfo.setLoggedIn(false);
            return loginInfo;
          }
        } else if (Configuration.appUserMode == Configuration.UserMode.umMultipleSameApps)
        {
          if (userService.isUserAdmin())
          {
            if (appUser == null)
            {
              appUser = new AppUser();
              appUser.EMailAddress = user.getEmail();
              appUser.FirstName = user.getNickname();
              appUser.LastName = "UNKNOWN";
              appUser.City = "";
              appUser.Country = "";
              appUser.isSubscriptionPaid = true;
              appUser.isSuperDude = true;
              appUser.isUser = true;
              ObjectifyService.ofy().save().entity(appUser);
            }
            loginInfo.setAppUserShared(appUser);
            loginInfo.setLoggedIn(true);
          } else
          {
            loginInfo.setLoggedIn(appUser != null);
            return loginInfo;
          }
        } else
        {
          loginInfo.setLoggedIn(true);
        }

      }
    } else
    {
      loginInfo.setLoggedIn(false);
    }
    
    if(loginInfo.isLoggedIn() && SettingStore.has(Constants.SETTING_DATABASEVERSION) && Integer.parseInt(SettingStore.get(Constants.SETTING_DATABASEVERSION, "1")) < Constants.databaseVersion)
    {
      throw new IllegalArgumentException("Data store needs an upgrade! administrator needs to run dbupgrade scriptlet");
    }

    return loginInfo;
  }

}
