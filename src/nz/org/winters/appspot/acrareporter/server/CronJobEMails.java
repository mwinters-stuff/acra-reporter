package nz.org.winters.appspot.acrareporter.server;
import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.AppUser;
import nz.org.winters.appspot.acrareporter.store.DailyCounts;
import nz.org.winters.appspot.acrareporter.store.RegisterDataStores;

public class CronJobEMails extends HttpServlet
{

  /**
   * 
   */
  private static final long serialVersionUID = 461747578656770635L;
  
  
  static
  {
    RegisterDataStores.register();
  }

//  private static final Logger log = Logger.getLogger(CronJobEMails.class.getName());
  
  
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
  {
    resp.setContentType("text/plain");
    
    RemoteDataServiceImpl remote = new RemoteDataServiceImpl();

    // get todays daily counts
    List<DailyCounts> counts = DailyCounts.getAllYesterday();
    
    for(DailyCounts count: counts)
    {
      AppUser user = remote.getAppUser(count.Owner);
      
      if(user.isSubscriptionPaid)
      {
        try
        {
          Properties props = new Properties();
          Session session = Session.getInstance(props, null);
          Message msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress("acra@wintersacrareporter.appspotmail.com", "ACRA Reporter"));
          msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user.EMailAddress, user.FirstName + " " + user.LastName));
          msg.setSubject("ACRA Reports Summary for - " + count.date.toString());

          String bodyText = 
              "Overall: \r\n" +
              "      Reports: " + count.Reports + "\r\n" + 
              "        Fixed: " + count.Fixed + "\r\n" + 
              "    Looked At: " + count.LookedAt + "\r\n" +
              "      Deleted: " + count.Deleted + "\r\n\r\n" +
              "Package Totals\r\n" +
              "Package                              Reports   Fixed     Looked At  Deleted\r\n" +
              "------------------------------------ --------- --------- ---------  ---------\r\n";
          
          StringBuilder sb = new StringBuilder();
          Formatter formatter = new Formatter(sb, Locale.US);
          
          // get each package and list totals.
          List<AppPackage> apppackages = remote.getPackages(count.Owner);
          for(AppPackage apppackage: apppackages)
          {
            formatter.format("%50s %9d %9d %9d %9d\r\n",apppackage.Totals.Reports,apppackage.Totals.Fixed,apppackage.Totals.LookedAt,apppackage.Totals.Deleted);
          }
          bodyText = bodyText + sb.toString();
          

          msg.setText(bodyText);

          Transport.send(msg);
        } catch (Exception e)
        {
          e.printStackTrace();
  //        log.warning("Exception " + e.getMessage());
        }
        
      }
      
    }
    
    // send each to each user.
    
    
    
    
    resp.getWriter().println("DONE");
    
  }
}
