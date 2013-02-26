package nz.org.winters.appspot.acrareporter.client.ui;

import java.util.Comparator;
import java.util.List;

import nz.org.winters.appspot.acrareporter.client.RemoteDataService;
import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.client.ui.images.Resources;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.DailyCounts;

import com.google.gwt.ajaxloader.client.Properties;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
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
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.events.ReadyHandler;
import com.google.gwt.visualization.client.formatters.DateFormat;
import com.google.gwt.visualization.client.visualizations.Table;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;
import com.google.gwt.visualization.client.visualizations.corechart.TextStyle;

public class Overview extends Composite implements ChangeHandler, AppPackageView.CallbackClosePackageView
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
  DataGrid<AppPackage>                        appTotalsTable = new DataGrid<AppPackage>();
  @UiField
  VerticalPanel                                     basePanel;
  @UiField
  ScrollPanel                                       overviewPanel;
  @UiField
  Button                                            buttonAddPackage;

  private final RemoteDataServiceAsync              remoteService  = GWT.create(RemoteDataService.class);

  private LoginInfo                                 loginInfo;

  private DataTable                                 mTotalGraphData;
  private PieChart                                  mPackageTotalsGraph;
  private LineChart                                 mTotalsMonthGraph;
  private LineChart                                 mPackageMonthGraph;

  private DataTable                                 mTotalsMonthGraphData;
  private DataTable                                 mPackageMonthGraphData;
  private List<AppPackage>                    mAppPackage;
  private DateFormat                                mShortDateFormat;
 

  private int                                       browserWidth;
  private int                                       browserHeight;

  private TextStyle                                 mBoldTitleFont;

  public static final ProvidesKey<AppPackage> KEY_PROVIDER   = new ProvidesKey<AppPackage>()
                                                                   {
                                                                     @Override
                                                                     public Object getKey(AppPackage item)
                                                                     {
                                                                       return item == null ? null : item.id;
                                                                     }
                                                                   };
  private AppPackageListProvider                              mPackageTableDataProvider   = new AppPackageListProvider();

  interface OverviewUiBinder extends UiBinder<Widget, Overview>
  {
  }

  public Overview(LoginInfo loginInfo)
  {
    this.loginInfo = loginInfo;
    initWidget(uiBinder.createAndBindUi(this));
    AppLoadingView.getInstance().start();

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
        mBoldTitleFont = TextStyle.create();
        mBoldTitleFont.setFontSize(18);

        DateFormat.Options dfo = DateFormat.Options.create();
        dfo.setPattern(DateFormat.FormatType.SHORT);
        mShortDateFormat = DateFormat.create(dfo);

        mPackageTotalsGraph = new PieChart(createTotalReportTable(), createTotalReportOptions());
        pieHolder.add(mPackageTotalsGraph);

        mTotalsMonthGraph = new LineChart(createTotalsMonthTable(), createTotalsMonthOptions());
        topPanel.add(mTotalsMonthGraph);

        mPackageMonthGraph = new LineChart(createPackageMonthTable(), createPackageMonthOptions(null));
        midPanel.add(mPackageMonthGraph);

        mPackageTotalsGraph.addReadyHandler(new ReadyHandler()
        {

          @Override
          public void onReady(ReadyEvent event)
          {
            totalsDataSelection.setEnabled(true);

            AppLoadingView.getInstance().stop();
          }

        });

        updateData();

      }

    };

    VisualizationUtils.loadVisualizationApi(onLoadCallback, CoreChart.PACKAGE, Table.PACKAGE);

  }

  private void updateData()
  {
    updateTotalsGraph();
    updateTotalsMonthGraph();
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
        options.setTitle("Total Reports Fixed per App");
        break;
      case 2: // looked at
        options.setTitle("Total Reports Looked At per App");
        break;
      case 3: // not fixed
        options.setTitle("Total Reports Not Fixed per App");
        break;
      case 4: // new
        options.setTitle("Total New Reports per App");
        break;
    }

    options.setTitleTextStyle(mBoldTitleFont);
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
    options.setTitleTextStyle(mBoldTitleFont);

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
    options.setTitleTextStyle(mBoldTitleFont);

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
    mTotalsMonthGraphData.addColumn(ColumnType.NUMBER, "New");

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
    mPackageMonthGraphData.addColumn(ColumnType.NUMBER, "Looked At");
    mPackageMonthGraphData.addColumn(ColumnType.NUMBER, "Not Fixed");
    mPackageMonthGraphData.addColumn(ColumnType.NUMBER, "Fixed");
    mPackageMonthGraphData.addColumn(ColumnType.NUMBER, "New");
    return mPackageMonthGraphData;
  }

  // month totals bar chart.

  private void updateTotalsGraph()
  {
    mPackageTableDataProvider.startLoading();

    remoteService.getPackageGraphDataTotals(loginInfo, new AsyncCallback<List<AppPackage>>()
    {

      @Override
      public void onSuccess(List<AppPackage> result)
      {
        mAppPackage = result;
        loadTotalsGraphData();

        mPackageTableDataProvider.stopLoading(result);
        appTotalsTable.getColumnSortList().clear();
        appTotalsTable.getColumnSortList().push(appTotalsTable.getColumn(1));
        
        ColumnSortEvent.fire(appTotalsTable, appTotalsTable.getColumnSortList());
        
        if (!result.isEmpty())
        {
          appTotalsTable.getSelectionModel().setSelected(result.get(0), true);
        }

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
    remoteService.getLastMonthDailyCounts(loginInfo, new AsyncCallback<List<DailyCounts>>()
    {

      @Override
      public void onFailure(Throwable caught)
      {
        Window.alert(caught.getMessage());

      }

      @Override
      public void onSuccess(List<DailyCounts> result)
      {

        mTotalsMonthGraphData.removeRows(0, mTotalsMonthGraphData.getNumberOfRows());

        mTotalsMonthGraphData.addRows(result.size());
        for (int i = 0; i < result.size(); i++)
        {
          DailyCounts data = result.get(i);
          mTotalsMonthGraphData.setValue(i, 0, data.date);
          mTotalsMonthGraphData.setValue(i, 1, data.Reports);
          mTotalsMonthGraphData.setValue(i, 2, data.Fixed);
          mTotalsMonthGraphData.setValue(i, 3, data.NewReports());
        }
        mShortDateFormat.format(mTotalsMonthGraphData, 0);

        mTotalsMonthGraph.draw(mTotalsMonthGraphData, createTotalsMonthOptions());

      }
    });

  }

  private void updateAppMonthGraph(final String packageName)
  {

    remoteService.getPackageLastMonthDailyCounts(loginInfo, packageName, new AsyncCallback<List<DailyCounts>>()
    {

      @Override
      public void onSuccess(List<DailyCounts> result)
      {

        mPackageMonthGraphData.removeRows(0, mPackageMonthGraphData.getNumberOfRows());
        if (result.size() > 0)
        {
          mPackageMonthGraphData.addRows(result.size());
          for (int i = 0; i < result.size(); i++)
          {
            DailyCounts data = result.get(i);
            mPackageMonthGraphData.setValue(i, 0, data.date);
            mPackageMonthGraphData.setValue(i, 1, data.Reports);
            mPackageMonthGraphData.setValue(i, 2, data.LookedAt);
            mPackageMonthGraphData.setValue(i, 3, data.NotFixedReports());
            mPackageMonthGraphData.setValue(i, 4, data.Fixed);
            mPackageMonthGraphData.setValue(i, 5, data.NewReports());
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
    List<AppPackage> packages  = mPackageTableDataProvider.getList();
    for (AppPackage appPackage : packages)
    {
      if (appPackage.PACKAGE_NAME.equals(packageName))
      {
        return appPackage.AppName;
      }
    }
    return "";
  }

  @Override
  public void onChange(ChangeEvent event)
  {
    loadTotalsGraphData();

  }

  void loadTotalsGraphData()
  {
    mTotalGraphData.removeRows(0, mTotalGraphData.getNumberOfRows());
    // mPackageTableData.removeRows(0, mPackageTableData.getNumberOfRows());

    mTotalGraphData.addRows(mAppPackage.size());
    // mPackageTableData.addRows(mAppPackage.size());
    for (int i = 0; i < mAppPackage.size(); i++)
    {
      AppPackage data = mAppPackage.get(i);

      mTotalGraphData.setValue(i, 0, data.AppName);

      int newc = data.Totals.NewReports();
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
          mTotalGraphData.setValue(i, 1, data.Totals.NotFixedReports());
          break;
        case 4: // new
          mTotalGraphData.setValue(i, 1, newc);
          break;
      }

    }
    mPackageTotalsGraph.draw(mTotalGraphData, createTotalReportOptions());

  }

  private void editPackage(AppPackage packagedata)
  {
    // Window.alert(object.AppName);
    PackageEdit.doEditDialog(packagedata, remoteService, new PackageEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, AppPackage appPackage)
      {
        if (ok)
        {
          loadTotalsGraphData();
        }
      }
    });
  }

  private void viewPackage(AppPackage packagedata)
  {

    // RootLayoutPanel.get().clear();
    // RootLayoutPanel.get().add(new AppPackageView(loginInfo,packagedata));

    // panel.add(new OldMainPage(loginInfo));

    AppLoadingView.getInstance().start();

    overviewPanel.setVisible(false);
    basePanel.remove(overviewPanel);
    basePanel.add(new AppPackageView(loginInfo, packagedata, this));

  }

  private void createPackageTableColumns()
  {
    
    ListHandler<AppPackage> columnSortHandler = new ListHandler<AppPackage>(mPackageTableDataProvider.getList());
    mPackageTableDataProvider.addDataDisplay(appTotalsTable);
    panelAppGrid.setWidth((int) ((double) browserWidth * 0.5) + "px");
    // panelAppGrid.setHeight("300px");
    panelAppGrid.setHeight((int) ((double) browserHeight * 0.5) + "px");

    appTotalsTable.setAutoHeaderRefreshDisabled(true);
    appTotalsTable.setEmptyTableWidget(new Label("No Apps"));
    appTotalsTable.setLoadingIndicator(new Image(Resources.INSTANCE.loaderImage()));

    // Create name column.
    Column<AppPackage, String> nameColumn = new Column<AppPackage, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackage data)
      {
        return data.AppName;
      }
    };
    nameColumn.setSortable(true);

    Column<AppPackage, String> newColumn = new Column<AppPackage, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackage data)
      {

        return Integer.toString(data.Totals.NewReports());
      }

    };
    newColumn.setSortable(true);
    newColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);
    newColumn.setDefaultSortAscending(false);

    Column<AppPackage, String> notFixedColumn = new Column<AppPackage, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackage data)
      {

        return Integer.toString(data.Totals.NotFixedReports());
      }

    };
    notFixedColumn.setSortable(true);
    notFixedColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);

    Column<AppPackage, String> lookedAtColumn = new Column<AppPackage, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackage data)
      {

        return Integer.toString(data.Totals.LookedAt);
      }

    };
    lookedAtColumn.setSortable(true);
    lookedAtColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);

    Column<AppPackage, String> fixedColumn = new Column<AppPackage, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackage data)
      {

        return Integer.toString(data.Totals.Fixed);
      }

    };
    fixedColumn.setSortable(true);
    fixedColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);

    Column<AppPackage, String> deletedColumn = new Column<AppPackage, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackage data)
      {

        return Integer.toString(data.Totals.Deleted);
      }

    };
    deletedColumn.setSortable(true);
    deletedColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);

    Column<AppPackage, String> reportsColumn = new Column<AppPackage, String>(new TextCell())
    {
      @Override
      public String getValue(AppPackage data)
      {

        return Integer.toString(data.Totals.Reports);
      }

    };

    reportsColumn.setSortable(true);
    reportsColumn.setHorizontalAlignment(Column.ALIGN_RIGHT);

    ActionCell<AppPackage> actionEdit = new ActionCell<AppPackage>("Edit", new ActionCell.Delegate<AppPackage>()
    {

      @Override
      public void execute(AppPackage object)
      {
        editPackage(object);
      }

    });

    Column<AppPackage, AppPackage> editColumn = new Column<AppPackage, AppPackage>(actionEdit)
    {

      @Override
      public AppPackage getValue(AppPackage object)
      {
        return object;
      }

    };
    editColumn.setHorizontalAlignment(Column.ALIGN_CENTER);

    ActionCell<AppPackage> actionOpen = new ActionCell<AppPackage>("Open", new ActionCell.Delegate<AppPackage>()
    {

      @Override
      public void execute(AppPackage object)
      {
        viewPackage(object);
      }

    });

    Column<AppPackage, AppPackage> openColumn = new Column<AppPackage, AppPackage>(actionOpen)
    {

      @Override
      public AppPackage getValue(AppPackage object)
      {
        return object;
      }

    };
    openColumn.setHorizontalAlignment(Column.ALIGN_CENTER);

    columnSortHandler.setComparator(nameColumn, new Comparator<AppPackage>()
    {
      public int compare(AppPackage o1, AppPackage o2)
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
    columnSortHandler.setComparator(newColumn, new Comparator<AppPackage>()
    {
      public int compare(AppPackage o1, AppPackage o2)
      {
        if (o1 == o2)
        {
          return 0;
        }

        if (o1 != null && o2 != null)
        {
          return new Integer(o1.Totals.NewReports()).compareTo(o2.Totals.NewReports());
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(notFixedColumn, new Comparator<AppPackage>()
    {
      public int compare(AppPackage o1, AppPackage o2)
      {
        if (o1 == o2)
        {
          return 0;
        }

        if (o1 != null && o2 != null)
        {
          return new Integer(o1.Totals.NotFixedReports()).compareTo(o2.Totals.NotFixedReports());
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(lookedAtColumn, new Comparator<AppPackage>()
    {
      public int compare(AppPackage o1, AppPackage o2)
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
    columnSortHandler.setComparator(fixedColumn, new Comparator<AppPackage>()
    {
      public int compare(AppPackage o1, AppPackage o2)
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
    columnSortHandler.setComparator(reportsColumn, new Comparator<AppPackage>()
    {
      public int compare(AppPackage o1, AppPackage o2)
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
    columnSortHandler.setComparator(deletedColumn, new Comparator<AppPackage>()
    {
      public int compare(AppPackage o1, AppPackage o2)
      {
        if (o1 == o2)
        {
          return 0;
        }

        if (o1 != null && o2 != null)
        {
          return new Integer(o1.Totals.Deleted).compareTo(o2.Totals.Deleted);
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
    appTotalsTable.addColumn(deletedColumn, "Deleted");
    appTotalsTable.addColumn(reportsColumn, "Reports");
    appTotalsTable.addColumn(editColumn, "");
    appTotalsTable.addColumn(openColumn, "");

    String numWidth = "10%";
    appTotalsTable.setColumnWidth(newColumn, numWidth);
    appTotalsTable.setColumnWidth(notFixedColumn, numWidth);
    appTotalsTable.setColumnWidth(lookedAtColumn, numWidth);
    appTotalsTable.setColumnWidth(fixedColumn, numWidth);
    appTotalsTable.setColumnWidth(deletedColumn, numWidth);
    appTotalsTable.setColumnWidth(reportsColumn, numWidth);
    appTotalsTable.setColumnWidth(editColumn, "60px");
    appTotalsTable.setColumnWidth(openColumn, "85px");

    // appTotalsTable.addColumn(, "Actions");
    final SingleSelectionModel<AppPackage> singleSelectionModel = new SingleSelectionModel<AppPackage>(KEY_PROVIDER);
    appTotalsTable.setSelectionModel(singleSelectionModel, DefaultSelectionEventManager.<AppPackage> createDefaultManager());
    singleSelectionModel.addSelectionChangeHandler(new Handler()
    {

      @Override
      public void onSelectionChange(SelectionChangeEvent event)
      {
        AppPackage appPackage = singleSelectionModel.getSelectedObject();
        updateAppMonthGraph(appPackage.PACKAGE_NAME);
      }
    });
  }

  @Override
  public void close(AppPackageView view)
  {
    AppLoadingView.getInstance().start();
    overviewPanel.setVisible(true);
    view.setVisible(false);

    basePanel.remove(view);
    basePanel.add(overviewPanel);

    updateData();

    AppLoadingView.getInstance().stop();

  }

  @UiHandler("buttonAddPackage")
  void onButtonAddPackageClick(ClickEvent event)
  {
    PackageEdit.doAddDialog(loginInfo, remoteService, new PackageEdit.DialogCallback()
    {

      @Override
      public void result(boolean ok, AppPackage appPackage)
      {
        if (ok)
        {
          mAppPackage.add(appPackage);
          loadTotalsGraphData();

        }
      }
    });

  }

  @UiHandler("buttonUsers")
  void onButtonUsersClick(ClickEvent event)
  {
    PopupUsers.showPopup(loginInfo, (Widget) event.getSource());
  }
  
  
  

  class AppPackageListProvider extends ListDataProvider<AppPackage>
  {
    public void startLoading()
    {
      super.updateRowCount(0, false);
    }

    public void stopLoading(List<AppPackage> list)
    {
      if (list != null)
      {
        setList(list);
      }
      super.updateRowCount(getList().size(), true);

    }
  }  
}
