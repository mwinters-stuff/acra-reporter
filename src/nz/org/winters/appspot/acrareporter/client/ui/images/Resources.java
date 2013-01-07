package nz.org.winters.appspot.acrareporter.client.ui.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface Resources extends ClientBundle
{
  public static final Resources INSTANCE =  GWT.create(Resources.class);
  
  @Source("Blue-arrow-left-32.png")
  public ImageResource leftArrow();
  @Source("Blue-arrow-right-32.png")
  public ImageResource rightArrow();
  
  @Source("ajax-loader.gif")
  public ImageResource loaderImage();
//  @Source("left_arrow.png")
//  public ImageResource leftArrow();
//  @Source("right_arrow.png")
//  public ImageResource rightArrow();
}
