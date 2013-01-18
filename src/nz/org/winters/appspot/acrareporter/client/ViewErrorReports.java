package nz.org.winters.appspot.acrareporter.client;
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
import nz.org.winters.appspot.acrareporter.client.ui.AppLoadingView;
import nz.org.winters.appspot.acrareporter.client.ui.FrontPage;
import nz.org.winters.appspot.acrareporter.client.ui.OldMainPage;
import nz.org.winters.appspot.acrareporter.client.ui.SignUp;
import nz.org.winters.appspot.acrareporter.shared.Configuration;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ViewErrorReports implements EntryPoint
{




  private static ViewErrorReportsUiBinder uiBinder = GWT.create(ViewErrorReportsUiBinder.class);
  @UiField SimplePanel panel;
 

  interface ViewErrorReportsUiBinder extends UiBinder<Widget, ViewErrorReports>
  {
  }

  // public ViewErrorReports()
  // {
  //
  // initWidget(uiBinder.createAndBindUi(this));
  // }
  //
  // public ViewErrorReports(String firstName)
  // {
  // initWidget(uiBinder.createAndBindUi(this));
  //
  // }

  private final RemoteDataServiceAsync remoteService = GWT.create(RemoteDataService.class);

  private LoginInfo                    loginInfo     = null;

//  private boolean                      mSignup;

  // private VerticalPanel loginPanel = new VerticalPanel();
  // private Label loginLabel = new Label(
  // "Please sign in to your Google Account to access the Console.");
  // private Anchor sigCnInLink = new Anchor("Sign In");

  String getBaseURL()
  {
    String baseUrl = GWT.getHostPageBaseURL();
    if (!GWT.isProdMode())
    {
      baseUrl = "http://127.0.0.1:8888/ACRAReporter.html?gwt.codesvr=127.0.0.1:9997";
    }
    return baseUrl;
  }
  
  public void onModuleLoad()
  {
    DOM.removeChild(RootPanel.getBodyElement(), DOM.getElementById("loading"));

    LoginServiceAsync loginService = GWT.create(LoginService.class);
    Window.enableScrolling(false);
    Window.setMargin("0px");

    // this little trick ensures that when using the debug instance locally that the
    // login redirects work correctly..
    String baseUrl = getBaseURL();

    loginService.login(baseUrl, new AsyncCallback<LoginInfo>()
    {
      public void onFailure(Throwable error)
      {
        GWT.log("login onFailure: " + error.getMessage());
        handleError(error);
      }

      public void onSuccess(LoginInfo result)
      {
        loginInfo = result;
        // GWT.log("login url = " + loginInfo.getLoginUrl());
        if (loginInfo.isLoggedIn())
        {
          boolean signup = false;
          if(Configuration.appUserMode == Configuration.UserMode.umMultipleSeperate)
          {
            Storage stockStore = Storage.getLocalStorageIfSupported();
            if (stockStore != null)
            {
              if (stockStore.getItem("signup") != null)
              {
                signup = stockStore.getItem("signup").equals(Boolean.toString(true));
                stockStore.removeItem("signup");
              }
            }
          }

          if (signup)
          {
            loadSignup();
          } else if (loginInfo.getAppUserShared() != null)
          {
            loadConsole();
          } else
          {
            Window.alert("Not a valid user");
            Window.Location.replace(loginInfo.getLogoutUrl());
          }
        } else if(Configuration.appUserMode ==  Configuration.UserMode.umMultipleSeperate)
        {
          loadFrontPage();
        }else
        {
          loadLogin(false);
        }
      }

    });

  }

  protected void loadSignup()
  {
    RootLayoutPanel.get().add(new SignUp(loginInfo, new SignUp.Callback()
    {

      @Override
      public void finished()
      {
        Window.Location.replace(getBaseURL());
      }
    }));
  }

  private void doLogout()
  {
    Window.Location.replace(loginInfo.getLogoutUrl());
  }

  private void loadFrontPage()
  {
    RootLayoutPanel.get().add(new FrontPage(new FrontPage.Callback()
    {

      @Override
      public void buttonWiki()
      {
        Window.Location.replace(Configuration.wikiURL);
      }

      @Override
      public void buttonSignUp()
      {
        loadLogin(true);

      }

      @Override
      public void buttonLogin()
      {
        loadLogin(false);

      }
    }));
  }

  private void loadLogin(boolean signup)
  {
    GWT.log("load login");
    // mSignup = signup;

    if (signup)
    {
      Storage stockStore = Storage.getLocalStorageIfSupported();
      if (stockStore != null)
      {
        stockStore.setItem("signup", Boolean.toString(signup));
      } else
      {
        Window.alert("HTML5 Storage Compatible Browser Required!");
      }
    }

    Window.Location.replace(loginInfo.getLoginUrl());
  }

  private void handleError(Throwable error)
  {
    Window.alert(error.getMessage());
    if (error instanceof NotLoggedInException)
    {
      Window.Location.replace(loginInfo.getLogoutUrl());
    }
  }

  private void loadConsole()
  {
    RootLayoutPanel.get().clear();
    RootLayoutPanel.get().add(uiBinder.createAndBindUi(this));

    panel.add(new OldMainPage(loginInfo));
  //  panel.add(new Overview(loginInfo));
  }

 
  
  
  public void startLoading()
  {
    AppLoadingView.getInstance().start();
    
  }

  public void stopLoading()
  {
    AppLoadingView.getInstance().stop();
  }

 
//  @UiHandler("buttonLogout")
//  void onButtonLogoutClick(ClickEvent event)
//  {
//    if(Window.confirm("Logout of your google account?"))
//    {
//      Window.Location.replace(loginInfo.getLogoutUrl());
//    }
//  }

  

  
}
