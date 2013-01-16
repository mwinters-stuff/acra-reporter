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
import nz.org.winters.appspot.acrareporter.store.RegisterDataStores;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.googlecode.objectify.ObjectifyService;

public class LoginServiceImpl extends RemoteServiceServlet implements LoginService
{

  static
  {
    RegisterDataStores.register();

  }

  private static final long serialVersionUID = 4769899395757286696L;

  @Override
  public LoginInfo login(String requestUri)
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
        loginInfo.setAppUserShared(appUser == null ? null : appUser.toShared());

        if (Configuration.appUserMode == Configuration.UserMode.umSingle)
        {
          if (userService.isUserAdmin())
          {
            if (appUser == null)
            {
              appUser = ObjectifyService.ofy().load().type(AppUser.class).first().get();
              if (appUser != null)
              {
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
              appUser.save();
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
              appUser.save();
            }
            loginInfo.setAppUserShared(appUser.toShared());
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
    return loginInfo;
  }

}
