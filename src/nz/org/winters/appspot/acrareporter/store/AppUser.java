package nz.org.winters.appspot.acrareporter.store;

import nz.org.winters.appspot.acrareporter.server.ServerOnlyUtils;
import nz.org.winters.appspot.acrareporter.shared.AppUserShared;
import nz.org.winters.appspot.acrareporter.shared.Counts;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Serialize;
import com.googlecode.objectify.annotation.Unindex;

@Entity
@Index
public class AppUser
{
  @Id public Long id;
  public String EMailAddress;
  public String FirstName;
  public String LastName;
  public String City;
  public String Country;
  public String AnalyticsTrackingId;
  public String AuthString;
  
  
  public boolean isSuperDude = false;
  public boolean isUser = true;
  public boolean isSubscriptionPaid = false;
  
  @Serialize
  @Unindex
  public Counts Totals = new Counts();
  
  
  public AppUser()
  {
  }
  
  public AppUserShared toShared()
  {
    AppUserShared shared= new AppUserShared();
    shared.id = id;
    shared.EMailAddress = EMailAddress;
    shared.FirstName = FirstName;
    shared.LastName = LastName;
    shared.City = City;
    shared.Country = Country;
    shared.AnalyticsTrackingId  = AnalyticsTrackingId;
    shared.isSubscriptionPaid = isSubscriptionPaid;
    shared.isUser = isUser;
    shared.isSuperDude = isSuperDude;
    shared.AuthString = AuthString;
    
    String[] auths = ServerOnlyUtils.decodeAuthString(AuthString);
    if (auths != null)
    {
      shared.AuthUsername = auths[0];
      shared.AuthPassword = auths[1];
    }

    
    shared.Totals.copy(Totals);
    
    return shared;
  }
  
  public void fromShared(AppUserShared shared)
  {
    id = shared.id;
    EMailAddress = shared.EMailAddress;
    FirstName = shared.FirstName;
    LastName = shared.LastName;
    City = shared.City;
    Country = shared.Country;
    AuthString = shared.AuthString;
    AnalyticsTrackingId = shared.AnalyticsTrackingId;
    isSubscriptionPaid = shared.isSubscriptionPaid;
    isUser = shared.isUser;
    isSuperDude = shared.isSuperDude;
    shared.AuthString = ServerOnlyUtils.encodeAuthString(shared.AuthUsername, shared.AuthPassword);
    AuthString = shared.AuthString;
  }
}
