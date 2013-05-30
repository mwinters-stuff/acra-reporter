package nz.org.winters.appspot.acrareporter.client.ui;

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
import nz.org.winters.appspot.acrareporter.client.RemoteDataService;
import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.shared.Utils;
import nz.org.winters.appspot.acrareporter.store.ACRALog;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.BasicErrorInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;


public class ACRAReportView extends Composite
{
  @UiField
  Label                                 captionPanelCenter;

  @UiField
  TextArea                              textStackTrace;
  @UiField
  TextArea                              textRawStackTrace;
  @UiField
  SimplePanel                           tabBuild;
  @UiField
  TextArea                              textCustomData;
  @UiField
  SimplePanel                           tabInitialConfiguration;
  @UiField
  SimplePanel                           tabCrashConfiguration;
  @UiField
  SimplePanel                           tabDisplay;
  @UiField
  TextArea                              tabUserComment;
  @UiField
  TextArea                              tabDumpSysMemInfo;
  @UiField
  TextArea                              tabLogApplication;
  @UiField
  TextArea                              tabLogcat;
  @UiField
  TextArea                              tabLogEvents;
  @UiField
  TextArea                              tabLogRadio;
  @UiField
  TextArea                              tabLogDropbox;
  @UiField
  Tree                                  tabDeviceFeatures;
  @UiField
  SimplePanel                           tabEnvironment;
  @UiField
  DisclosurePanel                       tabSharedPreferences;
  @UiField
  DisclosurePanel                       tabSettingsSystem;
  @UiField
  DisclosurePanel                       tabSettingsSecure;
  @UiField
  DisclosurePanel                       tabSettingsGlobal;
  @UiField
  TabLayoutPanel                        tabPanel;
  // @UiField(provided=true)
  // TabLayoutPanel tabPanel = new
  // ScrolledTabLayoutPanel(3.0,Unit.EM,Resources.INSTANCE.leftArrow(),Resources.INSTANCE.rightArrow());

  @UiField
  Label                                 textAppVersionCode;
  @UiField
  Label                                 textAppVersionName;
  @UiField
  Label                                 textPhoneModel;
  @UiField
  Label                                 textBrand;
  @UiField
  Label                                 textProduct;
  @UiField
  Label                                 textAndroidVersion;
  @UiField
  Label                                 textMemory;
  @UiField
  Label                                 textUserAppStartDate;
  @UiField
  Label                                 textUserCrashDate;
  @UiField
  Label                                 textDeviceID;
  @UiField
  Label                                 textUserIP;
  @UiField
  Label                                 textSilent;
  @UiField
  Label                                 textUserEMail;
  @UiField
  CheckBox                              checkLookedAt;
  @UiField
  CheckBox                              checkFixed;
  @UiField
  CheckBox                              checkEMailed;
  @UiField
  Button                                buttonDeleteReport;
  @UiField
  Button                                buttonReportRetrace;
  @UiField
  Button                                buttonReportEmail;
  
  @UiField
  DisclosurePanel                       disStackTrace;
  @UiField
  DisclosurePanel                       disRawStackTrace;

  NameValueList                         nvlBuild;
  NameValueList                         nvlInitialConfiguration;
  NameValueList                         nvlCrashConfiguration;
  NameValueList                         nvlDisplay;
  NameValueList                         nvlEnvironment;
  NameValueList                         nvlPreferences;
  NameValueList                         nvlSettingsSystem;
  NameValueList                         nvlSettingsSecure;

  private UIConstants                   constants     = (UIConstants) GWT.create(UIConstants.class);

  private LoginInfo                     mLoginInfo;
  private CallbackReloadPackageList     mCallbackReloadPackageList;
  private final RemoteDataServiceAsync  remoteService = GWT.create(RemoteDataService.class);

  private NameValueList                 nvlSettingsGlobal;

  private AppPackage              mAppPackage;

  private static ACRAReportViewUiBinder uiBinder      = GWT.create(ACRAReportViewUiBinder.class);

  interface ACRAReportViewUiBinder extends UiBinder<Widget, ACRAReportView>
  {
  }

  public interface CallbackReloadPackageList
  {
    public void reloadPackageList();
  }

