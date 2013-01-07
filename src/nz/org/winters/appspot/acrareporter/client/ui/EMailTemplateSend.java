package nz.org.winters.appspot.acrareporter.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.TextBox;

public class EMailTemplateSend extends Composite
{

  private static EMailTemplateSendUiBinder uiBinder = GWT.create(EMailTemplateSendUiBinder.class);
  @UiField TextArea textBody;
  @UiField TextArea textEMailAddresses;
  @UiField TextBox textSubject;

  interface EMailTemplateSendUiBinder extends UiBinder<Widget, EMailTemplateSend>
  {
  }

  public EMailTemplateSend()
  {
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("buttonOK")
  void onButtonOKClick(ClickEvent event) {
  }
  @UiHandler("buttonCancel")
  void onButtonCancelClick(ClickEvent event) {
  }
}
