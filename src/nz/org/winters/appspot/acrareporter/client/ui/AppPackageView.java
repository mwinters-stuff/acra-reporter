package nz.org.winters.appspot.acrareporter.client.ui;

import nz.org.winters.appspot.acrareporter.client.RemoteDataService;
import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.client.ui.ACRAReportView.CallbackReloadPackageList;
import nz.org.winters.appspot.acrareporter.client.ui.MainErrorsList.CallbackShowReport;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.BasicErrorInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class AppPackageView extends Composite implements CallbackReloadPackageList, CallbackShowReport
{
  private final RemoteDataServiceAsync  remoteService = GWT.create(RemoteDataService.class);

  @UiField
  DockLayoutPanel                       dockLayoutPanel;
  @UiField
  Button                                buttonClose;
  @UiField
  Label                                 labelTitle;

  private MainErrorsList                mMainErrorsList;
  private ACRAReportView                mACRAReportView;
  protected BasicErrorInfo        mSelectedBasicErrorInfo;

  private LoginInfo                     mLoginInfo;
  private AppPackage              mAppPackage;

  private UIConstants                   constants     = (UIConstants) GWT.create(UIConstants.class);

  private CallbackClosePackageView      mCallbackClosePackageView;

  private static AppPackageViewUiBinder uiBinder      = GWT.create(AppPackageViewUiBinder.class);

  public interface CallbackClosePackageView
  {
    public void close(AppPackageView view);
  }

  interface AppPackageViewUiBinder extends UiBinder<Widget, AppPackageView>
  {
  }

  public AppPackageView(LoginInfo loginInfo, AppPackage appPackage, CallbackClosePackageView callbackClose)
  {
    mCallbackClosePackageView = callbackClose;
    mAppPackage = appPackage;
    mLoginInfo = loginInfo;
    initWidget(uiBinder.createAndBindUi(this));

    mACRAReportView = new ACRAReportView(this, loginInfo, appPackage);
    mMainErrorsList = new MainErrorsList(this, loginInfo, appPackage);

    dockLayoutPanel.addWest(mMainErrorsList, 300);
    dockLayoutPanel.add(mACRAReportView);
    labelTitle.setText(constants.appPackageLabelTitle(appPackage.AppName,appPackage.PACKAGE_NAME));

    mACRAReportView.clearData();
    mMainErrorsList.setAppPackage(appPackage.PACKAGE_NAME);
  }

  public void clearData()
  {
    mACRAReportView.clearData();

  }

  public void startLoading()
  {
    AppLoadingView.getInstance().start();

  }

  public void stopLoading()
  {
    AppLoadingView.getInstance().stop();
  }

  @Override
  public void reloadPackageList()
  {
    mMainErrorsList.refreshList();

  }

  @Override
  public void showReport(BasicErrorInfo basicErrorInfo)
  {
    mACRAReportView.showACRAReport(basicErrorInfo);

  }

  @UiHandler("buttonClose")
  void onButtonCloseClick(ClickEvent event)
  {
    mCallbackClosePackageView.close(this);
  }

  @UiHandler("buttonMapFiles")
  void onButtonMapFilesClick(ClickEvent event)
  {
    MappingList.doDialog(mAppPackage.PACKAGE_NAME, new MappingList.DialogCallback()
    {

      @Override
      public void closed()
      {

      }
    });
  }

  @UiHandler("buttonEditPackage")
  void onButtonEditPackageClick(ClickEvent event)
  {
    PackageEdit.doEditDialog(mAppPackage, remoteService, new PackageEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, AppPackage appPackage)
      {
        if (ok)
        {
          labelTitle.setText(constants.appPackageLabelTitle(appPackage.AppName,appPackage.PACKAGE_NAME));
        }
      }
    });
  }
}