  public ACRAReportView(CallbackReloadPackageList callback, LoginInfo loginInfo, AppPackage appPackage)
  {
    setLoginInfo(loginInfo);

    mCallbackReloadPackageList = callback;
    mAppPackage = appPackage;

    initWidget(uiBinder.createAndBindUi(this));

    nvlBuild = new NameValueList();
    tabBuild.add(nvlBuild);

    nvlInitialConfiguration = new NameValueList();
    tabInitialConfiguration.add(nvlInitialConfiguration);

    nvlCrashConfiguration = new NameValueList();
    tabCrashConfiguration.add(nvlCrashConfiguration);

    nvlDisplay = new NameValueList();
    tabDisplay.add(nvlDisplay);

    nvlEnvironment = new NameValueList();
    tabEnvironment.add(nvlEnvironment);

    nvlPreferences = new NameValueList();
    tabSharedPreferences.add(nvlPreferences);

    nvlSettingsSystem = new NameValueList();
    tabSettingsSystem.add(nvlSettingsSystem);

    nvlSettingsSecure = new NameValueList();
    tabSettingsSecure.add(nvlSettingsSecure);

    nvlSettingsGlobal = new NameValueList();
    tabSettingsGlobal.add(nvlSettingsGlobal);
  }

  AsyncCallback<ACRALog> mGetACRALogCallback = new AsyncCallback<ACRALog>()
                                                   {

                                                     @Override
                                                     public void onSuccess(ACRALog result)
                                                     {
                                                       populateValues(result);
                                                     }

                                                     @Override
                                                     public void onFailure(Throwable caught)
                                                     {

                                                       stopLoading();
                                                       clearData();
                                                     }
                                                   };

  private BasicErrorInfo mSelectedBasicErrorInfo;

  private ACRALog        mACRALog;

  private void populateValues(ACRALog result)
  {
    mACRALog = result;
    BasicErrorInfo beio = mSelectedBasicErrorInfo;
    if (result == null)
    {
      stopLoading();
      captionPanelCenter.setText(constants.acraReportViewErrorFetch(""));
      clearData();
      return;
    }
    captionPanelCenter.setText(constants.acraReportViewLabelTitle(result.REPORT_ID));

    textStackTrace.setText(result.MAPPED_STACK_TRACE);
    textRawStackTrace.setText(result.STACK_TRACE);
    
    if(Utils.isEmpty(result.MAPPED_STACK_TRACE))
    {
      disStackTrace.setOpen(false);
      disRawStackTrace.setOpen(true);
    }else
    {
      disStackTrace.setOpen(true);
      disRawStackTrace.setOpen(false);
    }
    
    nvlBuild.setData(result.BUILD);
    textCustomData.setText(result.CUSTOM_DATA);
    nvlInitialConfiguration.setData(result.INITIAL_CONFIGURATION);
    nvlCrashConfiguration.setData(result.CRASH_CONFIGURATION);
    nvlDisplay.setData(result.DISPLAY);
    tabUserComment.setText(result.USER_COMMENT);
    tabDumpSysMemInfo.setText(result.DUMPSYS_MEMINFO);
    tabLogApplication.setText(result.APPLICATION_LOG);
    tabLogcat.setText(result.LOGCAT);
    tabLogEvents.setText(result.EVENTSLOG);
    tabLogRadio.setText(result.RADIOLOG);
    tabLogDropbox.setText(result.DROPBOX);

    loadDeviceFeaturesTree(tabDeviceFeatures, result.DEVICE_FEATURES);

    nvlEnvironment.setData(result.ENVIRONMENT);
    nvlPreferences.setData(result.SHARED_PREFERENCES);
    nvlSettingsSystem.setData(result.SETTINGS_SYSTEM);
    nvlSettingsSecure.setData(result.SETTINGS_SECURE);
    nvlSettingsGlobal.setData(result.SETTINGS_GLOBAL);

    textAppVersionCode.setText(Integer.toString(result.APP_VERSION_CODE));
    textAppVersionName.setText(result.APP_VERSION_NAME);

    textAppVersionCode.setText(Integer.toString(result.APP_VERSION_CODE));
    textAppVersionName.setText(result.APP_VERSION_NAME);
    textPhoneModel.setText(result.PHONE_MODEL);
    textBrand.setText(result.BRAND);
    textProduct.setText(result.PRODUCT);
    textAndroidVersion.setText(result.ANDROID_VERSION);
    textMemory.setText(memToMB(result.TOTAL_MEM_SIZE) + " / " + memToMB(result.AVAILABLE_MEM_SIZE));
    textUserAppStartDate.setText(UIUtils.reportDateToLocal(result.USER_APP_START_DATE));
    textUserCrashDate.setText(UIUtils.reportDateToLocal(result.USER_CRASH_DATE));
    textDeviceID.setText(result.DEVICE_ID);
    textUserIP.setText(result.USER_IP);
    textSilent.setText(result.IS_SILENT);
    textUserEMail.setText(result.USER_EMAIL);

    checkFixed.setValue(beio.fixed);
    checkLookedAt.setValue(beio.lookedAt);
    checkEMailed.setValue(beio.emailed);
    checkFixed.setEnabled(true);
    checkLookedAt.setEnabled(true);
    checkEMailed.setEnabled(true);
    buttonDeleteReport.setEnabled(true);
    buttonReportEmail.setEnabled(true);
    buttonReportRetrace.setEnabled(true);

    stopLoading();

  }



