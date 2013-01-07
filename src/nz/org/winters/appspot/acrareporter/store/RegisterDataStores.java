package nz.org.winters.appspot.acrareporter.store;

import com.googlecode.objectify.ObjectifyService;

public class RegisterDataStores
{
  public static void register()
  {
    ObjectifyService.register(ACRALog.class);
    ObjectifyService.register(AppPackage.class);
    ObjectifyService.register(AppUser.class);
    ObjectifyService.register(BasicErrorInfo.class);
    ObjectifyService.register(DailyCounts.class);
    ObjectifyService.register(MappingFile.class);

  }
}
