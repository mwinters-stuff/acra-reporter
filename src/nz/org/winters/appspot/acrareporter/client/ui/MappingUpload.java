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
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.shared.Utils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MappingUpload extends Composite
{
  public interface DialogCallback
  {
    public void result(boolean ok);
  }

  private static UIConstants           constants = (UIConstants) GWT.create(UIConstants.class);
  private static MappingUploadUiBinder uiBinder  = GWT.create(MappingUploadUiBinder.class);
  @UiField(provided = true)
  FormPanel                            form;
  @UiField(provided = true)
  TextBox                              textVersion;
  @UiField(provided = true)
  TextBox                              textPackage;

  TextBox                              textId;
  @UiField(provided = true)
  FileUpload                           fileUpload;
  @UiField
  Button                               buttonOK;
  @UiField
  Button                               buttonCancel;
  @UiField(provided = true)
  VerticalPanel                        vertPanel;
  private DialogCallback               callback;

  interface MappingUploadUiBinder extends UiBinder<Widget, MappingUpload>
  {
  }

  public MappingUpload(final LoginInfo loginInfo, String packageName, final DialogCallback callback)
  {
    this.callback = callback;

    form = new FormPanel();
    textPackage = new TextBox();
    textVersion = new TextBox();
    fileUpload = new FileUpload();
    vertPanel = new VerticalPanel();

    initWidget(uiBinder.createAndBindUi(this));

    textPackage.setText(packageName);

    form.setAction("/mappingupload");
    form.setEncoding(FormPanel.ENCODING_MULTIPART);
    form.setMethod(FormPanel.METHOD_POST);

    textId = new TextBox();
    textId.setVisible(false);
    textId.setName("id");
    vertPanel.add(textId);

    // Add an event handler to the form.
    form.addSubmitHandler(new FormPanel.SubmitHandler()
    {
      public void onSubmit(SubmitEvent event)
      {
        // This event is fired just before the form is submitted. We can take
        // this opportunity to perform validation.
        String fileName = fileUpload.getFilename();
        if (Utils.isEmpty(fileName))
        {
          Window.alert(constants.mappingUploadAlertNofile());
          event.cancel();
          return;
        }
        if (textVersion.getText().length() == 0)
        {
          Window.alert(constants.mappingUploadAlertNoVersion());
          event.cancel();
        }
        textId.setValue(Long.toString(loginInfo.getAppUserShared().id));
        AppLoadingView.getInstance().start();

      }
    });
    form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler()
    {
      public void onSubmitComplete(SubmitCompleteEvent event)
      {
        AppLoadingView.getInstance().stop();
        String result = event.getResults();

        int pos = result.indexOf(";\">");
        if (pos >= 0)
        {
          result = result.substring(pos + 3, result.indexOf("</pre") - 1).trim();
        }

        if (Utils.isEmpty(result) || !result.equalsIgnoreCase("OK"))
        {
          Window.alert(constants.mappingUploadAlertResponse(result));
        } else
          callback.result(true);
      }
    });

  }

  @UiHandler("buttonOK")
  void onButtonOKClick(ClickEvent event)
  {
    String fileName = fileUpload.getFilename();
    if (Utils.isEmpty(fileName))
    {
      // The button isn't active unless we have a file to import
      return;
    }
    if (textVersion.getText().length() == 0)
    {
      Window.alert(constants.mappingUploadAlertNoVersion());
      return;
    }

    form.submit();
    // callback.result(true, null);
  }

  @UiHandler("buttonCancel")
  void onButtonCancelClick(ClickEvent event)
  {
    callback.result(false);
  }

  public static void doEditDialog(LoginInfo loginInfo, String packageName, final DialogCallback callback)
  {

    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText(constants.mappingUploadLabelTitle(packageName));

    // Create a table to layout the content
    MappingUpload pet = new MappingUpload(loginInfo, packageName, new MappingUpload.DialogCallback()
    {

      @Override
      public void result(boolean ok)
      {
        dialogBox.hide();
        callback.result(ok);
      }
    });

    pet.setWidth("100%");
    dialogBox.setWidget(pet);
    dialogBox.center();
    dialogBox.show();

  }

}
