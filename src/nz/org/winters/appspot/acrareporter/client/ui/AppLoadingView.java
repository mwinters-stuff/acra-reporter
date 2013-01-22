package nz.org.winters.appspot.acrareporter.client.ui;
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
import nz.org.winters.appspot.acrareporter.client.ui.images.Resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public final class AppLoadingView extends PopupPanel 
{
    private final FlowPanel container = new FlowPanel();
    private UIConstants                   constants     = (UIConstants) GWT.create(UIConstants.class);


    private static AppLoadingView mInstance = null;
    
    public static AppLoadingView getInstance()
    {
      if(mInstance == null)
      {
        mInstance = new AppLoadingView();
      }
      return mInstance;
    }
    
    private AppLoadingView()
    {        
        final Image ajaxImage = new Image(Resources.INSTANCE.loaderImage());
        final Grid grid = new Grid(1, 2);  
        grid.setWidget(0, 0, ajaxImage);
        grid.setText(0, 1, constants.loadingViewLabelLoading());    
        this.container.add(grid);
        add(this.container);       
    }

    @Override
    public Widget asWidget()
    {
        return this;
    }

    
    public void stop()
    {
        hide();
    }

    
    public void start()
    {
        center();
        show();
    }

    
    public void showWidget()
    {
        start();
    }
}
