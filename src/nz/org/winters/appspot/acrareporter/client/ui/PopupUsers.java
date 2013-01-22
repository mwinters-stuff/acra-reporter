package nz.org.winters.appspot.acrareporter.client.ui;

import nz.org.winters.appspot.acrareporter.client.RemoteDataService;
import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.shared.Configuration;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class PopupUsers extends Composite
{

  private static PopupUsersUiBinder    uiBinder      = GWT.create(PopupUsersUiBinder.class);
  @UiField
  Button                               buttonEdit;
  @UiField
  Button                               buttonNew;
  @UiField
  Button                               buttonDelete;
  private LoginInfo                    mLoginInfo;
  private final RemoteDataServiceAsync remoteService = GWT.create(RemoteDataService.class);

  interface PopupUsersUiBinder extends UiBinder<Widget, PopupUsers>
  {
  }

  public PopupUsers(LoginInfo loginInfo)
  {
    mLoginInfo = loginInfo;
    initWidget(uiBinder.createAndBindUi(this));
    
    buttonNew.setVisible(mLoginInfo.isUserAdmin() && Configuration.appUserMode == Configuration.UserMode.umMultipleSameApps);
    buttonDelete.setVisible(mLoginInfo.isUserAdmin() && (Configuration.appUserMode == Configuration.UserMode.umMultipleSameApps || Configuration.appUserMode == Configuration.UserMode.umMultipleSeperate));

  }

  @UiHandler("buttonEdit")
  void onButtonEditClick(ClickEvent event)
  {
    UserEdit.doEditDialog(mLoginInfo.getAppUserShared(), remoteService);
  }

  @UiHandler("buttonNew")
  void onButtonNewClick(ClickEvent event)
  {
    UserEdit.doAddDialog(mLoginInfo.getAppUserShared(), remoteService);
  }

  @UiHandler("buttonDelete")
  void onButtonDeleteClick(ClickEvent event)
  {
    Window.alert("Not yet implemented!");
  }

  public static void showPopup(LoginInfo loginInfo, Widget source)
  {
    final DecoratedPopupPanel simplePopup = new DecoratedPopupPanel(true);
    simplePopup.setWidget(new PopupUsers(loginInfo));
    int left = source.getAbsoluteLeft() - 10;
    int top = source.getAbsoluteTop() + source.getOffsetHeight() + 10;
    simplePopup.setPopupPosition(left, top);
    simplePopup.show();
  }
}
