package nz.org.winters.appspot.acrareporter.client.ui;

import nz.org.winters.appspot.acrareporter.client.ui.FrontPage.Callback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ClickEvent;

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
