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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class FrontPage extends Composite
{

  public interface Callback
  {
    void buttonLogin();
    void buttonWiki();
    void buttonSignUp();
  }
  private static FrontPageUiBinder uiBinder = GWT.create(FrontPageUiBinder.class);
  @UiField Button buttonLogin;
  @UiField Button buttonWiki;
  @UiField Button buttonSignUp;
  private Callback callback;

  interface FrontPageUiBinder extends UiBinder<Widget, FrontPage>
  {
  }

  public FrontPage(Callback callback)
  {
    this.callback = callback;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("buttonLogin")
  void onButtonLoginClick(ClickEvent event) {
    callback.buttonLogin();
  }
  @UiHandler("buttonWiki")
  void onButtonWikiClick(ClickEvent event) {
    callback.buttonWiki();
  }
  @UiHandler("buttonSignUp")
  void onButtonSignUpClick(ClickEvent event) {
    callback.buttonSignUp();
  }
}
