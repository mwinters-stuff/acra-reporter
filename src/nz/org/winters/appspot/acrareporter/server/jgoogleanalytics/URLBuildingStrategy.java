package nz.org.winters.appspot.acrareporter.server.jgoogleanalytics;

public interface URLBuildingStrategy
{
  public String buildURL(FocusPoint focusPoint);

  public void setRefererURL(String refererURL);
}
