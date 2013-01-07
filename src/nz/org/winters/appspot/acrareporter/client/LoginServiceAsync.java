package nz.org.winters.appspot.acrareporter.client;

import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoginServiceAsync {

	void login(String requestUri, AsyncCallback<LoginInfo> callback);

}