  private String memToMB(String mem)
  {
    double value = Double.parseDouble(mem);
    double valued = (double)value / 1024.0 / 1024.0;
    
    NumberFormat df = NumberFormat.getFormat("#0.00 mb");
    
    return df.format(valued);
  }



  private void recurseAddItem(TreeItem current, String[] items, int itemIndex)
  {
    if (itemIndex < items.length)
    {
      String looking = items[itemIndex];
      for (int i = 0; i < current.getChildCount(); i++)
      {
        TreeItem child = current.getChild(i);
        if (child.getText().equalsIgnoreCase(looking))
        {
          recurseAddItem(child, items, ++itemIndex);
          child.setState(true);
          return;
        }
      }
      TreeItem child = current.addTextItem(items[itemIndex]);
      recurseAddItem(child, items, ++itemIndex);
      child.setState(true);
    }
  }

  private void loadDeviceFeaturesTree(Tree tree, String featuresString)
  {
    String lines[] = featuresString.split("\n");

    TreeItem root = tree.addTextItem(constants.android());

    for (String line : lines)
    {
      if (line.startsWith("android."))
      {
        line = line.replace("android.", "");
        String items[] = line.split("\\p{Punct}");

        recurseAddItem(root, items, 0);
      } else
      {
        root.addTextItem(line);
      }
    }
    root.setState(true);
  }

  public void clearData()
  {
    captionPanelCenter.setText(constants.acraReportViewLabelReport());
    textStackTrace.setText("");
    textRawStackTrace.setText("");
    nvlBuild.clearData();
    textCustomData.setText("");
    nvlInitialConfiguration.clearData();
    nvlCrashConfiguration.clearData();
    nvlDisplay.clearData();
    tabUserComment.setText("");
    tabDumpSysMemInfo.setText("");
    tabLogApplication.setText("");
    tabLogcat.setText("");
    tabLogEvents.setText("");
    tabLogRadio.setText("");
    tabLogDropbox.setText("");
    tabDeviceFeatures.clear();
    nvlEnvironment.clearData();
    nvlPreferences.clearData();
    nvlSettingsSystem.clearData();
    nvlSettingsSecure.clearData();
    nvlSettingsGlobal.clearData();
    textAppVersionCode.setText("");
    textAppVersionName.setText("");
    textAppVersionCode.setText("");
    textAppVersionName.setText("");
    textPhoneModel.setText("");
    textBrand.setText("");
    textProduct.setText("");
    textAndroidVersion.setText("");
    textMemory.setText("");
    textUserAppStartDate.setText("");
    textUserCrashDate.setText("");
    textDeviceID.setText("");
    textUserIP.setText("");
    textSilent.setText("");

    textUserEMail.setText("");
    checkFixed.setValue(false);
    checkLookedAt.setValue(false);
    checkEMailed.setValue(false);

    checkFixed.setEnabled(false);
    checkLookedAt.setEnabled(false);
    checkEMailed.setEnabled(false);
    buttonDeleteReport.setEnabled(false);
    buttonReportEmail.setEnabled(false);
    buttonReportRetrace.setEnabled(false);
  }

