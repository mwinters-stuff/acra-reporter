package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

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


public class Counts implements Serializable, IsSerializable
{

  /**
   * 
   */
  private static final long serialVersionUID = 7132720948917830809L;

  public int Reports = 0;
  public int Fixed = 0;
  public int LookedAt = 0;
  public int Deleted = 0;
  
  public Counts()
  {
        
  }
  
  public Counts(Counts other)
  {
    copy(other);
  }

  public int incReports()
  {
    return ++Reports;
  }
  
  public int incFixed()
  {
    return ++Fixed;
  }
  
  public int incLookedAt()
  {
    return ++LookedAt;
  }
  
  public int incDeleted()
  {
    return ++Deleted;
  }

  public int decReports()
  {
    return --Reports;
  }
  
  public int decFixed()
  {
    return --Fixed;
  }
  
  public int decLookedAt()
  {
    return --LookedAt;
  }

  public int decDeleted()
  {
    return --Deleted;
  }

  public void copy(Counts other)
  {
    Reports = other.Reports;
    Fixed = other.Fixed;
    LookedAt = other.LookedAt;    
    Deleted = other.Deleted;
  }

  public String toLabelString()
  {
    return "Reports: " + Reports + ", Fixed: " + Fixed + ", Looked At: " + LookedAt + ", Deleted: " + Deleted;
  }
        
  

}
