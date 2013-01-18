package nz.org.winters.appspot.acrareporter.client.ui;

import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

public interface CallbackMainErrorReports
{

  void showACRAReport(BasicErrorInfoShared beio);

  void showPackage(String PACKAGE_NAME);

  AppPackageShared getAppPackage();

  LoginInfo getLoginInfo();

}