package nz.org.winters.appspot.acrareporter.client.ui;

import java.util.Comparator;
import java.util.List;

import nz.org.winters.appspot.acrareporter.client.RemoteDataService;
import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.DailyCountsShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;

import com.google.gwt.ajaxloader.client.Properties;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.Selection;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.events.ReadyHandler;
import com.google.gwt.visualization.client.events.SelectHandler;
import com.google.gwt.visualization.client.formatters.DateFormat;
import com.google.gwt.visualization.client.visualizations.Table;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;

public class Overview extends Composite implements ChangeHandler
{

  private static OverviewUiBinder                   uiBinder       = GWT.create(OverviewUiBinder.class);
  @UiField
  HorizontalPanel                                   topPanel;
  @UiField
  VerticalPanel                                     mainPanel;
  @UiField
  HorizontalPanel                                   midPanel;
  @UiField
  ListBox                                           totalsDataSelection;
  @UiField
  SimplePanel                                       pieHolder;
  @UiField
  HorizontalPanel                                   bottomPanel;
  @UiField
  VerticalPanel                                     panelAppGrid;
  @UiField(provided = true)
  DataGrid<AppPackageShared>                        appTotalsTable = new DataGrid<AppPackageShared>();

  private final RemoteDataServiceAsync              remoteService  = GWT.create(RemoteDataService.class);

  private LoginInfo                                 loginInfo;

  private DataTable                                 mTotalGraphData;
  private PieChart                                  mPackageTotalsGraph;
  private LineChart                                 mTotalsMonthGraph;
  private LineChart                                 mPackageMonthGraph;

  private DataTable                                 mTotalsMonthGraphData;
  private DataTable                                 mPackageMonthGraphData;
  // private Table mPackageTable;
  // private DataTable mPackageTableData;
  private List<AppPackageShared>                    mAppPackageShared;
  private DateFormat                                mShortDateFormat;

  private int                                       browserWidth;
  private int                                       browserHeight;

  public static final ProvidesKey<AppPackageShared> KEY_PROVIDER   = new ProvidesKey<AppPackageShared>()
                                                                   {
                                                                     @Override
                                                                     public Object getKey(AppPackageShared item)
                                                                     {
                                                                       return item == null ? null : item.id;
                                                                     }
                                                                   };

  interface OverviewUiBinder extends UiBinder<Widget, Overview>
  {
  }

  public Overview(LoginInfo loginInfo)
  {
    this.loginInfo = loginInfo;
    initWidget(uiBinder.createAndBindUi(this));

    browserWidth = 1200;
    browserHeight = 540;
    if (Window.getClientWidth() > browserWidth)
    {
      browserWidth = Window.getClientWidth() - 50;
    }
    if (Window.getClientHeight() > browserHeight)
    {
      browserHeight = Window.getClientHeight() - 50;
    }

    createPackageTableColumns();
    totalsDataSelection.setEnabled(false);

    totalsDataSelection.addItem("Reports");
    totalsDataSelection.addItem("Fixed");
    totalsDataSelection.addItem("Looked At");
    totalsDataSelection.addItem("Not Fixed");
    totalsDataSelection.addItem("New");

    totalsDataSelection.addChangeHandler(this);

    Runnable onLoadCallback = new Runnable()
    {
      public void run()
      {
        DateFormat.Options dfo = DateFormat.Options.create();
        dfo.setPattern(DateFormat.FormatType.SHORT);
        mShortDateFormat = DateFormat.create(dfo);
        // Panel panel = RootPanel.get();

        mPackageTotalsGraph = new PieChart(createTotalReportTable(), createTotalReportOptions());
        pieHolder.add(mPackageTotalsGraph);
        //mPackageTotalsGraph.addSelectHandler(mTotalsSelectHandler);

        mTotalsMonthGraph = new LineChart(createTotalsMonthTable(), createTotalsMonthOptions());
        topPanel.add(mTotalsMonthGraph);

        // mPackageTable = new Table(createPackageTableTable(),
        // createPackageTableOptions());
        // midPanel.add(mPackageTable);
        // mPackageTable.addSelectHandler(mPackageTableSelectHandler);

        mPackageMonthGraph = new LineChart(createPackageMonthTable(), createPackageMonthOptions(null));
        midPanel.add(mPackageMonthGraph);

        mPackageTotalsGraph.addReadyHandler(new ReadyHandler()
        {

          @Override
          public void onReady(ReadyEvent event)
          {
            totalsDataSelection.setEnabled(true);

          }

        });

        updateTotalsGraph();
        updateTotalsMonthGraph();

      }

    };

    VisualizationUtils.loadVisualizationApi(onLoadCallback, CoreChart.PACKAGE, Table.PACKAGE);

  }

