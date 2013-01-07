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
import java.util.ArrayList;
import java.util.Comparator;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

public class NameValueList extends Composite
{

  public static class NameValueInfo implements Comparable<NameValueInfo> {

    /**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<NameValueInfo> KEY_PROVIDER = new ProvidesKey<NameValueInfo>() {
      @Override
      public Object getKey(NameValueInfo item) {
        return item == null ? null : item.getName();
      }
    };

    private String name;
    private String value;

    public NameValueInfo(String name, String value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public int compareTo(NameValueInfo o) {
      return (o == null || o.name == null) ? -1 : -o.name.compareTo(name);
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof NameValueInfo) {
        return name.equals(((NameValueInfo) o).name);
      }
      return false;
    }

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }
    
    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }
  private ListHandler<NameValueInfo>             sortHandler;
  private ListDataProvider<NameValueInfo> dataProvider = new ListDataProvider<NameValueInfo>();

  
  private static NameValueListUiBinder uiBinder = GWT.create(NameValueListUiBinder.class);
  @UiField(provided=true) CellTable<NameValueInfo> cellTable = new CellTable<NameValueInfo>(NameValueInfo.KEY_PROVIDER);

  interface NameValueListUiBinder extends UiBinder<Widget, NameValueList>
  {
  }

  public NameValueList()
  {
    initWidget(uiBinder.createAndBindUi(this));
    
    
    sortHandler = new ListHandler<NameValueInfo>(dataProvider.getList());
    cellTable.addColumnSortHandler(sortHandler);
    
    initTableColumns(sortHandler);
    
    dataProvider.setList(new ArrayList<NameValueInfo>());
    sortHandler.setList(dataProvider.getList());
    dataProvider.addDataDisplay(cellTable);

    
  }
  
  private void initTableColumns(ListHandler<NameValueInfo> sortHandler)
  {
    Column<NameValueInfo, String> nameColumn = new Column<NameValueInfo, String>(new TextCell())
    {
      @Override
      public String getValue(NameValueInfo object)
      { // 2012-12-02T18:07:33.000-06:00
        return object.name;
      }
    };
    nameColumn.setSortable(true);
    sortHandler.setComparator(nameColumn, new Comparator<NameValueInfo>()
    {
      @Override
      public int compare(NameValueInfo o1, NameValueInfo o2)
      {
          return o1.name.compareTo(o2.name);
      }
    });
    cellTable.addColumn(nameColumn, "Name");
    cellTable.setColumnWidth(nameColumn, 300, Unit.PX);

    // Value
    Column<NameValueInfo, String> valueColumn = new Column<NameValueInfo, String>(new TextCell())
    {
      @Override
      public String getValue(NameValueInfo object)
      {
        return object.value;
      }
    };
    valueColumn.setSortable(true);
    sortHandler.setComparator(valueColumn, new Comparator<NameValueInfo>()
    {
      @Override
      public int compare(NameValueInfo o1, NameValueInfo o2)
      {
        return o1.value.compareTo(o2.value);
      }
    });
    cellTable.addColumn(valueColumn, "Value");
    cellTable.setColumnWidth(valueColumn, 600, Unit.PX);


  }
  

  public void setData(String data)
  {
    // TODO Auto-generated method stub
    ArrayList<NameValueInfo> nvList = new ArrayList<NameValueInfo>();
    
    String[] lines = data.split("\n");
    for(String line: lines)
    {
      NameValueInfo nvi ;
      String[] pair = line.trim().split("=");
      if(pair.length == 2)
      {
        nvi = new NameValueInfo(pair[0],pair[1]);
      }else
      {
        nvi = new NameValueInfo("NO PAIR " + nvList.size()+1,line.trim());
      }
      nvList.add(nvi);
    }
    
    
    dataProvider.setList(nvList);
    sortHandler.setList(dataProvider.getList());
    
  }

  public void clearData()
  {
    // TODO Auto-generated method stub
    
  }

}
