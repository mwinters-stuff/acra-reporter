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
import nz.org.winters.appspot.acrareporter.shared.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
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
  TextBox                         textAndroidAPIKey;
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
    textAndroidAPIKey.setText(appUserShared.AndroidKey);

    textTrackingID.setText(appUserShared.AnalyticsTrackingId);

  }

  public UserEdit(DialogCallback callback)
  {
    this.callback = callback;
    this.appUserShared = new AppUserShared();
    initWidget(uiBinder.createAndBindUi(this));

    textEMailAddress.setText("");
    textEMailAddress.setReadOnly(false);

    textFirstName.setText("");
    textLastName.setText("");
    textCity.setText("");
    textCountry.setText("");

    textAuthUsername.setText("");
    textAuthPassword.setText("");
    textAuthUsername.setReadOnly(true);
    textAuthPassword.setReadOnly(true);
    
    textAndroidAPIKey.setText("");

    textTrackingID.setText("");
    textTrackingID.setReadOnly(true);
  }

  @UiHandler("buttonOK")
  void onButtonOKClick(ClickEvent event)
  {
    if (Utils.isEmpty(textEMailAddress.getText()) && !textEMailAddress.isReadOnly())
    {
      Window.alert("Please enter email address!");
      textEMailAddress.setFocus(true);
      return;
    }

    if (Utils.isEmpty(textFirstName.getText()))
    {
      Window.alert("Please enter first name!");
      textFirstName.setFocus(true);
      return;
    }
    if (Utils.isEmpty(textLastName.getText()))
    {
      Window.alert("Please enter last name!");
      textLastName.setFocus(true);

      return;
    }

    if (Utils.isEmpty(textCity.getText()))
    {
      Window.alert("Please enter town / city!");
      return;
    }

    if (Utils.isEmpty(textCountry.getText()))
    {
      Window.alert("Please enter Country!");
      return;
    }

    if (Utils.isEmpty(textAuthUsername.getText()) && !textAuthUsername.isReadOnly())
    {
      Window.alert("Please enter Authentication Username!");
      textAuthUsername.setFocus(true);
      return;
    }

    if (Utils.isEmpty(textAuthPassword.getText()) && !textAuthPassword.isReadOnly())
    {
      Window.alert("Please enter Authentication Password!");
      textAuthPassword.setFocus(true);
      return;
    }

    appUserShared.EMailAddress = textEMailAddress.getText();
    appUserShared.FirstName = textFirstName.getText();
    appUserShared.LastName = textLastName.getText();
    appUserShared.City = textCity.getText();
    appUserShared.Country = textCountry.getText();

    appUserShared.AuthUsername = textAuthUsername.getText();
    appUserShared.AuthPassword = textAuthPassword.getText();
    appUserShared.AndroidKey = textAndroidAPIKey.getText();

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

  public static void doAddDialog(final AppUserShared adminAppUserShared, final RemoteDataServiceAsync remoteService)
  {
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText("Add New User");

    // Create a table to layout the content
    UserEdit pet = new UserEdit(new UserEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, AppUserShared appUserShared)
      {
        if (ok)
        {
          appUserShared.adminAppUserId = adminAppUserShared.id;

          remoteService.addAppUser(appUserShared, new AsyncCallback<Void>()
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
