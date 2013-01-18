package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PackageGraphData implements Serializable, IsSerializable
{

  /**
   * 
   */
  private static final long serialVersionUID = 5702485653608190346L;

  public PackageGraphData(){};
  
  public PackageGraphData(AppPackageShared shared)
  {
    this.id = shared.id;
    this.AppName = shared.AppName;
    this.PACKAGE_NAME = shared.PACKAGE_NAME;
    this.counts = new Counts(shared.Totals);
  }
  
  public Long id;
  public String AppName;
  public String PACKAGE_NAME;
  public Counts counts;
  
}
