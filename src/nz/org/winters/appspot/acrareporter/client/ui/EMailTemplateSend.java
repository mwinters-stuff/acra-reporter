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
