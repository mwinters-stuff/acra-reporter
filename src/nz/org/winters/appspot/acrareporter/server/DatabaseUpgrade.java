package nz.org.winters.appspot.acrareporter.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.MappingFile;
import nz.org.winters.appspot.acrareporter.store.MappingFileData;
import nz.org.winters.appspot.acrareporter.store.MappingFileInfo;

import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;

public class DatabaseUpgrade extends HttpServlet
{

  /**
	 * 
	 */
  private static final long serialVersionUID = -7124147168269817368L;
  static
  {
    RegisterDataStores.register();
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
  {
    resp.setContentType("text/plain");

    try
    {
      int dbversion = Integer.parseInt(SettingStore.get(Constants.SETTING_DATABASEVERSION, "1"));
      if (dbversion == Constants.databaseVersion)
      {
        resp.getWriter().println("Database already up-to-date");
        return;
      }

      if (dbversion < 2)
      {

        List<Long> deleteids = new ArrayList<Long>();
        List<AppPackage> appPackages = ObjectifyService.ofy().load().type(AppPackage.class).list();

        for (AppPackage appPackage : appPackages)
        {
          Query<MappingFile> mapsquery = ObjectifyService.ofy().load().type(MappingFile.class).filter("apppackage", appPackage.PACKAGE_NAME).limit(5);
          QueryResultIterator<MappingFile> iterator = mapsquery.iterator();
          while (iterator.hasNext())
          {
            while (iterator.hasNext())
            {
              MappingFile map = iterator.next();

              MappingFileInfo mfi = new MappingFileInfo();
              mfi.Owner = map.Owner;
              mfi.version = map.version;
              mfi.PACKAGE_NAME = map.getApppackage();
              mfi.uploadDate = map.uploadDate;
              
              Key<MappingFileInfo> resultkey = ObjectifyService.ofy().save().entity(mfi).now();
              
              MappingFileData mfd = new MappingFileData();
              mfd.add(map.mapping);
              mfd.mappingFileInfoId = resultkey.getId();
              ObjectifyService.ofy().save().entity(mfd);

              deleteids.add(map.id);
            }
            ObjectifyService.ofy().delete().type(MappingFile.class).ids(deleteids);
            resp.getWriter().println("Package: " + appPackage.PACKAGE_NAME + " converted " + deleteids.size() + " MappingFile records.");
            deleteids.clear();

            mapsquery = ObjectifyService.ofy().load().type(MappingFile.class).filter("apppackage", appPackage.PACKAGE_NAME).limit(5);
            iterator = mapsquery.iterator();
          }

        }
      }

      SettingStore.put(Constants.SETTING_DATABASEVERSION, Integer.toString(Constants.databaseVersion));

    } catch (Exception e)
    {
      resp.getWriter().println("ERROR: " + e.getMessage());
    }

    resp.getWriter().println("DONE");
  }
}
