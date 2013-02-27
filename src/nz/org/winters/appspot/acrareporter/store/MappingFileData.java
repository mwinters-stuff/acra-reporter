package nz.org.winters.appspot.acrareporter.store;

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

// MappingFile storage.
import com.googlecode.objectify.annotation.EntitySubclass;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;

@EntitySubclass(index=true)
public class MappingFileData extends MappingFileInfo
{
  
  /**
   * 
   */
  private static final long serialVersionUID = -7701266581233307849L;
  @Unindex
  @Serialize(zip=true) public String mapping;

  public MappingFileData(){};
  
  public MappingFileData(AppUser appUser, String apppackage, String version)
  {
    super(appUser,apppackage,version);
  }
  
  public void add(String line)
  {
    mapping = mapping + "\n" + line;
  }


  public String getMapping()
  {
    return mapping;
  }

  public void setMapping(String mapping)
  {
    this.mapping = mapping;
  }
  
}
