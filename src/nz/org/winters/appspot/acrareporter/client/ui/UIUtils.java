package nz.org.winters.appspot.acrareporter.client.ui;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;

public class UIUtils
{
  static public String reportDateToLocal(String in)
  {
    Date date = reportDateToDate(in);
    if(date != null)
    {
      return DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM).format(date);// df2.format(date);
    } else
    {
      return in;
    }
  }
  
  static public Date reportDateToDate(String in)
  {
    try
    {
      if (in.indexOf(':') > 0)
      {
        String x = in.replace("T", " ").replace(".000", " ");

        int p = x.lastIndexOf(':');
        String y = x.substring(0, p) + x.substring(p + 1);

        return DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss Z").parse(y);
      } else
      {
        return null;
      }
    } catch (Exception e)
    {
      return null;
    }
  }
}