  @UiHandler("buttonDeleteReport")
  void onButtonDeleteReportClick(ClickEvent event)
  {
    if (!Window.confirm(constants.acraReportViewConfirmDelete()))
    {
      return;
    }

    startLoading();

    final BasicErrorInfo beio = mSelectedBasicErrorInfo;

    remoteService.deleteReport(beio.REPORT_ID, new AsyncCallback<Void>()
    {

      @Override
      public void onSuccess(Void result)
      {
        clearData();
        mCallbackReloadPackageList.reloadPackageList();
      }

      @Override
      public void onFailure(Throwable caught)
      {
        // TODO Auto-generated method stub
        stopLoading();

      }
    });

  }

  @UiHandler("checkLookedAt")
  void onCheckLookedAtClick(ClickEvent event)
  {
    startLoading();
    final BasicErrorInfo beio = mSelectedBasicErrorInfo;

    remoteService.markReportLookedAt(beio.REPORT_ID, checkLookedAt.getValue(), new AsyncCallback<Void>()
    {

      @Override
      public void onSuccess(Void result)
      {
        // TODO Auto-generated method stub
        beio.lookedAt = true;
        stopLoading();

      }

      @Override
      public void onFailure(Throwable caught)
      {
        // TODO Auto-generated method stub
        stopLoading();

      }
    });
  }

  @UiHandler("checkFixed")
  void onCheckFixedClick(ClickEvent event)
  {
    startLoading();

    final BasicErrorInfo beio = mSelectedBasicErrorInfo;

    remoteService.markReportFixed(beio.REPORT_ID, checkFixed.getValue(), new AsyncCallback<Void>()
    {

      @Override
      public void onSuccess(Void result)
      {
        beio.fixed = true;
        beio.lookedAt = true;
        checkLookedAt.setValue(true);
        stopLoading();

      }

      @Override
      public void onFailure(Throwable caught)
      {
        // TODO Auto-generated method stub
        stopLoading();

      }
    });
  }

  @UiHandler("checkEMailed")
  void onCheckEMailedClick(ClickEvent event)
  {
    startLoading();
    final BasicErrorInfo beio = mSelectedBasicErrorInfo;

    remoteService.markReportEMailed(beio.REPORT_ID, checkEMailed.getValue(), new AsyncCallback<Void>()
    {

      @Override
      public void onSuccess(Void result)
      {
        beio.emailed = true;
        stopLoading();
      }

      @Override
      public void onFailure(Throwable caught)
      {
        // TODO Auto-generated method stub
        stopLoading();

      }
    });
  }

  @UiHandler("buttonReportRetrace")
  void onButtonReportRetraceClick(ClickEvent event)
  {
    startLoading();
    final BasicErrorInfo beio = mSelectedBasicErrorInfo;

    remoteService.retraceReport(beio.REPORT_ID, new AsyncCallback<Void>()
    {

      @Override
      public void onSuccess(Void result)
      {
        clearData();
        remoteService.getACRALog(beio.REPORT_ID, mGetACRALogCallback);
      }

      @Override
      public void onFailure(Throwable caught)
      {
        stopLoading();
      }
    });
  }

  @UiHandler("buttonReportEmail")
  void onButtonReportEmailClick(ClickEvent event)
  {
    EMailTemplateSend.doDialog(getLoginInfo(), mAppPackage, mACRALog, remoteService, new EMailTemplateSend.DialogCallback()
    {

      @Override
      public void result(boolean ok)
      {
        if (ok)
        {
          mSelectedBasicErrorInfo.emailed = true;
          populateValues(mACRALog);
        }

      }
    });
  }

  public void showACRAReport(BasicErrorInfo beio)
  {
    mSelectedBasicErrorInfo = beio;
    captionPanelCenter.setText(constants.acraReportViewLabelTitle(beio.REPORT_ID));
    clearData();
    remoteService.getACRALog(beio.REPORT_ID, mGetACRALogCallback);

  }

  public void startLoading()
  {
    AppLoadingView.getInstance().start();

  }

  public void stopLoading()
  {
    AppLoadingView.getInstance().stop();
  }



  public LoginInfo getLoginInfo()
  {
    return mLoginInfo;
  }



  public void setLoginInfo(LoginInfo mLoginInfo)
  {
    this.mLoginInfo = mLoginInfo;
  }

}
