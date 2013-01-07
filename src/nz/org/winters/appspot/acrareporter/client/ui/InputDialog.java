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
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class InputDialog extends Composite
{
  public interface DialogCallback
  {
    public void result(boolean ok, String inputValue);
  }

  private static InputDialogUiBinder uiBinder = GWT.create(InputDialogUiBinder.class);
  private static DialogBox           dialogBox;
  @UiField
  TextBox                            textBox;
  @UiField
  Label                              labelInput;
  private DialogCallback             callback;

  interface InputDialogUiBinder extends UiBinder<Widget, InputDialog>
  {
  }

  public InputDialog(String label, String value, DialogCallback callback)
  {
    this.callback = callback;

    initWidget(uiBinder.createAndBindUi(this));

    labelInput.setText(label);
    textBox.setText(value);

  }

  @UiHandler("buttonOK")
  void onButtonOKClick(ClickEvent event)
  {
    dialogBox.hide();
    callback.result(true, textBox.getText());
  }

  @UiHandler("buttonCancel")
  void onButtonCancelClick(ClickEvent event)
  {
    dialogBox.hide();
    callback.result(false, textBox.getText());
  }

  public static void doInput(String caption, String label, String value, DialogCallback callback)
  {
    dialogBox = new DialogBox();
    dialogBox.setText(caption);

    InputDialog input = new InputDialog(label, value, callback);
    input.setWidth("100%");
    dialogBox.setWidget(input);
    dialogBox.center();
    dialogBox.show();

  }
}
