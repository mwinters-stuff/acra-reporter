package nz.org.winters.appspot.acrareporter.client.ui;

import nz.org.winters.appspot.acrareporter.client.ui.images.Resources;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public final class AppLoadingView extends PopupPanel 
{
    private final FlowPanel container = new FlowPanel();

    public AppLoadingView()
    {        
        final Image ajaxImage = new Image(Resources.INSTANCE.loaderImage());
        final Grid grid = new Grid(1, 2);  
        grid.setWidget(0, 0, ajaxImage);
        grid.setText(0, 1, "Loading...");    
        this.container.add(grid);
        add(this.container);       
    }

    @Override
    public Widget asWidget()
    {
        return this;
    }

    
    public void stopProcessing()
    {
        hide();
    }

    
    public void startProcessing()
    {
        center();
        show();
    }

    
    public void showWidget()
    {
        startProcessing();
    }
}
