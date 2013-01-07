package nz.org.winters.appspot.acrareporter.store;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnLoad;

@Entity
@Index
public class BasicErrorInfo
{

  public BasicErrorInfo()
  {
    lookedAt = false;
    fixed = false;
    emailed = false;
  };

  @Id
  public Long    id;
  @Index
  public Long    Owner;
  public Date    Timestamp;
  public String  APP_VERSION_NAME;
  public String  PACKAGE_NAME;
  public String  REPORT_ID;
  public String  ANDROID_VERSION;
  public String  USER_CRASH_DATE;

  public Boolean lookedAt;
  public Boolean fixed;
  public Boolean emailed;

  public BasicErrorInfoShared toShared()
  {
    BasicErrorInfoShared shared = new BasicErrorInfoShared();
    shared.id = id;
    shared.Owner = Owner;
    shared.Timestamp = Timestamp;
    shared.APP_VERSION_NAME = APP_VERSION_NAME;
    shared.PACKAGE_NAME = PACKAGE_NAME;
    shared.REPORT_ID = REPORT_ID;
    shared.ANDROID_VERSION = ANDROID_VERSION;
    shared.USER_CRASH_DATE = USER_CRASH_DATE;
    shared.lookedAt = lookedAt;
    shared.fixed = fixed;

    shared.emailed = emailed;
    shared.crashDate = null;

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    try
    {
      if (USER_CRASH_DATE.indexOf(':') > 0)
      {
        String x = USER_CRASH_DATE.replace("T", " ").replace(".000", " ");

        int p = x.lastIndexOf(':');
        String y = x.substring(0, p) + x.substring(p + 1);

        shared.crashDate = df.parse(y);

        shared.formatCrashDate = df2.format(shared.crashDate);
      } else
      {
        shared.formatCrashDate = USER_CRASH_DATE;
      }
    } catch (ParseException e)
    {
      shared.formatCrashDate = USER_CRASH_DATE;
    }

    return shared;
  }

  @OnLoad
  void onLoad()
  {
    if (this.Owner == null)
    {
      this.Owner = 12038L;
    }
  }
}
