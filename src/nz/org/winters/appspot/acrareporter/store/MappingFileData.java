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
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;

@Entity
public class MappingFileData
{

  /**
   * 
   */
//  private static final long serialVersionUID = -7701266581233307849L;
  @Id
  public Long               id;

  @Index
  public Long               mappingFileInfoId;

  @Unindex
  @Serialize(zip = true)
  public String             mapping;

  public MappingFileData()
  {
  };

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
