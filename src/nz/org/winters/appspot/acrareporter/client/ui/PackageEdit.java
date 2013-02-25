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
import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PackageEdit extends Composite
{
  private static UIConstants                   constants     = (UIConstants) GWT.create(UIConstants.class);
  public interface DialogCallback
  {
    public void result(boolean ok, AppPackageShared appPackageShared);
  }

  private static PackageEmailTemplateUiBinder uiBinder = GWT.create(PackageEmailTemplateUiBinder.class);
  @UiField
  TextBox                                     textAppName;
  @UiField
  TextBox                                     textPackage;
  @UiField
  TextBox                                     textAuthUsername;
  @UiField
  PasswordTextBox                             textAuthPassword;
  @UiField
  TextBox                                     textEMailAddress;
  @UiField
  TextBox                                     textSubject;
  @UiField
  TextArea                                    textTemplate;
  @UiField
  CheckBox                                    checkDisallowOldReports;
  @UiField
  Button                                      buttonOK;
  @UiField
  Button                                      buttonCancel;
  private DialogCallback                      callback;
  private AppPackageShared                    appPackageShared;

  interface PackageEmailTemplateUiBinder extends UiBinder<Widget, PackageEdit>
  {
  }

  public PackageEdit(DialogCallback callback)
  {
    initWidget(uiBinder.createAndBindUi(this));
    this.appPackageShared = new AppPackageShared();
    this.callback = callback;

  }

  public PackageEdit(AppPackageShared aps, DialogCallback callback)
  {
    initWidget(uiBinder.createAndBindUi(this));
    this.appPackageShared = aps;
    this.callback = callback;
    textEMailAddress.setText(aps.EMailAddress);
    textSubject.setText(aps.EMailSubject);
    textTemplate.setText(aps.EMailTemplate);
    textAppName.setText(aps.AppName);
    textPackage.setText(aps.PACKAGE_NAME);
    textPackage.setReadOnly(true);
    textAuthUsername.setText(aps.AuthUsername);
    textAuthPassword.setText(aps.AuthPassword);
    checkDisallowOldReports.setValue(aps.DiscardOldVersionReports);

  }

  @UiHandler("buttonOK")
  void onButtonOKClick(ClickEvent event)
  {
    appPackageShared.EMailAddress = textEMailAddress.getText();
    appPackageShared.EMailSubject = textSubject.getText();
    appPackageShared.EMailTemplate = textTemplate.getText();
    appPackageShared.AppName = textAppName.getText();
    appPackageShared.PACKAGE_NAME = textPackage.getText();

    appPackageShared.AuthPassword = textAuthPassword.getText();
    appPackageShared.AuthUsername = textAuthUsername.getText();
    appPackageShared.DiscardOldVersionReports = checkDisallowOldReports.getValue();

    callback.result(true, appPackageShared);
  }

  @UiHandler("buttonCancel")
  void onButtonCancelClick(ClickEvent event)
  {
    callback.result(false, appPackageShared);
  }

  public static void doEditDialog(AppPackageShared appPackageShared, final RemoteDataServiceAsync remoteService, final DialogCallback callback)
  {

    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText(constants.packageEditLabelEdit(appPackageShared.PACKAGE_NAME));

    // Create a table to layout the content
    PackageEdit pet = new PackageEdit(appPackageShared, new PackageEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, final AppPackageShared appPackageShared)
      {
        if (ok)
        {
          remoteService.writeAppPackageShared(appPackageShared, new AsyncCallback<Void>()
          {

            @Override
            public void onFailure(Throwable caught)
            {

            }

            @Override
            public void onSuccess(Void result)
            {
              dialogBox.hide();
              callback.result(true, appPackageShared);

            }

          });
        } else
        {
          dialogBox.hide();
        }

      }
    });

    pet.setWidth("100%");
    dialogBox.setWidget(pet);
    dialogBox.center();
    dialogBox.show();

  }

  public static void doAddDialog(final LoginInfo loginInfo, final RemoteDataServiceAsync remoteService, final DialogCallback callback)
  {
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText(constants.packageEditLabelAdd());

    // Create a table to layout the content
    PackageEdit pet = new PackageEdit(new PackageEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, final AppPackageShared appPackageShared)
      {
        if (ok)
        {
          remoteService.addAppPackageShared(loginInfo, appPackageShared, new AsyncCallback<Void>()
          {

            @Override
            public void onFailure(Throwable caught)
            {

            }

            @Override
            public void onSuccess(Void result)
            {
              dialogBox.hide();
              callback.result(true, appPackageShared);

            }

          });
        } else
        {
          dialogBox.hide();
        }

      }
    });

    pet.setWidth("100%");
    dialogBox.setWidget(pet);
    dialogBox.center();
    dialogBox.show();

  }
}
