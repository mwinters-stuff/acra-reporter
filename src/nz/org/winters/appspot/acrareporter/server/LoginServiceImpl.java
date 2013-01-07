package nz.org.winters.appspot.acrareporter.server;

import nz.org.winters.appspot.acrareporter.client.LoginService;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.store.AppUser;
import nz.org.winters.appspot.acrareporter.store.MappingFile;
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
      if (userService.isUserLoggedIn())
      { // && userService.isUserAdmin()
        AppUser appUser = ObjectifyService.ofy().load().type(AppUser.class).filter("EMailAddress", user.getEmail()).first().get();

        loginInfo.setAppUserShared(appUser == null ? null : appUser.toShared());
        loginInfo.setEmailAddress(user.getEmail());
        loginInfo.setNickname(user.getNickname());
        loginInfo.setLoggedIn(true);
      }
    } else
    {
      loginInfo.setLoggedIn(false);
    }
    return loginInfo;
  }
  // }else
  // {
  // // if(appUser == null && userService.isUserAdmin())
  // // {
  // // appUser =
  // ObjectifyService.ofy().load().type(AppUser.class).filter("EMailAddress","mathew@winters.org.nz").first().get();
  // // if(appUser == null)
  // // {
  // // appUser = new AppUser();
  // // appUser.EMailAddress = "mathew@winters.org.nz";
  // // appUser.City = "Christchurch";
  // // appUser.Country = "New Zealand";
  // // appUser.FirstName = "Mathew";
  // // appUser.LastName = "Winters";
  // // appUser.isSubscriptionPaid = true;
  // // appUser.isSuperDude = true;
  // // appUser.isUser = true;
  // // ObjectifyService.ofy().save().entity(appUser);
  // // }

}
