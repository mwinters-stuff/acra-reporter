package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;
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

// Serializable version of AppPackage
public class AppPackageShared implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 6338620817023591690L;
  public Long id;
  public String PACKAGE_NAME;
  public String EMailAddress;
  public String EMailSubject;
  public String EMailTemplate;
  
  public String AuthString;
  public String AuthUsername;
  public String AuthPassword;
  public String AppName;
  public Long Owner;
  public Counts Totals;
  
  public AppPackageShared()
  {
    Totals = new Counts();
  }
  
  public AppPackageShared(String apppackage)
  {
    Totals = new Counts();
    this.PACKAGE_NAME = apppackage;
  }

 
}
