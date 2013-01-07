package nz.org.winters.appspot.acrareporter.server;
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
