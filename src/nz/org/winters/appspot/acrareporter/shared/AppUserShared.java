package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;


public class AppUserShared  implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public Long id;
  public String EMailAddress;
  public String FirstName;
  public String LastName;
  public String City;
  public String Country;
  public String AnalyticsTrackingId;
  
  
  public boolean isSuperDude;
  public boolean isUser;
  public boolean isSubscriptionPaid;
  
  public Counts Totals = new Counts();
  public String AuthString;
  public String AuthUsername;
  public String AuthPassword;
  
  
  public AppUserShared()
  {

  }
  
  
  
}