  // report pie chart
  private Options createTotalReportOptions()
  {
    PieOptions options = PieOptions.create();
    options.setWidth((int) ((double) browserWidth * 0.4));
    options.setHeight((int) ((double) browserHeight * 0.4));
    options.set3D(true);
    options.setPieSliceText("value");
    switch (totalsDataSelection.getSelectedIndex())
    {
      case 0: // reports
        options.setTitle("Total Reports per App");
        break;
      case 1: // fixed
        options.setTitle("Total Fixed per App");
        break;
      case 2: // looked at
        options.setTitle("Total Looked At per App");
        break;
      case 3: // not fixed
        options.setTitle("Total Not Fixed per App");
        break;
      case 4: // new
        options.setTitle("Total New per App");
        break;
    }
    Properties animation = Properties.create();
    animation.set("duration", 1000.0);
    animation.set("easing", "out");
    options.set("animation", animation);

    return options;
  }

  // month totals bar chart.

  private Options createTotalsMonthOptions()
  {
    Options options = Options.create();
    options.setWidth((int) ((double) browserWidth * 0.6));
    options.setHeight((int) ((double) browserHeight * 0.4));

    options.setTitle("Total reports per day");

    return options;
  }

  // options for package table
  // private Table.Options createPackageTableOptions()
  // {
  // Table.Options options = Table.Options.create();
  // options.setWidth((int) ((double) browserWidth * 0.5) + "px");
  // options.setHeight("300px");
  // options.setSortAscending(false);
  // options.setSortColumn(1);
  // options.setAllowHtml(true);
  // return options;
  // }

  // package month line chart options.
  private Options createPackageMonthOptions(String appName)
  {
    Options options = Options.create();
    options.setWidth((int) ((double) browserWidth * 0.5));
    options.setHeight(300);
    if (appName != null)
    {

      options.setTitle(appName + " - Month to today");
    } else
    {
      options.setTitle("App Month");

    }

    Properties animation = Properties.create();
    animation.set("duration", 1000.0);
    animation.set("easing", "out");
    options.set("animation", animation);

    return options;
  }

  private AbstractDataTable createTotalReportTable()
  {
    mTotalGraphData = DataTable.create();
    mTotalGraphData.addColumn(ColumnType.STRING, "App");
    mTotalGraphData.addColumn(ColumnType.NUMBER, "Reports");

    return mTotalGraphData;
  }

  // month totals bar chart.

  private AbstractDataTable createTotalsMonthTable()
  {
    mTotalsMonthGraphData = DataTable.create();
    mTotalsMonthGraphData.addColumn(ColumnType.DATE, "Date");
    mTotalsMonthGraphData.addColumn(ColumnType.NUMBER, "Reports");
    mTotalsMonthGraphData.addColumn(ColumnType.NUMBER, "Fixed");

    return mTotalsMonthGraphData;
  }

  // private AbstractDataTable createPackageTableTable()
  // {
  // mPackageTableData = DataTable.create();
  // mPackageTableData.addColumn(ColumnType.STRING, "App");
  // mPackageTableData.addColumn(ColumnType.NUMBER, "New");
  // mPackageTableData.addColumn(ColumnType.NUMBER, "Not Fixed");
  // mPackageTableData.addColumn(ColumnType.NUMBER, "Looked At");
  // mPackageTableData.addColumn(ColumnType.NUMBER, "Fixed");
  // mPackageTableData.addColumn(ColumnType.NUMBER, "Reports");
  // mPackageTableData.addColumn(ColumnType.STRING, "Actions");
  // return mPackageTableData;
  // }

