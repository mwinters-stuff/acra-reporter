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
import java.util.ArrayList;
import java.util.List;

import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.shared.Utils;
import nz.org.winters.appspot.acrareporter.store.ACRALog;
import nz.org.winters.appspot.acrareporter.store.AppPackage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class EMailTemplateSend extends Composite
{
  private static UIConstants constants = (UIConstants) GWT.create(UIConstants.class);

  public interface DialogCallback
  {
    public void result(boolean ok);
  }

  private static EMailTemplateSendUiBinder uiBinder = GWT.create(EMailTemplateSendUiBinder.class);
  @UiField
  TextArea                                 textBody;
  @UiField
  TextArea                                 textEMailAddresses;
  @UiField
  TextBox                                  textSubject;

  private DialogCallback                   mCallback;
  private RemoteDataServiceAsync           remoteService;
  private List<String>                     reportIds;
  private AppPackage                 appPackage;
  private LoginInfo                        loginInfo;

  interface EMailTemplateSendUiBinder extends UiBinder<Widget, EMailTemplateSend>
  {
  }

  public EMailTemplateSend(LoginInfo loginInfo, AppPackage appPackage, List<String> reportIds, RemoteDataServiceAsync remoteService, DialogCallback callback)
  {
    mCallback = callback;
    this.reportIds = reportIds;
    this.remoteService = remoteService;
    this.loginInfo = loginInfo;
    this.appPackage = appPackage;

    initWidget(uiBinder.createAndBindUi(this));

    textSubject.setText(appPackage.EMailSubject);
    textBody.setText(appPackage.EMailTemplate);

    startLoading();
    remoteService.findEMailAddresses(reportIds, new AsyncCallback<String>()
    {

      @Override
      public void onSuccess(String result)
      {
        textEMailAddresses.setText(result);
        stopLoading();

      }

      @Override
      public void onFailure(Throwable caught)
      {
        Window.alert(caught.toString());
        stopLoading();

      }
    });
  }

  public EMailTemplateSend(LoginInfo loginInfo, AppPackage appPackage, ACRALog acraLog, RemoteDataServiceAsync remoteService, DialogCallback callback)
  {
    mCallback = callback;
    this.remoteService = remoteService;
    this.reportIds = new ArrayList<String>();
    this.reportIds.add(acraLog.REPORT_ID);
    this.loginInfo = loginInfo;
    this.appPackage = appPackage;

    initWidget(uiBinder.createAndBindUi(this));

    // populate.
    textSubject.setText(appPackage.EMailSubject);
    textBody.setText(appPackage.EMailTemplate);
    textEMailAddresses.setText(Utils.findEMail(acraLog.USER_EMAIL, acraLog.USER_COMMENT));
  }

  @UiHandler("buttonOK")
  void onButtonOKClick(ClickEvent event)
  {
    // send the e-mail via server..
    startLoading();
    remoteService.sendFixedEMail(loginInfo, reportIds, textEMailAddresses.getText(), textSubject.getText(), textBody.getText(), new AsyncCallback<Void>()
    {

      @Override
      public void onSuccess(Void result)
      {
        stopLoading();
        mCallback.result(true);
      }

      @Override
      public void onFailure(Throwable caught)
      {
        stopLoading();
        Window.alert(caught.toString());
      }
    });
  }

  @UiHandler("buttonCancel")
  void onButtonCancelClick(ClickEvent event)
  {
    mCallback.result(false);
  }

  public static void doDialog(LoginInfo loginInfo, AppPackage appPackage, List<String> reportIds, final RemoteDataServiceAsync remoteService, final DialogCallback callback)
  {
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText(constants.emailLabelSend(appPackage.AppName));

    // Create a table to layout the content
    EMailTemplateSend pet = new EMailTemplateSend(loginInfo, appPackage, reportIds, remoteService, new EMailTemplateSend.DialogCallback()
    {

      @Override
      public void result(boolean ok)
      {
        if (ok)
        {
          dialogBox.hide();
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

  public static void doDialog(LoginInfo loginInfo, AppPackage appPackage, ACRALog acraLog, final RemoteDataServiceAsync remoteService, final DialogCallback callback)
  {
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText(constants.emailLabelSend(appPackage.AppName));

    // Create a table to layout the content
    EMailTemplateSend pet = new EMailTemplateSend(loginInfo, appPackage, acraLog, remoteService, new EMailTemplateSend.DialogCallback()
    {

      @Override
      public void result(boolean ok)
      {
        if (ok)
        {
          dialogBox.hide();
        } else
        {
          dialogBox.hide();
        }

      }
    });

    pet.setWidth("680px");
    dialogBox.setWidget(pet);
    dialogBox.center();
    dialogBox.show();

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
