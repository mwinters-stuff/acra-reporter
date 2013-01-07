package nz.org.winters.appspot.acrareporter.client;

import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
	  public LoginInfo login(String requestUri);
}
