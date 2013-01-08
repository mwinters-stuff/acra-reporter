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
import java.util.Iterator;
import java.util.List;

import nz.org.winters.appspot.acrareporter.client.ui.ACRAReportView;
import nz.org.winters.appspot.acrareporter.client.ui.AppLoadingView;
import nz.org.winters.appspot.acrareporter.client.ui.FrontPage;
import nz.org.winters.appspot.acrareporter.client.ui.MainErrorsList;
import nz.org.winters.appspot.acrareporter.client.ui.MappingList;
import nz.org.winters.appspot.acrareporter.client.ui.MappingUpload;
import nz.org.winters.appspot.acrareporter.client.ui.PackageEdit;
import nz.org.winters.appspot.acrareporter.client.ui.SignUp;
import nz.org.winters.appspot.acrareporter.client.ui.UserEdit;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class ViewErrorReports implements EntryPoint, ChangeHandler
{


  public interface CallbackMainErrorReports
  {
    void startLoading();

    void stopLoading();

    void showACRAReport(BasicErrorInfoShared beio);

    void showPackage(String PACKAGE_NAME);

  }

  private static ViewErrorReportsUiBinder uiBinder = GWT.create(ViewErrorReportsUiBinder.class);
  @UiField
  ListBox                                 appsCombo;

  @UiField
  MenuItem                                miMappingsAdd;
  @UiField
  MenuItem                                miViewMappings;
  @UiField
  MenuItem                                miPackageEdit;
  @UiField
  MenuItem                                miPackageAdd;
  @UiField
  MenuItem                                miUserEdit;
  @UiField
  DockLayoutPanel                         dockLayoutPanel;

  @UiField
  Button                                  buttonLogout;
  @UiField Label textAppStats;
  @UiField Label textUserStats;

  MainErrorsList                          mMainErrorsList;
  protected BasicErrorInfoShared          mSelectedBasicErrorInfo;

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
  private List<AppPackageShared>       listAppPackages;

  private LoginInfo                    loginInfo     = null;
  private ACRAReportView               mACRAREportView;
  private boolean                      mSignup;

  // private VerticalPanel loginPanel = new VerticalPanel();
  // private Label loginLabel = new Label(
  // "Please sign in to your Google Account to access the Console.");
  // private Anchor sigCnInLink = new Anchor("Sign In");

  public void onModuleLoad()
  {
    DOM.removeChild(RootPanel.getBodyElement(), DOM.getElementById("loading"));

    LoginServiceAsync loginService = GWT.create(LoginService.class);
    Window.enableScrolling(false);
    Window.setMargin("0px");

    String baseUrl = GWT.getHostPageBaseURL();
    if (!GWT.isProdMode())
    {
      baseUrl = "http://127.0.0.1:8888/ACRAReporter.html?gwt.codesvr=127.0.0.1:9997";
    }

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
          Storage stockStore = Storage.getLocalStorageIfSupported();
          if (stockStore != null)
          {
            if (stockStore.getItem("signup") != null)
            {
              mSignup = stockStore.getItem("signup").equals(Boolean.toString(true));
              stockStore.removeItem("signup");
            }
          }

          if (mSignup)
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
        } else
        {
          loadFrontPage();
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
        Window.Location.replace("/");
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
        Window.Location.replace("http://www.winters.org.nz/acra-reporter");
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

    mMainErrorsList = new MainErrorsList(mCallbackMainErrorReports);
    mACRAREportView = new ACRAReportView(mCallbackMainErrorReports);
    dockLayoutPanel.addWest(mMainErrorsList, 280);
    dockLayoutPanel.add(mACRAREportView);

    appsCombo.addChangeHandler(this);
    
    
    textUserStats.setText("Totals - " + loginInfo.getAppUserShared().Totals.toLabelString());
    textAppStats.setText("");

    setupMenus();
    mACRAREportView.clearData();

    remoteService.getPackages(loginInfo, mGetPackages);

  }

  AsyncCallback<List<AppPackageShared>> mGetPackages = new AsyncCallback<List<AppPackageShared>>()
                                                     {

                                                       @Override
                                                       public void onSuccess(List<AppPackageShared> result)
                                                       {
                                                         String selected_package = "";
                                                         if (appsCombo.getSelectedIndex() >= 0)
                                                         {
                                                           selected_package = appsCombo.getItemText(appsCombo.getSelectedIndex());
                                                         }

                                                         appsCombo.clear();
                                                         listAppPackages = result;

                                                         // for(AppPackageShared
                                                         // app: result)
                                                         // {
                                                         int selectIndex = 0;
                                                         Iterator<AppPackageShared> iter = result.iterator();
                                                         while (iter.hasNext())
                                                         {
                                                           AppPackageShared app = iter.next();

                                                           String item = app.AppName != null && app.AppName.length() > 0 ? app.AppName : app.PACKAGE_NAME;
                                                           appsCombo.addItem(item);
                                                           if (item.equalsIgnoreCase(selected_package))
                                                           {
                                                             selectIndex = appsCombo.getItemCount() - 1;
                                                           }

                                                         }
                                                         if (!result.isEmpty())
                                                         {
                                                           appsCombo.setSelectedIndex(selectIndex);
                                                           String apppackage = listAppPackages.get(selectIndex).PACKAGE_NAME;
                                                           mMainErrorsList.setAppPackage(apppackage);
                                                           remoteService.getPackage(apppackage, new AsyncGetPackage());
                                                         } else
                                                         {
                                                           stopLoading();
                                                         }

                                                       }

                                                       @Override
                                                       public void onFailure(Throwable caught)
                                                       {
                                                         stopLoading();
                                                         listAppPackages = null;

                                                         // errorLabel.setText("Remote Procedure Call - Failure "
                                                         // +
                                                         // caught.getMessage());
                                                         // errorLabel.setVisible(true);
                                                         // sendButton.setEnabled(false);
                                                       }
                                                     };

  private void setupMenus()
  {

    miMappingsAdd.setScheduledCommand(new Command()
    {

      @Override
      public void execute()
      {
        if (listAppPackages == null || listAppPackages.isEmpty())
          return;

        AppPackageShared aps = listAppPackages.get(appsCombo.getSelectedIndex());

        MappingUpload.doEditDialog(aps.PACKAGE_NAME, new MappingUpload.DialogCallback()
        {

          @Override
          public void result(boolean ok)
          {
            // TODO Auto-generated method stub

          }
        });

      }
    });

    miViewMappings.setScheduledCommand(new Command()
    {

      @Override
      public void execute()
      {
        if (listAppPackages == null || listAppPackages.isEmpty())
          return;

        AppPackageShared aps = listAppPackages.get(appsCombo.getSelectedIndex());

        MappingList.doDialog(aps.PACKAGE_NAME, new MappingList.DialogCallback()
        {

          @Override
          public void closed()
          {
            // TODO Auto-generated method stub

          }
        });

      }
    });

    miPackageEdit.setScheduledCommand(new Command()
    {

      @Override
      public void execute()
      {
        if (listAppPackages == null || listAppPackages.isEmpty())
          return;

        AppPackageShared aps = listAppPackages.get(appsCombo.getSelectedIndex());
        PackageEdit.doEditDialog(aps, remoteService, new PackageEdit.DialogCallback()
        {

          @Override
          public void result(boolean ok, AppPackageShared appPackageShared)
          {
            remoteService.getPackages(loginInfo, mGetPackages);
          }
        });

      }
    });

    miPackageAdd.setScheduledCommand(new Command()
    {

      @Override
      public void execute()
      {
        PackageEdit.doAddDialog(loginInfo, remoteService, new PackageEdit.DialogCallback()
        {

          @Override
          public void result(boolean ok, AppPackageShared appPackageShared)
          {
            remoteService.getPackages(loginInfo, mGetPackages);
          }
        });
      }
    });

    miUserEdit.setScheduledCommand(new Command()
    {

      @Override
      public void execute()
      {
        UserEdit.doEditDialog(loginInfo.getAppUserShared(), remoteService);
      }

    });
  }

  @Override
  public void onChange(ChangeEvent event)
  {
    mACRAREportView.clearData();
    if (appsCombo.getSelectedIndex() >= 0)
    {
      startLoading();
      String apppackage = listAppPackages.get(appsCombo.getSelectedIndex()).PACKAGE_NAME;

      mMainErrorsList.setAppPackage(apppackage);
      
      remoteService.getPackage(apppackage, new AsyncGetPackage());
    }

  }

  
  AppLoadingView appLoadingView = new AppLoadingView();
  public void startLoading()
  {
    mCallbackMainErrorReports.startLoading();
    
  }

  public void stopLoading()
  {
    mCallbackMainErrorReports.stopLoading();
  }

  CallbackMainErrorReports mCallbackMainErrorReports = new CallbackMainErrorReports()
                                                     {

                                                       @Override
                                                       public void stopLoading()
                                                       {
                                                         appLoadingView.stopProcessing();
                                                       }

                                                       @Override
                                                       public void startLoading()
                                                       {
                                                         // TODO Auto-generated
                                                         // method stub
                                                         appLoadingView.startProcessing();
                                                       }

                                                       @Override
                                                       public void showACRAReport(BasicErrorInfoShared beio)
                                                       {
                                                         mACRAREportView.showACRAReport(loginInfo, beio);

                                                       }

                                                       @Override
                                                       public void showPackage(String PACKAGE_NAME)
                                                       {
                                                         mMainErrorsList.setAppPackage(PACKAGE_NAME);

                                                       }
                                                     };

  @UiHandler("buttonLogout")
  void onButtonLogoutClick(ClickEvent event)
  {
    if(Window.confirm("Logout of your google account?"))
    {
      Window.Location.replace(loginInfo.getLogoutUrl());
    }
  }

  
  private final class AsyncGetPackage implements AsyncCallback<AppPackageShared>
  {
    @Override
    public void onSuccess(AppPackageShared result)
    {
      textAppStats.setText("App Stats - " + result.Totals.toLabelString());
    }

    @Override
    public void onFailure(Throwable caught)
    {
      textAppStats.setText("");
      
    }
  }
  
}
