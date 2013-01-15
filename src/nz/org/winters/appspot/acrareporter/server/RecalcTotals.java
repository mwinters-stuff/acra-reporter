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
import nz.org.winters.appspot.acrareporter.store.RegisterDataStores;

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
			
			resp.getWriter().println("User: "+ user.Totals.toString());
			
			List<AppPackage> appPackages = ObjectifyService.ofy().load().type(AppPackage.class).filter("Owner", user.id).list();
			
			for(AppPackage appPackage: appPackages)
			{
				resp.getWriter().println("Package: " + appPackage.PACKAGE_NAME + " = " + appPackage.Totals.toString());
				appPackage.Totals.clear();
				
				List<BasicErrorInfo> basicErrorInfos = ObjectifyService.ofy().load().type(BasicErrorInfo.class).filter("PACKAGE_NAME", appPackage.PACKAGE_NAME).list();
				for(BasicErrorInfo basicErrorInfo: basicErrorInfos)
				{
					user.Totals.incReports();
					appPackage.Totals.incReports();
					
					if(basicErrorInfo.fixed)
					{
						user.Totals.incFixed();
						appPackage.Totals.incFixed();
					}
					if(basicErrorInfo.lookedAt)
					{
						user.Totals.incLookedAt();
						appPackage.Totals.incLookedAt();
					}
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
