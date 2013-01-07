package nz.org.winters.appspot.acrareporter.client;

import java.util.List;
import java.util.Map;

import nz.org.winters.appspot.acrareporter.shared.ACRALogShared;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.AppUserShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.shared.MappingFileShared;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface RemoteDataServiceAsync
{
  void retrace(String mapping, String error, AsyncCallback<String> callback);

  void getMaps(LoginInfo user, AsyncCallback<Map<Long, String>> callback);

  void retrace(Long mappingId, String error, AsyncCallback<String> callback);

  void getPackages(LoginInfo user,AsyncCallback<List<AppPackageShared>> callback);

  void getBasicErrorInfo(String apppackage, AsyncCallback<List<BasicErrorInfoShared>> callback);

  void getACRALog(String REPORT_ID, AsyncCallback<ACRALogShared> callback);

  void deleteReport(String REPORT_ID, AsyncCallback<Void> callback);

  void markReportFixed(String REPORT_ID, boolean state, AsyncCallback<Void> callback);

  void markReportLookedAt(String REPORT_ID, boolean state, AsyncCallback<Void> callback);

  void retraceReport(String REPORT_ID, AsyncCallback<Void> callback);

  void markReportEMailed(String REPORT_ID, boolean state, AsyncCallback<Void> callback);

  void getPackage(String PACKAGE_NAME, AsyncCallback<AppPackageShared> callback);

  void markReportsLookedAt(List<String> reportIds, boolean state, AsyncCallback<Void> callback);

  void markReportsEMailed(List<String> reportIds, boolean state, AsyncCallback<Void> callback);

  void markReportsFixed(List<String> reportIds, boolean state, AsyncCallback<Void> callback);

  void writeAppPackageShared(AppPackageShared appPackageShared, AsyncCallback<Void> callback);

  void addAppPackageShared(LoginInfo user,AppPackageShared appPackageShared, AsyncCallback<Void> callback);

  void writeAppUserShared(AppUserShared appUserShared, AsyncCallback<Void> callback);

  void addAppUserShared(LoginInfo user, AppUserShared appUserShared, AsyncCallback<Void> callback);

  void getMappingFiles(String PACKAGE_NAME, AsyncCallback<List<MappingFileShared>> callback);

  void deleteMappings(List<Long> ids, AsyncCallback<Void> callback);

  void editMappingVersion(Long id, String version, AsyncCallback<Void> callback);

  void deleteReports(List<String> reportIds, AsyncCallback<Void> callback);


}
