package nz.org.winters.appspot.acrareporter.client.ui;

import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;

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
