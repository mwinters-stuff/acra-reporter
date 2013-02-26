package nz.org.winters.appspot.acrareporter.server;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.org.winters.appspot.acrareporter.shared.Utils;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.AppUser;
import nz.org.winters.appspot.acrareporter.store.BasicErrorInfo;
import nz.org.winters.appspot.acrareporter.store.DailyCounts;
import nz.org.winters.appspot.acrareporter.store.MappingFile;

import com.googlecode.objectify.ObjectifyService;

public class RecalcTotals extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7124147168269817368L;
	static {
		RegisterDataStores.register();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");

	
		try {
			String useremail = req.getParameter("user");
			if(Utils.isEmpty(useremail))
			{
				resp.getWriter().println("ERROR: no user");
				return;
			}
			
			AppUser user = ObjectifyService.ofy().load().type(AppUser.class).filter("EMailAddress", useremail).first().get();
			if(user == null)
			{
				resp.getWriter().println("ERROR: User invalid");
				return;
			}

			
			
			
			user.Totals.clear();
			
			List<DailyCounts> dc = ObjectifyService.ofy().load().type(DailyCounts.class).filter("Owner", user.id).list();
			ObjectifyService.ofy().delete().entities(dc);
			dc.clear();
			
			resp.getWriter().println("User: "+ user.Totals.toString());
			
			List<AppPackage> appPackages = ObjectifyService.ofy().load().type(AppPackage.class).filter("Owner", user.id).list();
			
			for(AppPackage appPackage: appPackages)
			{
	      List<MappingFile> maps = ObjectifyService.ofy().load().type(MappingFile.class).filter("apppackage",appPackage.PACKAGE_NAME).list();
	      ObjectifyService.ofy().save().entities(maps);

	      resp.getWriter().println("Package: " + appPackage.PACKAGE_NAME + " = " + appPackage.Totals.toString());
				appPackage.Totals.clear();

				dc = ObjectifyService.ofy().load().type(DailyCounts.class).filter("PACKAGE_NAME", appPackage.PACKAGE_NAME).list();
				ObjectifyService.ofy().delete().entities(dc);
				dc.clear();
        
				DailyCounts packageDaily = null;
        DailyCounts userDaily = null;
				
				List<BasicErrorInfo> basicErrorInfos = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", appPackage.PACKAGE_NAME).order("Timestamp").list();
				for(BasicErrorInfo basicErrorInfo: basicErrorInfos)
				{
				  DailyCountsGetters.getDate(user.id, basicErrorInfo.Timestamp);
					if(userDaily == null || packageDaily == null || !packageDaily.date.equals(DailyCountsGetters.removeTimeFromDate(basicErrorInfo.Timestamp)) || !userDaily.date.equals(DailyCountsGetters.removeTimeFromDate(basicErrorInfo.Timestamp)))
					{
					  if(userDaily != null)
					  {
					    ObjectifyService.ofy().save().entity(userDaily);
					  }
					  if(packageDaily != null)
					  {
					    ObjectifyService.ofy().save().entity(packageDaily);
					  }
	          userDaily = DailyCountsGetters.getDate(user.id, basicErrorInfo.Timestamp);
	          packageDaily = DailyCountsGetters.getDate(basicErrorInfo.PACKAGE_NAME, basicErrorInfo.Timestamp);
					  
					}
					  
					  
					
					userDaily.incReports();
					packageDaily.incReports();
					
					user.Totals.incReports();
					appPackage.Totals.incReports();
					
					if(basicErrorInfo.fixed)
					{
						userDaily.incFixed();
						packageDaily.incFixed();
						user.Totals.incFixed();
						appPackage.Totals.incFixed();
					}
					if(basicErrorInfo.fixed && !basicErrorInfo.lookedAt)
					{
						basicErrorInfo.lookedAt = true;
						ObjectifyService.ofy().save().entity(basicErrorInfo);
					}
					if(basicErrorInfo.lookedAt)
					{
						userDaily.incLookedAt();
						packageDaily.incLookedAt();
						user.Totals.incLookedAt();
						appPackage.Totals.incLookedAt();
					}
				}
        if(userDaily != null)
        {
          ObjectifyService.ofy().save().entity(userDaily);
        }
        if(packageDaily != null)
        {
          ObjectifyService.ofy().save().entity(packageDaily);
        }
				
				resp.getWriter().println("Package: " + appPackage.PACKAGE_NAME + " = " + appPackage.Totals.toString());
				
			}
			ObjectifyService.ofy().save().entities(appPackages);
			ObjectifyService.ofy().save().entity(user);
			resp.getWriter().println("User: "+ user.Totals.toString());

		} catch (Exception e) {
			resp.getWriter().println("ERROR: " + e.getMessage());
		}

		resp.getWriter().println("DONE");
	}
}