  private AbstractDataTable createPackageMonthTable()
  {
    mPackageMonthGraphData = DataTable.create();
    mPackageMonthGraphData.addColumn(ColumnType.DATE, "Date");

    mPackageMonthGraphData.addColumn(ColumnType.NUMBER, "Reports");
    mPackageMonthGraphData.addColumn(ColumnType.NUMBER, "Fixed");
    mPackageMonthGraphData.addColumn(ColumnType.NUMBER, "Looked At");
    mPackageMonthGraphData.addColumn(ColumnType.NUMBER, "Not Fixed");
    mPackageMonthGraphData.addColumn(ColumnType.NUMBER, "New");
    return mPackageMonthGraphData;
  }

  // month totals bar chart.

  private void updateTotalsGraph()
  {
    remoteService.getPackageGraphDataTotals(loginInfo, new AsyncCallback<List<AppPackageShared>>()
    {

      @Override
      public void onSuccess(List<AppPackageShared> result)
      {
        mAppPackageShared = result;
        loadTotalsGraphData();
      }

      @Override
      public void onFailure(Throwable caught)
      {
        Window.alert(caught.getMessage());

      }
    });

  }

  private void updateTotalsMonthGraph()
  {
    remoteService.getLastMonthDailyCounts(loginInfo, new AsyncCallback<List<DailyCountsShared>>()
    {

      @Override
      public void onFailure(Throwable caught)
      {
        Window.alert(caught.getMessage());

      }

      @Override
      public void onSuccess(List<DailyCountsShared> result)
      {

        mTotalsMonthGraphData.removeRows(0, mTotalsMonthGraphData.getNumberOfRows());

        mTotalsMonthGraphData.addRows(result.size());
        for (int i = 0; i < result.size(); i++)
        {
          DailyCountsShared data = result.get(i);
          mTotalsMonthGraphData.setValue(i, 0, data.date);
          mTotalsMonthGraphData.setValue(i, 1, data.Reports);
          mTotalsMonthGraphData.setValue(i, 2, data.Fixed);
        }
        mShortDateFormat.format(mTotalsMonthGraphData, 0);

        mTotalsMonthGraph.draw(mTotalsMonthGraphData, createTotalsMonthOptions());

      }
    });

  }

  private void updateAppMonthGraph(final String packageName)
  {

    remoteService.getPackageLastMonthDailyCounts(loginInfo, packageName, new AsyncCallback<List<DailyCountsShared>>()
    {

      @Override
      public void onSuccess(List<DailyCountsShared> result)
      {

        mPackageMonthGraphData.removeRows(0, mPackageMonthGraphData.getNumberOfRows());
        if (result.size() > 0)
        {
          mPackageMonthGraphData.addRows(result.size());
          for (int i = 0; i < result.size(); i++)
          {
            DailyCountsShared data = result.get(i);
            mPackageMonthGraphData.setValue(i, 0, data.date);
            mPackageMonthGraphData.setValue(i, 1, data.Reports);
            mPackageMonthGraphData.setValue(i, 2, data.Fixed);
            mPackageMonthGraphData.setValue(i, 3, data.LookedAt);
            mPackageMonthGraphData.setValue(i, 4, data.Reports - data.Fixed);
            mPackageMonthGraphData.setValue(i, 5, data.Reports - data.LookedAt);
          }
          mShortDateFormat.format(mPackageMonthGraphData, 0);

        }
        mPackageMonthGraph.draw(mPackageMonthGraphData, createPackageMonthOptions(getAppName(packageName)));

      }

      @Override
      public void onFailure(Throwable caught)
      {
        Window.alert(caught.getMessage());
      }
    });
  }

