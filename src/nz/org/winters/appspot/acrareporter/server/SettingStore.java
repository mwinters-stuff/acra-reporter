package nz.org.winters.appspot.acrareporter.server;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class SettingStore
{
  @Id
  public Long id;
  @Index
  public String name;
  public String value;
  
  
  public static String get(String name, String defaultvalue)
  {
    SettingStore setting= ObjectifyService.ofy().load().type(SettingStore.class).filter("name",name).first().get();
    if(setting == null)
    {
      setting= new SettingStore();
      setting.name = name;
      setting.value = defaultvalue;
      setting.save();
    }
    return setting.value;
  }
  
  public static void put(String name, String value)
  {
    SettingStore setting= ObjectifyService.ofy().load().type(SettingStore.class).filter("name",name).first().get();
    if(setting == null)
    {
      setting= new SettingStore();
      setting.name = name;
    }
    setting.value = value;
    setting.save();
  }

  public void save()
  {
    ObjectifyService.ofy().save().entity(this);
  }

}
