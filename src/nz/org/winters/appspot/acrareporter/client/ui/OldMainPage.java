package nz.org.winters.appspot.acrareporter.client.ui;

import java.util.Iterator;
import java.util.List;

import nz.org.winters.appspot.acrareporter.client.RemoteDataService;
import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.Configuration;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

public class OldMainPage extends Composite implements ChangeHandler
{
  private static OldMainPageUiBinder   uiBinder      = GWT.create(OldMainPageUiBinder.class);
  @UiField
  ListBox                              appsCombo;

  @UiField
  MenuItem                             miMappingsAdd;
  @UiField
  MenuItem                             miViewMappings;
  @UiField
  MenuItem                             miPackageEdit;
  @UiField
  MenuItem                             miPackageAdd;
  @UiField
  MenuItem                             miUserAdd;
  @UiField
  MenuItem                             miUserEdit;
  @UiField
  MenuItem                             miUserDelete;
  @UiField
  DockLayoutPanel                      dockLayoutPanel;

  @UiField
  Button                               buttonLogout;
  @UiField
  Label                                textAppStats;
  @UiField
  Label                                textUserStats;

  private AppPackageView               mAppPackageView;
  private List<AppPackageShared>       listAppPackages;
  private final RemoteDataServiceAsync remoteService = GWT.create(RemoteDataService.class);

  private LoginInfo                    mLoginInfo     = null;

  interface OldMainPageUiBinder extends UiBinder<Widget, OldMainPage>
  {
  }

  public OldMainPage(LoginInfo loginInfo)
  {
    mLoginInfo = loginInfo;
    initWidget(uiBinder.createAndBindUi(this));

    mAppPackageView = new AppPackageView(loginInfo, mCallbackMainErrorReports);
    dockLayoutPanel.add(mAppPackageView);

    appsCombo.addChangeHandler(this);

    textUserStats.setText("Totals - " + loginInfo.getAppUserShared().Totals.toLabelString());
    textAppStats.setText("");

    setupMenus();

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
                                                           mAppPackageView.setAppPackage(apppackage);

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
            remoteService.getPackages(mLoginInfo, mGetPackages);
          }
        });

      }
    });

    miPackageAdd.setScheduledCommand(new Command()
    {

      @Override
      public void execute()
      {
        PackageEdit.doAddDialog(mLoginInfo, remoteService, new PackageEdit.DialogCallback()
        {

          @Override
          public void result(boolean ok, AppPackageShared appPackageShared)
          {
            remoteService.getPackages(mLoginInfo, mGetPackages);
          }
        });
      }
    });

    miUserAdd.setScheduledCommand(new Command()
    {

      @Override
      public void execute()
      {
        UserEdit.doAddDialog(mLoginInfo.getAppUserShared(), remoteService);
      }

    });

    miUserAdd.setVisible(mLoginInfo.isUserAdmin() && Configuration.appUserMode == Configuration.UserMode.umMultipleSameApps);

    miUserEdit.setScheduledCommand(new Command()
    {

      @Override
      public void execute()
      {
        UserEdit.doEditDialog(mLoginInfo.getAppUserShared(), remoteService);
      }

    });

    miUserDelete.setVisible(mLoginInfo.isUserAdmin() && (Configuration.appUserMode == Configuration.UserMode.umMultipleSameApps || Configuration.appUserMode == Configuration.UserMode.umMultipleSeperate));
    miUserDelete.setScheduledCommand(new Command()
    {

      @Override
      public void execute()
      {
        // UserEdit.doEditDialog(loginInfo.getAppUserShared(), remoteService);
      }

    });
  }

  @Override
  public void onChange(ChangeEvent event)
  {
    mAppPackageView.clearData();
    if (appsCombo.getSelectedIndex() >= 0)
    {
      startLoading();
      String apppackage = listAppPackages.get(appsCombo.getSelectedIndex()).PACKAGE_NAME;

      mAppPackageView.setAppPackage(apppackage);

    }

  }
  
  CallbackMainErrorReports mCallbackMainErrorReports = new CallbackMainErrorReports()
  {

    @Override
    public void showACRAReport(BasicErrorInfoShared beio)
    {
      mAppPackageView.showACRAReport(beio);

    }

    @Override
    public void showPackage(String PACKAGE_NAME)
    {
      mAppPackageView.setAppPackage(PACKAGE_NAME);

    }

   @Override
   public AppPackageShared getAppPackage()
   {
     return listAppPackages.get(appsCombo.getSelectedIndex());
   }

   @Override
   public LoginInfo getLoginInfo()
   {
     // TODO Auto-generated method stub
     return mLoginInfo;
   }
  };

  public void startLoading()
  {
    AppLoadingView.getInstance().start();
    
  }

  public void stopLoading()
  {
    AppLoadingView.getInstance().stop();
  }

}