  protected String getAppName(String packageName)
  {
    List<AppPackageShared> shared = mPackageTableDataProvider.getList();
    for(AppPackageShared appPackage: shared)
    {
      if(appPackage.PACKAGE_NAME.equals(packageName))
      {
        return appPackage.AppName;
      }
    }
    return "";
  }

//  SelectHandler                              mTotalsSelectHandler = new SelectHandler()
//                                                                  {
//
//                                                                    @Override
//                                                                    public void onSelect(SelectEvent event)
//                                                                    {
//                                                                      JsArray<Selection> selected = mPackageTotalsGraph.getSelections();
//
//                                                                      if (selected.length() == 0)
//                                                                        return;
//
//                                                                      String packageName = mAppPackageShared.get(selected.get(0).getRow()).PACKAGE_NAME;
//
//                                                                      // mPackageTable.setSelections(selected);
//                                                                      updateAppMonthGraph(packageName);
//                                                                    }
//
//                                                                  };
  private ListDataProvider<AppPackageShared> mPackageTableDataProvider;

  // SelectHandler mPackageTableSelectHandler = new SelectHandler()
  // {
  //
  // @Override
  // public void onSelect(SelectEvent event)
  // {
  // JsArray<Selection> selected = mPackageTable.getSelections();
  // Selection sel = selected.get(0);
  // if(sel != null)
  // {
  // int row = sel.getRow();
  // Selection news = Selection.createRowSelection(row);
  //
  // JsArray<Selection> pieselect = Selection.createArray().cast();
  // pieselect.push(news);
  // mPackageTotalsGraph.setSelections(pieselect);
  // updateAppMonthGraph(selected);
  // }
  // }
  //
  // };

  @Override
  public void onChange(ChangeEvent event)
  {
    loadTotalsGraphData();

  }

  void loadTotalsGraphData()
  {
    mTotalGraphData.removeRows(0, mTotalGraphData.getNumberOfRows());
    // mPackageTableData.removeRows(0, mPackageTableData.getNumberOfRows());

    mTotalGraphData.addRows(mAppPackageShared.size());
    // mPackageTableData.addRows(mAppPackageShared.size());
    int lastnewc = 0;
    int selectRow = 0;

    List<AppPackageShared> tableList = mPackageTableDataProvider.getList();
    tableList.clear();
    for (int i = 0; i < mAppPackageShared.size(); i++)
    {
      AppPackageShared data = mAppPackageShared.get(i);
      tableList.add(data);

      mTotalGraphData.setValue(i, 0, data.AppName);

      int newc = data.Totals.Reports - data.Totals.LookedAt;
      switch (totalsDataSelection.getSelectedIndex())
      {
        case 0: // reports
          mTotalGraphData.setValue(i, 1, data.Totals.Reports);
          break;
        case 1: // fixed
          mTotalGraphData.setValue(i, 1, data.Totals.Fixed);
          break;
        case 2: // looked at
          mTotalGraphData.setValue(i, 1, data.Totals.LookedAt);
          break;
        case 3: // not fixed
          mTotalGraphData.setValue(i, 1, data.Totals.Reports - data.Totals.Fixed);
          break;
        case 4: // new
          mTotalGraphData.setValue(i, 1, newc);
          break;
      }

      // mTotalGraphData.setProperty(i,0,"DATA", Long.toString(data.id));
      if (newc > lastnewc)
      {
        lastnewc = newc;
        selectRow = i;
      }

    }
    mPackageTotalsGraph.draw(mTotalGraphData, createTotalReportOptions());

    appTotalsTable.getColumnSortList().clear();
    appTotalsTable.getColumnSortList().push(new ColumnSortInfo(appTotalsTable.getColumn(1), false));

    // mPackageTable.draw(mPackageTableData, createPackageTableOptions());
    // if (mPackageTable.getSelections().length() == 0)
    // {
    // Selection sel = Selection.createRowSelection(selectRow);
    // JsArray<Selection> tableselect = Selection.createArray().cast();
    // tableselect.push(sel);
    // mPackageTable.setSelections(tableselect);
    // // mPackageTotalsGraph.setSelections(tableselect);
    // updateAppMonthGraph(tableselect);
    // }

  }

