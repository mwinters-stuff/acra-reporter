package nz.org.winters.appspot.acrareporter.client.ui;

import nz.org.winters.appspot.acrareporter.client.RemoteDataService;
import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.client.ui.ACRAReportView.CallbackReloadPackageList;
import nz.org.winters.appspot.acrareporter.client.ui.MainErrorsList.CallbackShowReport;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

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
  protected BasicErrorInfoShared        mSelectedBasicErrorInfo;

  private LoginInfo                     mLoginInfo;
  private AppPackageShared              mAppPackageShared;

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

  public AppPackageView(LoginInfo loginInfo, AppPackageShared appPackageShared, CallbackClosePackageView callbackClose)
  {
    mCallbackClosePackageView = callbackClose;
    mAppPackageShared = appPackageShared;
    mLoginInfo = loginInfo;
    initWidget(uiBinder.createAndBindUi(this));

    mACRAReportView = new ACRAReportView(this, loginInfo, appPackageShared);
    mMainErrorsList = new MainErrorsList(this, loginInfo, appPackageShared);

    dockLayoutPanel.addWest(mMainErrorsList, 300);
    dockLayoutPanel.add(mACRAReportView);
    labelTitle.setText(constants.appPackageLabelTitle(appPackageShared.AppName,appPackageShared.PACKAGE_NAME));

    mACRAReportView.clearData();
    mMainErrorsList.setAppPackage(appPackageShared.PACKAGE_NAME);
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
  public void showReport(BasicErrorInfoShared basicErrorInfo)
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
    MappingList.doDialog(mAppPackageShared.PACKAGE_NAME, new MappingList.DialogCallback()
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
    PackageEdit.doEditDialog(mAppPackageShared, remoteService, new PackageEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, AppPackageShared appPackageShared)
      {
        if (ok)
        {
          labelTitle.setText(constants.appPackageLabelTitle(appPackageShared.AppName,appPackageShared.PACKAGE_NAME));
        }
      }
    });
  }
}
