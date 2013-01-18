package nz.org.winters.appspot.acrareporter.client.ui;

import java.util.List;

import nz.org.winters.appspot.acrareporter.client.RemoteDataService;
import nz.org.winters.appspot.acrareporter.client.RemoteDataServiceAsync;
import nz.org.winters.appspot.acrareporter.shared.DailyCountsShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.shared.PackageGraphData;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart;
import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart.PieOptions;

public class Overview extends Composite
{

  private static OverviewUiBinder      uiBinder      = GWT.create(OverviewUiBinder.class);
  @UiField HorizontalPanel panel;

  private final RemoteDataServiceAsync remoteService = GWT.create(RemoteDataService.class);

  private LoginInfo                    loginInfo;

  private DataTable                    mTotalGraphData;
  private PieChart                     mPackageTotalsGraph;
  private LineChart                    mTotalsMonthGraph;

  private DataTable                    mTotalsMonthGraphData;

  interface OverviewUiBinder extends UiBinder<Widget, Overview>
  {
  }

  public Overview(LoginInfo loginInfo)
  {
    this.loginInfo = loginInfo;
    initWidget(uiBinder.createAndBindUi(this));

    Runnable onLoadCallback = new Runnable()
    {
      public void run()
      {
        //Panel panel = RootPanel.get();

        // Create a pie chart visualization.
        mPackageTotalsGraph = new PieChart(createTotalReportTable(), createTotalReportOptions());
        panel.add(mPackageTotalsGraph);

        mTotalsMonthGraph = new LineChart(createTotalsMonthTable(), createTotalsMonthOptions());
        panel.add(mTotalsMonthGraph);
      }

    };

    VisualizationUtils.loadVisualizationApi(onLoadCallback, CoreChart.PACKAGE);

  }

  private Options createTotalReportOptions()
  {
    PieOptions options = PieOptions.create();
    options.setWidth(500);
    options.setHeight(340);
    options.set3D(true);
    options.setTitle("Total Reports per App");

    return options;
  }

  private AbstractDataTable createTotalReportTable()
  {
    mTotalGraphData = DataTable.create();
    mTotalGraphData.addColumn(ColumnType.STRING, "Package");
    mTotalGraphData.addColumn(ColumnType.NUMBER, "Reports");

    updateTotalsGraph();
    return mTotalGraphData;
  }

  private Options createTotalsMonthOptions()
  {
    Options options = Options.create();
    options.setWidth(700);
    options.setHeight(340);

    options.setTitle("Reports per day");

    return options;
  }

  private AbstractDataTable createTotalsMonthTable()
  {
    mTotalsMonthGraphData = DataTable.create();
    mTotalsMonthGraphData.addColumn(ColumnType.DATE, "Date");
    mTotalsMonthGraphData.addColumn(ColumnType.NUMBER, "Reports");
    mTotalsMonthGraphData.addColumn(ColumnType.NUMBER, "Fixed");

    updateTotalsMonthGraph();
    return mTotalsMonthGraphData;
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
          mTotalsMonthGraphData.setValue(i, 0, result.get(i).date);
          mTotalsMonthGraphData.setValue(i, 1, result.get(i).Reports);
          mTotalsMonthGraphData.setValue(i, 2, result.get(i).Fixed);
        }
        mTotalsMonthGraph.draw(mTotalsMonthGraphData, createTotalsMonthOptions());

      }
    });

  }

  private void updateTotalsGraph()
  {
    remoteService.getPackageGraphDataTotals(loginInfo, new AsyncCallback<List<PackageGraphData>>()
    {

      @Override
      public void onSuccess(List<PackageGraphData> result)
      {
        mTotalGraphData.removeRows(0, mTotalGraphData.getNumberOfRows());

        mTotalGraphData.addRows(result.size());
        for (int i = 0; i < result.size(); i++)
        {
          mTotalGraphData.setValue(i, 0, result.get(i).AppName);
          mTotalGraphData.setValue(i, 1, result.get(i).counts.Reports);
        }
        mPackageTotalsGraph.draw(mTotalGraphData, createTotalReportOptions());
      }

      @Override
      public void onFailure(Throwable caught)
      {
        Window.alert(caught.getMessage());

      }
    });

  }
}
