package nz.org.winters.appspot.acrareporter.client.ui;

import nz.org.winters.appspot.acrareporter.client.RemoteDataService;
import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class AppPackageView extends Composite
{
  private final RemoteDataServiceAsync  remoteService = GWT.create(RemoteDataService.class);

  @UiField
  DockLayoutPanel                       dockLayoutPanel;

  private MainErrorsList                mMainErrorsList;
  private ACRAReportView                mACRAReportView;

  private CallbackMainErrorReports mCallbackMainErrorReports;
  
  protected BasicErrorInfoShared        mSelectedBasicErrorInfo;
  private AppPackageShared mAppPackage;

  private String mPackageName;

  private LoginInfo mLoginInfo;
  
  private static AppPackageViewUiBinder uiBinder      = GWT.create(AppPackageViewUiBinder.class);

  interface AppPackageViewUiBinder extends UiBinder<Widget, AppPackageView>
  {
  }

  public AppPackageView(LoginInfo loginInfo, CallbackMainErrorReports callbackMainErrorReports)
  {
    mCallbackMainErrorReports = callbackMainErrorReports;
    mLoginInfo = loginInfo;
    initWidget(uiBinder.createAndBindUi(this));
    mMainErrorsList = new MainErrorsList(mCallbackMainErrorReports);
    mACRAReportView = new ACRAReportView(mCallbackMainErrorReports);
    dockLayoutPanel.addWest(mMainErrorsList, 280);
    dockLayoutPanel.add(mACRAReportView);
    mACRAReportView.clearData();
  }

  public void setAppPackage(String PACKAGE_NAME)
  {
    mPackageName = PACKAGE_NAME;
    mMainErrorsList.setAppPackage(PACKAGE_NAME);
    remoteService.getPackage(PACKAGE_NAME, new AsyncGetPackage());
    
  }

  private final class AsyncGetPackage implements AsyncCallback<AppPackageShared>
  {
    
    @Override
    public void onSuccess(AppPackageShared result)
    {
      mAppPackage = result;
     // textAppStats.setText("App Stats - " + result.Totals.toLabelString());
      stopLoading();
    }

    @Override
    public void onFailure(Throwable caught)
    {
     // textAppStats.setText("");
      stopLoading();
    }
  }

  public void clearData()
  {
    mACRAReportView.clearData();
    
  }

  public void showACRAReport(BasicErrorInfoShared beio)
  {
    mSelectedBasicErrorInfo = beio;
    mACRAReportView.showACRAReport(mLoginInfo, beio);
    
  }
  
  public void startLoading()
  {
    AppLoadingView.getInstance().start();
    
  }

  public void stopLoading()
  {
    AppLoadingView.getInstance().stop();
  }

}
