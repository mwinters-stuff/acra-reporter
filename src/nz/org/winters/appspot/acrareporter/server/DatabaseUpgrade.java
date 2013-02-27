package nz.org.winters.appspot.acrareporter.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.org.winters.appspot.acrareporter.shared.Utils;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.MappingFile;
import nz.org.winters.appspot.acrareporter.store.MappingFileData;

import com.google.appengine.api.datastore.QueryResultIterator;
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
      int dbversion = Integer.parseInt(SettingStore.get("databaseVersion", "1"));

      if (dbversion < 2)
      {

        List<Long> deleteids = new ArrayList<Long>();
        List<AppPackage> appPackages = ObjectifyService.ofy().load().type(AppPackage.class).list();

        for (AppPackage appPackage : appPackages)
        {
          resp.getWriter().println("Package: " + appPackage.PACKAGE_NAME);
          // int offset = 5;

          Query<MappingFile> mapsquery = ObjectifyService.ofy().load().type(MappingFile.class).filter("apppackage", appPackage.PACKAGE_NAME);
          QueryResultIterator<MappingFile> iterator = mapsquery.iterator();
          while (iterator.hasNext())
          {
            MappingFile map = iterator.next();

            MappingFileData mfd = new MappingFileData();
            mfd.Owner = map.Owner;
            mfd.version = map.version;
            mfd.PACKAGE_NAME = map.getApppackage();
            mfd.uploadDate = map.uploadDate;
            mfd.mapping = map.mapping;
            ObjectifyService.ofy().save().entity(mfd);
            
            deleteids.add(map.id);
          } 
          ObjectifyService.ofy().delete().type(MappingFile.class).ids(deleteids);
          deleteids.clear();
        }
      }
      
      SettingStore.put("databaseVersion", "2");

    } catch (Exception e)
    {
      resp.getWriter().println("ERROR: " + e.getMessage());
    }

    resp.getWriter().println("DONE");
  }
}
