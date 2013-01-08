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
import nz.org.winters.appspot.acrareporter.client.ViewErrorReports;
import nz.org.winters.appspot.acrareporter.client.ViewErrorReports.CallbackMainErrorReports;
import nz.org.winters.appspot.acrareporter.shared.ACRALogShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.shared.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
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
  CaptionPanel                          captionPanelCenter;

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
  TextArea                              tabLogcat;
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

  NameValueList                         nvlBuild;
  NameValueList                         nvlInitialConfiguration;
  NameValueList                         nvlCrashConfiguration;
  NameValueList                         nvlDisplay;
  NameValueList                         nvlEnvironment;
  NameValueList                         nvlSharedPreferences;
  NameValueList                         nvlSettingsSystem;
  NameValueList                         nvlSettingsSecure;

  private LoginInfo mLoginInfo;
  private CallbackMainErrorReports      mCallbackMainErrorReports;
  private final RemoteDataServiceAsync  remoteService = GWT.create(RemoteDataService.class);

  private static ACRAReportViewUiBinder uiBinder      = GWT.create(ACRAReportViewUiBinder.class);

  interface ACRAReportViewUiBinder extends UiBinder<Widget, ACRAReportView>
  {
  }

  
  public ACRAReportView(ViewErrorReports.CallbackMainErrorReports callback)
  {
    mCallbackMainErrorReports = callback;

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

    nvlSharedPreferences = new NameValueList();
    tabSharedPreferences.add(nvlSharedPreferences);

    nvlSettingsSystem = new NameValueList();
    tabSettingsSystem.add(nvlSettingsSystem);

    nvlSettingsSecure = new NameValueList();
    tabSettingsSecure.add(nvlSettingsSecure);

  }

  AsyncCallback<ACRALogShared> mGetACRALogCallback = new AsyncCallback<ACRALogShared>()
                                                   {

                                                     @Override
                                                     public void onSuccess(ACRALogShared result)
                                                     {
                                                       populateValues(result);
                                                     }

                                                     @Override
                                                     public void onFailure(Throwable caught)
                                                     {
                                                       // TODO Auto-generated
                                                       // method stub
                                                       mCallbackMainErrorReports.stopLoading();
                                                       clearData();
                                                     }
                                                   };

  private BasicErrorInfoShared mSelectedBasicErrorInfo;

  private ACRALogShared mACRALog;

  private void populateValues(ACRALogShared result)
  {
    mACRALog = result;
    BasicErrorInfoShared beio = mSelectedBasicErrorInfo;
    if (result == null)
    {
      mCallbackMainErrorReports.stopLoading();
      captionPanelCenter.setCaptionText("Report: " + beio.REPORT_ID + " - Error Fetching");
      clearData();
      return;
    }
    captionPanelCenter.setCaptionText("Report: " + result.REPORT_ID);

    textStackTrace.setText(Utils.isEmpty(result.MAPPED_STACK_TRACE) ? result.STACK_TRACE : result.MAPPED_STACK_TRACE);
    if (textStackTrace.getText().length() == 0)
    {
      textStackTrace.setText("Mapping not found!");
    }
    textStackTrace.setText(result.MAPPED_STACK_TRACE);
    textRawStackTrace.setText(result.STACK_TRACE);
    nvlBuild.setData(result.BUILD);
    textCustomData.setText(result.CUSTOM_DATA);
    nvlInitialConfiguration.setData(result.INITIAL_CONFIGURATION);
    nvlCrashConfiguration.setData(result.CRASH_CONFIGURATION);
    nvlDisplay.setData(result.DISPLAY);
    tabUserComment.setText(result.USER_COMMENT);
    tabDumpSysMemInfo.setText(result.DUMPSYS_MEMINFO);
    tabLogcat.setText(result.LOGCAT);

    loadDeviceFeaturesTree(tabDeviceFeatures, result.DEVICE_FEATURES);

    nvlEnvironment.setData(result.ENVIRONMENT);
    nvlSharedPreferences.setData(result.SHARED_PREFERENCES);
    nvlSettingsSystem.setData(result.SETTINGS_SYSTEM);
    nvlSettingsSecure.setData(result.SETTINGS_SECURE);

    textAppVersionCode.setText(Integer.toString(result.APP_VERSION_CODE));
    textAppVersionName.setText(result.APP_VERSION_NAME);

    textAppVersionCode.setText(Integer.toString(result.APP_VERSION_CODE));
    textAppVersionName.setText(result.APP_VERSION_NAME);
    textPhoneModel.setText(result.PHONE_MODEL);
    textBrand.setText(result.BRAND);
    textProduct.setText(result.PRODUCT);
    textAndroidVersion.setText(result.ANDROID_VERSION);
    textMemory.setText(result.TOTAL_MEM_SIZE + "/" + result.AVAILABLE_MEM_SIZE);
    textUserAppStartDate.setText(result.USER_APP_START_DATE);
    textUserCrashDate.setText(result.USER_CRASH_DATE);
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

    mCallbackMainErrorReports.stopLoading();

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

    TreeItem root = tree.addTextItem("Android");

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
    captionPanelCenter.setCaptionText("Report");
    textStackTrace.setText("");
    textRawStackTrace.setText("");
    nvlBuild.clearData();
    textCustomData.setText("");
    nvlInitialConfiguration.clearData();
    nvlCrashConfiguration.clearData();
    nvlDisplay.clearData();
    tabUserComment.setText("");
    tabDumpSysMemInfo.setText("");
    tabLogcat.setText("");
    tabDeviceFeatures.clear();
    nvlEnvironment.clearData();
    nvlSharedPreferences.clearData();
    nvlSettingsSystem.clearData();
    nvlSettingsSecure.clearData();
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
    if (!Window.confirm("Are you sure you want to delete this report?"))
    {
      return;
    }

    mCallbackMainErrorReports.startLoading();

    final BasicErrorInfoShared beio = mSelectedBasicErrorInfo;

    remoteService.deleteReport(beio.REPORT_ID, new AsyncCallback<Void>()
    {

      @Override
      public void onSuccess(Void result)
      {
        mCallbackMainErrorReports.showPackage(beio.PACKAGE_NAME);
      }

      @Override
      public void onFailure(Throwable caught)
      {
        // TODO Auto-generated method stub
        mCallbackMainErrorReports.stopLoading();

      }
    });

  }

  @UiHandler("checkLookedAt")
  void onCheckLookedAtClick(ClickEvent event)
  {
    mCallbackMainErrorReports.startLoading();
    final BasicErrorInfoShared beio = mSelectedBasicErrorInfo;

    remoteService.markReportLookedAt(beio.REPORT_ID, checkLookedAt.getValue(), new AsyncCallback<Void>()
    {

      @Override
      public void onSuccess(Void result)
      {
        // TODO Auto-generated method stub
        beio.lookedAt = true;
        mCallbackMainErrorReports.stopLoading();

      }

      @Override
      public void onFailure(Throwable caught)
      {
        // TODO Auto-generated method stub
        mCallbackMainErrorReports.stopLoading();

      }
    });
  }

  @UiHandler("checkFixed")
  void onCheckFixedClick(ClickEvent event)
  {
    mCallbackMainErrorReports.startLoading();

    final BasicErrorInfoShared beio = mSelectedBasicErrorInfo;

    remoteService.markReportFixed(beio.REPORT_ID, checkFixed.getValue(), new AsyncCallback<Void>()
    {

      @Override
      public void onSuccess(Void result)
      {
        beio.fixed = true;
        mCallbackMainErrorReports.stopLoading();

      }

      @Override
      public void onFailure(Throwable caught)
      {
        // TODO Auto-generated method stub
        mCallbackMainErrorReports.stopLoading();

      }
    });
  }

  @UiHandler("checkEMailed")
  void onCheckEMailedClick(ClickEvent event)
  {
    mCallbackMainErrorReports.startLoading();
    final BasicErrorInfoShared beio = mSelectedBasicErrorInfo;

    remoteService.markReportEMailed(beio.REPORT_ID, checkEMailed.getValue(), new AsyncCallback<Void>()
    {

      @Override
      public void onSuccess(Void result)
      {
        beio.emailed = true;
        mCallbackMainErrorReports.stopLoading();

      }

      @Override
      public void onFailure(Throwable caught)
      {
        // TODO Auto-generated method stub
        mCallbackMainErrorReports.stopLoading();

      }
    });
  }

  @UiHandler("buttonReportRetrace")
  void onButtonReportRetraceClick(ClickEvent event)
  {
    mCallbackMainErrorReports.startLoading();
    final BasicErrorInfoShared beio = mSelectedBasicErrorInfo;

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
        mCallbackMainErrorReports.stopLoading();
      }
    });
  }

  @UiHandler("buttonReportEmail")
  void onButtonReportEmailClick(ClickEvent event)
  {
    EMailTemplateSend.doDialog(mLoginInfo, mACRALog, remoteService, new EMailTemplateSend.DialogCallback()
    {
      
      @Override
      public void result(boolean ok)
      {
        if(ok)
        {
          mSelectedBasicErrorInfo.emailed = true;
          populateValues(mACRALog);
        }
        
      }
    });
  }

  public void showACRAReport(LoginInfo loginInfo, BasicErrorInfoShared beio)
  {
    mLoginInfo = loginInfo;
    mSelectedBasicErrorInfo = beio;
    captionPanelCenter.setCaptionText("Report: " + beio.REPORT_ID + " - Loading...");
    clearData();
    remoteService.getACRALog(beio.REPORT_ID, mGetACRALogCallback);

  }
}
