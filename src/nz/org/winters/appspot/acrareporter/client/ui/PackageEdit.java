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
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.store.AppPackage;

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
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PackageEdit extends Composite
{
  private static UIConstants                   constants     = (UIConstants) GWT.create(UIConstants.class);
  public interface DialogCallback
  {
    public void result(boolean ok, AppPackage appPackage);
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
  IntegerBox                                  textMapsToKeep;
  @UiField
  Button                                      buttonOK;
  @UiField
  Button                                      buttonCancel;
  private DialogCallback                      callback;
  private AppPackage                    appPackage;

  interface PackageEmailTemplateUiBinder extends UiBinder<Widget, PackageEdit>
  {
  }

  public PackageEdit(DialogCallback callback)
  {
    initWidget(uiBinder.createAndBindUi(this));
    this.appPackage = new AppPackage();
    this.callback = callback;

  }

  public PackageEdit(AppPackage aps, DialogCallback callback)
  {
    initWidget(uiBinder.createAndBindUi(this));
    this.appPackage = aps;
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
    textMapsToKeep.setValue(aps.mappingsToKeep);

  }

  @UiHandler("buttonOK")
  void onButtonOKClick(ClickEvent event)
  {
    appPackage.EMailAddress = textEMailAddress.getText();
    appPackage.EMailSubject = textSubject.getText();
    appPackage.EMailTemplate = textTemplate.getText();
    appPackage.AppName = textAppName.getText();
    appPackage.PACKAGE_NAME = textPackage.getText();

    appPackage.AuthPassword = textAuthPassword.getText();
    appPackage.AuthUsername = textAuthUsername.getText();
    appPackage.DiscardOldVersionReports = checkDisallowOldReports.getValue();
    appPackage.mappingsToKeep = textMapsToKeep.getValue();
    
    callback.result(true, appPackage);
  }

  @UiHandler("buttonCancel")
  void onButtonCancelClick(ClickEvent event)
  {
    callback.result(false, appPackage);
  }

  public static void doEditDialog(AppPackage appPackage, final RemoteDataServiceAsync remoteService, final DialogCallback callback)
  {

    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText(constants.packageEditLabelEdit(appPackage.PACKAGE_NAME));

    // Create a table to layout the content
    PackageEdit pet = new PackageEdit(appPackage, new PackageEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, final AppPackage appPackage)
      {
        if (ok)
        {
          remoteService.writeAppPackage(appPackage, new AsyncCallback<Void>()
          {

            @Override
            public void onFailure(Throwable caught)
            {

            }

            @Override
            public void onSuccess(Void result)
            {
              dialogBox.hide();
              callback.result(true, appPackage);

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
      public void result(boolean ok, final AppPackage appPackage)
      {
        if (ok)
        {
          remoteService.addAppPackage(loginInfo, appPackage, new AsyncCallback<Void>()
          {

            @Override
            public void onFailure(Throwable caught)
            {

            }

            @Override
            public void onSuccess(Void result)
            {
              dialogBox.hide();
              callback.result(true, appPackage);

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