  private void editPackage(AppPackageShared packagedata)
  {
    // Window.alert(object.AppName);
    PackageEdit.doEditDialog(packagedata, remoteService, new PackageEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, AppPackageShared appPackageShared)
      {
        if (ok)
        {
          loadTotalsGraphData();
        }
      }
    });
  }

  private void viewPackage(AppPackageShared packagedata)
  {
    Window.alert(packagedata.AppName);
  }

  private void createPackageTableColumns()
  {
    mPackageTableDataProvider = new ListDataProvider<AppPackageShared>();
    ListHandler<AppPackageShared> columnSortHandler = new ListHandler<AppPackageShared>(mPackageTableDataProvider.getList());
    mPackageTableDataProvider.addDataDisplay(appTotalsTable);
    panelAppGrid.setWidth((int) ((double) browserWidth * 0.5) + "px");
    // panelAppGrid.setHeight("300px");
    panelAppGrid.setHeight((int) ((double) browserHeight * 0.5) + "px");

    appTotalsTable.setAutoHeaderRefreshDisabled(true);
    appTotalsTable.setEmptyTableWidget(new Label("No Apps"));

    // Create name column.
    Column<AppPackageShared, String> nameColumn = new Column<AppPackageShared, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackageShared data)
      {
        return data.AppName;
      }
    };
    nameColumn.setSortable(true);

    Column<AppPackageShared, String> newColumn = new Column<AppPackageShared, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackageShared data)
      {

        return Integer.toString(data.Totals.Reports - data.Totals.LookedAt);
      }

    };
    newColumn.setSortable(true);
    newColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);

    Column<AppPackageShared, String> notFixedColumn = new Column<AppPackageShared, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackageShared data)
      {

        return Integer.toString(data.Totals.Reports - data.Totals.Fixed);
      }

    };
    notFixedColumn.setSortable(true);
    notFixedColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);

    Column<AppPackageShared, String> lookedAtColumn = new Column<AppPackageShared, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackageShared data)
      {

        return Integer.toString(data.Totals.LookedAt);
      }

    };
    lookedAtColumn.setSortable(true);
    lookedAtColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);

    Column<AppPackageShared, String> fixedColumn = new Column<AppPackageShared, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackageShared data)
      {

        return Integer.toString(data.Totals.Fixed);
      }

    };
    fixedColumn.setSortable(true);
    fixedColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);

    Column<AppPackageShared, String> reportsColumn = new Column<AppPackageShared, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackageShared data)
      {

        return Integer.toString(data.Totals.Reports);
      }

    };

    reportsColumn.setSortable(true);
    reportsColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);

    ActionCell<AppPackageShared> actionEdit = new ActionCell<AppPackageShared>("Edit", new ActionCell.Delegate<AppPackageShared>()
    {

      @Override
      public void execute(AppPackageShared object)
      {
        editPackage(object);
      }

    });

    Column<AppPackageShared, AppPackageShared> editColumn = new Column<AppPackageShared, AppPackageShared>(actionEdit)
    {

      @Override
      public AppPackageShared getValue(AppPackageShared object)
      {
        return object;
      }

    };
    editColumn.setHorizontalAlignment(Column.ALIGN_CENTER);

    ActionCell<AppPackageShared> actionOpen = new ActionCell<AppPackageShared>("Open", new ActionCell.Delegate<AppPackageShared>()
    {

      @Override
      public void execute(AppPackageShared object)
      {
        viewPackage(object);
      }

    });

    Column<AppPackageShared, AppPackageShared> openColumn = new Column<AppPackageShared, AppPackageShared>(actionOpen)
    {

      @Override
      public AppPackageShared getValue(AppPackageShared object)
      {
        return object;
      }

    };
    openColumn.setHorizontalAlignment(Column.ALIGN_CENTER);

    columnSortHandler.setComparator(nameColumn, new Comparator<AppPackageShared>()
    {
      public int compare(AppPackageShared o1, AppPackageShared o2)
      {
        if (o1 == o2)
        {
          return 0;
        }

        if (o1 != null)
        {
          return (o2 != null) ? o1.AppName.compareTo(o2.AppName) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(newColumn, new Comparator<AppPackageShared>()
    {
      public int compare(AppPackageShared o1, AppPackageShared o2)
      {
        if (o1 == o2)
        {
          return 0;
        }

        if (o1 != null && o2 != null)
        {
          int o1value = o1.Totals.Reports - o1.Totals.LookedAt;
          int o2value = o2.Totals.Reports - o2.Totals.LookedAt;
          return new Integer(o1value).compareTo(o2value);
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(notFixedColumn, new Comparator<AppPackageShared>()
    {
      public int compare(AppPackageShared o1, AppPackageShared o2)
      {
        if (o1 == o2)
        {
          return 0;
        }

        if (o1 != null && o2 != null)
        {
          int o1value = o1.Totals.Reports - o1.Totals.Fixed;
          int o2value = o2.Totals.Reports - o2.Totals.Fixed;
          return new Integer(o1value).compareTo(o2value);
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(lookedAtColumn, new Comparator<AppPackageShared>()
    {
      public int compare(AppPackageShared o1, AppPackageShared o2)
      {
        if (o1 == o2)
        {
          return 0;
        }

        if (o1 != null && o2 != null)
        {
          return new Integer(o1.Totals.LookedAt).compareTo(o2.Totals.LookedAt);
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(fixedColumn, new Comparator<AppPackageShared>()
    {
      public int compare(AppPackageShared o1, AppPackageShared o2)
      {
        if (o1 == o2)
        {
          return 0;
        }

        if (o1 != null && o2 != null)
        {
          return new Integer(o1.Totals.Fixed).compareTo(o2.Totals.Fixed);
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(reportsColumn, new Comparator<AppPackageShared>()
    {
      public int compare(AppPackageShared o1, AppPackageShared o2)
      {
        if (o1 == o2)
        {
          return 0;
        }

        if (o1 != null && o2 != null)
        {
          return new Integer(o1.Totals.Reports).compareTo(o2.Totals.Reports);
        }
        return -1;
      }
    });

    appTotalsTable.addColumnSortHandler(columnSortHandler);

    appTotalsTable.addColumn(nameColumn, "App");
    appTotalsTable.addColumn(newColumn, "New");
    appTotalsTable.addColumn(notFixedColumn, "Not Fixed");
    appTotalsTable.addColumn(lookedAtColumn, "Looked At");
    appTotalsTable.addColumn(fixedColumn, "Fixed");
    appTotalsTable.addColumn(reportsColumn, "Reports");
    appTotalsTable.addColumn(editColumn, "");
    appTotalsTable.addColumn(openColumn, "");

    String numWidth = "10%";
    appTotalsTable.setColumnWidth(newColumn, numWidth);
    appTotalsTable.setColumnWidth(notFixedColumn, numWidth);
    appTotalsTable.setColumnWidth(lookedAtColumn, numWidth);
    appTotalsTable.setColumnWidth(fixedColumn, numWidth);
    appTotalsTable.setColumnWidth(reportsColumn, numWidth);
    appTotalsTable.setColumnWidth(editColumn, "60px");
    appTotalsTable.setColumnWidth(openColumn, "85px");

    // appTotalsTable.addColumn(, "Actions");
    final SingleSelectionModel<AppPackageShared> singleSelectionModel = new SingleSelectionModel<AppPackageShared>(KEY_PROVIDER);
    appTotalsTable.setSelectionModel(singleSelectionModel, DefaultSelectionEventManager.<AppPackageShared> createDefaultManager());
    singleSelectionModel.addSelectionChangeHandler(new Handler()
    {

      @Override
      public void onSelectionChange(SelectionChangeEvent event)
      {
        AppPackageShared appPackage = singleSelectionModel.getSelectedObject();
        updateAppMonthGraph(appPackage.PACKAGE_NAME);
      }
    });
  }

}
