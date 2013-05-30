package nz.org.winters.appspot.acrareporter.shared;

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

// helper class.
public class Utils
{
 
//  private String escapeHtml(String html)
//  {
//    if (html == null)
//    {
//      return null;
//    }
//    return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
//  }
//  
  public static boolean isEmpty(String in)
  {
    return in == null || in.trim().isEmpty();
  }
  
  
  public static String findEMail(String email, String comment)
  {
    
    if(!Utils.isEmpty(email))
    {
      return email;
    }else if(!Utils.isEmpty(comment))
    {
      String[] commentLines = comment.split("\n");
      for(String line: commentLines)
      {
        if(line.contains("@"))
        {
          return line;
        }
      }
    }
    return "";
  }
}
