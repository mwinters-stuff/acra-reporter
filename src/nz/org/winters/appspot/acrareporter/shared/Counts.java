package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;

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
