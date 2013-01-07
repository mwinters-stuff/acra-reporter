package nz.org.winters.appspot.acrareporter.server;

import org.apache.commons.codec.binary.Base64;

public class ServerOnlyUtils
{
  public static String[] decodeAuthString(String authString)
  {
    if (!isEmpty(authString))
    {
      byte[] bytes =  Base64.decodeBase64(authString.getBytes());
      String auth = new String(bytes);
      if (auth.length() > 0)
      {
        String[] auths = auth.split(":");
        if (auths.length == 2)
        {
          return auths;
        }
      }
    }
    return null;
  }
  
  public static String encodeAuthString(String username, String password)
  {
    String auths = username + ":" + password;
    byte[] auth = Base64.encodeBase64(auths.getBytes());
    return new String(auth);
  }
  
  public static boolean isEmpty(String in)
  {
    return in == null || in.isEmpty();
  }
  
}
