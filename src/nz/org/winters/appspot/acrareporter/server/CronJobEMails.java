package nz.org.winters.appspot.acrareporter.server;

/*
 * Copyright 2013 Mathew Winters

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import com.googlecode.objectify.ObjectifyService;

import nz.org.winters.appspot.acrareporter.shared.Configuration;
import nz.org.winters.appspot.acrareporter.shared.Utils;
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

  // private static final Logger log =
  // Logger.getLogger(CronJobEMails.class.getName());
  public List<AppPackage> getPackages(Long owner)
  {
    return ObjectifyService.ofy().load().type(AppPackage.class).filter("Owner", owner).list();
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
  {
    resp.setContentType("text/plain");

    RemoteDataServiceImpl remote = new RemoteDataServiceImpl();


    int daysBack = 1;
    if (!Utils.isEmpty(req.getParameter("daysback")))
    {
      daysBack = Integer.parseInt(req.getParameter("daysback"));
    }

    // resp.getWriter().println("NUM DAILY COUNTS: " + counts.size());
    // get todays daily counts
    List<DailyCounts> counts = DailyCounts.getAllUsersDaysBack(daysBack);

    for (DailyCounts count : counts)
    {
      // resp.getWriter().println("COUNT: " + count.toString());
      AppUser user = remote.getAppUser(count.Owner);
      // resp.getWriter().println("USER: " + user.toString());

      if (user.isSubscriptionPaid)
      {
        try
        {
          // resp.getWriter().println("SEND MESSAGE");
          List<DailyCounts> packagesYesterday = DailyCounts.getAllUserPackagesDaysBack(user.id,daysBack);

          Properties props = new Properties();
          Session session = Session.getInstance(props, null);
          Message msg = new MimeMessage(session);
          msg.setFrom(new InternetAddress(Configuration.defaultSenderEMailAddress, Configuration.defaultSenderName));
          msg.addRecipient(Message.RecipientType.TO, new InternetAddress(user.EMailAddress, user.FirstName + " " + user.LastName));
          msg.setSubject("ACRA Reports Summary for - " + count.date.toString());

          StringBuilder sb = new StringBuilder();
          Formatter formatter = new Formatter(sb, Locale.US);
          String bodyText;
          try
          {
            sb.append("Overall for " + count.dateString() + ": \r\n" + "      Reports: " + count.Reports + "\r\n" + "        Fixed: " + count.Fixed + "\r\n" + "    Looked At: " + count.LookedAt + "\r\n" + "      Deleted: " + count.Deleted + "\r\n" + "    Not Fixed: " + (count.Reports - count.Fixed) + "\r\n\r\n");

            if(!packagesYesterday.isEmpty())
            {
              String datestr = packagesYesterday.get(0).dateString();
              sb.append(datestr + " Package Totals\r\n" + "Package                                              Reports     Fixed Looked At   Deleted Not Fixed\r\n" + "-------------------------------------------------- --------- --------- ---------  -------- ---------\r\n");
  
              for (DailyCounts dailyPackage : packagesYesterday)
              {
                formatter.format("%-50s %9d %9d %9d %9d %9d\r\n", dailyPackage.PACKAGE_NAME, dailyPackage.Reports, dailyPackage.Fixed, dailyPackage.LookedAt, dailyPackage.Deleted, dailyPackage.Reports - dailyPackage.Fixed);
              }
              // resp.getWriter().println("PACKAGE: " + sb.toString() );
  
              sb.append("\r\n\r\n");
            }
            
            sb.append("Package Totals\r\n" + "Package                                              Reports     Fixed Looked At   Deleted Not Fixed\r\n" + "-------------------------------------------------- --------- --------- --------- --------- ---------\r\n");

            // get each package and list totals.
            List<AppPackage> apppackages = getPackages(count.Owner);
            for (AppPackage apppackage : apppackages)
            {
              formatter.format("%-50s %9d %9d %9d %9d %9d\r\n", apppackage.PACKAGE_NAME, apppackage.Totals.Reports, apppackage.Totals.Fixed, apppackage.Totals.LookedAt, apppackage.Totals.Deleted, apppackage.Totals.Reports - apppackage.Totals.Fixed);
            }
            // resp.getWriter().println("PACKAGE: " + sb.toString() );
            bodyText = sb.toString();
          } finally
          {
            formatter.close();
          }

          MimeBodyPart messageBodyPart = new MimeBodyPart();

          messageBodyPart.setContent("<pre>" + bodyText + "</pre>", "text/html");

          MimeMultipart multipart = new MimeMultipart();

          MimeBodyPart messageBodyPartText = new MimeBodyPart();

          messageBodyPartText.setContent(bodyText, "text/plain");

          multipart.addBodyPart(messageBodyPart);
          multipart.addBodyPart(messageBodyPartText);
          msg.setContent(multipart);

          resp.getWriter().println(bodyText);

          // msg.setText(bodyText);

          Transport.send(msg);
        } catch (Exception e)
        {
          resp.getWriter().println("ERROR: " + e.toString());
          e.printStackTrace();
          // log.warning("Exception " + e.getMessage());
        }

      }

    }

    // send each to each user.

    resp.getWriter().println("DONE");

  }
}
