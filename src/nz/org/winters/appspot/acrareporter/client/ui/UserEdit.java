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
import nz.org.winters.appspot.acrareporter.shared.AppUserShared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class UserEdit extends Composite
{
  public interface DialogCallback
  {
    public void result(boolean ok, AppUserShared appUserShared);
  }

  private static UserEditUiBinder uiBinder = GWT.create(UserEditUiBinder.class);

  @UiField
  TextBox                         textEMailAddress;
  @UiField
  TextBox                         textFirstName;
  @UiField
  TextBox                         textLastName;
  @UiField
  TextBox                         textCity;
  @UiField
  TextBox                         textCountry;
  @UiField
  TextBox                         textAuthUsername;
  @UiField
  PasswordTextBox                 textAuthPassword;
  @UiField
  TextBox                         textTrackingID;
  @UiField
  Button                          buttonOK;
  @UiField
  Button                          buttonCancel;
  private DialogCallback          callback;

  private AppUserShared           appUserShared;

  interface UserEditUiBinder extends UiBinder<Widget, UserEdit>
  {
  }

  public UserEdit(AppUserShared appUserShared, DialogCallback callback)
  {
    this.callback = callback;
    this.appUserShared = appUserShared;
    initWidget(uiBinder.createAndBindUi(this));

    textEMailAddress.setText(appUserShared.EMailAddress);
    textEMailAddress.setReadOnly(true);

    textFirstName.setText(appUserShared.FirstName);
    textLastName.setText(appUserShared.LastName);
    textCity.setText(appUserShared.City);
    textCountry.setText(appUserShared.Country);

    textAuthUsername.setText(appUserShared.AuthUsername);
    textAuthPassword.setText(appUserShared.AuthPassword);

    textTrackingID.setText(appUserShared.AnalyticsTrackingId);

  }

  @UiHandler("buttonOK")
  void onButtonOKClick(ClickEvent event)
  {
    appUserShared.FirstName = textFirstName.getText();
    appUserShared.LastName = textLastName.getText();
    appUserShared.City = textCity.getText();
    appUserShared.Country = textCountry.getText();

    appUserShared.AuthUsername = textAuthUsername.getText();
    appUserShared.AuthPassword = textAuthPassword.getText();

    appUserShared.AnalyticsTrackingId = textTrackingID.getText();

    callback.result(true, appUserShared);
  }

  @UiHandler("buttonCancel")
  void onButtonCancelClick(ClickEvent event)
  {
    callback.result(false, appUserShared);
  }

  public static void doEditDialog(AppUserShared appUserShared, final RemoteDataServiceAsync remoteService)
  {
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText("Edit User Information");

    // Create a table to layout the content
    UserEdit pet = new UserEdit(appUserShared, new UserEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, AppUserShared appUserShared)
      {
        if (ok)
        {
          remoteService.writeAppUserShared(appUserShared, new AsyncCallback<Void>()
          {

            @Override
            public void onFailure(Throwable caught)
            {

            }

            @Override
            public void onSuccess(Void result)
            {
              dialogBox.hide();

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
