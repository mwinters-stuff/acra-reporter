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
import nz.org.winters.appspot.acrareporter.shared.Utils;
import nz.org.winters.appspot.acrareporter.store.AppUser;

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
    public void result(boolean ok, AppUser appUser);
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

  private AppUser           appUser;

  interface UserEditUiBinder extends UiBinder<Widget, UserEdit>
  {
  }

  public UserEdit(AppUser appUser, DialogCallback callback)
  {
    this.callback = callback;
    this.appUser = appUser;
    initWidget(uiBinder.createAndBindUi(this));

    textEMailAddress.setText(appUser.EMailAddress);
    textEMailAddress.setReadOnly(true);

    textFirstName.setText(appUser.FirstName);
    textLastName.setText(appUser.LastName);
    textCity.setText(appUser.City);
    textCountry.setText(appUser.Country);

    textAuthUsername.setText(appUser.AuthUsername);
    textAuthPassword.setText(appUser.AuthPassword);
    textAndroidAPIKey.setText(appUser.AndroidKey);

    textTrackingID.setText(appUser.AnalyticsTrackingId);

  }

  public UserEdit(DialogCallback callback)
  {
    this.callback = callback;
    this.appUser = new AppUser();
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

    appUser.EMailAddress = textEMailAddress.getText();
    appUser.FirstName = textFirstName.getText();
    appUser.LastName = textLastName.getText();
    appUser.City = textCity.getText();
    appUser.Country = textCountry.getText();

    appUser.AuthUsername = textAuthUsername.getText();
    appUser.AuthPassword = textAuthPassword.getText();
    appUser.AndroidKey = textAndroidAPIKey.getText();

    appUser.AnalyticsTrackingId = textTrackingID.getText();

    callback.result(true, appUser);
  }

  @UiHandler("buttonCancel")
  void onButtonCancelClick(ClickEvent event)
  {
    callback.result(false, appUser);
  }

  public static void doEditDialog(AppUser appUser, final RemoteDataServiceAsync remoteService)
  {
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText("Edit User Information");

    // Create a table to layout the content
    UserEdit pet = new UserEdit(appUser, new UserEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, AppUser appUser)
      {
        if (ok)
        {
          remoteService.writeAppUser(appUser, new AsyncCallback<Void>()
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

  public static void doAddDialog(final AppUser adminAppUser, final RemoteDataServiceAsync remoteService)
  {
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText("Add New User");

    // Create a table to layout the content
    UserEdit pet = new UserEdit(new UserEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, AppUser appUser)
      {
        if (ok)
        {
          appUser.adminAppUserId = adminAppUser.id;

          remoteService.addAppUser(appUser, new AsyncCallback<Void>()
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
