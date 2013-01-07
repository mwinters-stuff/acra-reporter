package nz.org.winters.appspot.acrareporter.client;

import java.util.List;
import java.util.Map;

import nz.org.winters.appspot.acrareporter.shared.ACRALogShared;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.AppUserShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.shared.MappingFileShared;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("remote")
public interface RemoteDataService extends RemoteService
{
  String retrace(String mapping, String error) throws IllegalArgumentException;
  String retrace(Long mappingId, String error) throws IllegalArgumentException;
  
  Map<Long,String> getMaps(LoginInfo user) throws IllegalArgumentException;
  List<AppPackageShared> getPackages(LoginInfo user) throws IllegalArgumentException;

  AppPackageShared getPackage(String PACKAGE_NAME) throws IllegalArgumentException;
  List<BasicErrorInfoShared> getBasicErrorInfo(String PACKAGE_NAME) throws IllegalArgumentException;
  
  ACRALogShared getACRALog(String REPORT_ID) throws IllegalArgumentException;

  void deleteReport(String REPORT_ID) throws IllegalArgumentException;
  void markReportLookedAt(String REPORT_ID, boolean state) throws IllegalArgumentException;
  void markReportFixed(String REPORT_ID, boolean state) throws IllegalArgumentException;
  void markReportEMailed(String REPORT_ID, boolean state) throws IllegalArgumentException;

  void markReportsLookedAt(List<String> reportIds, boolean state) throws IllegalArgumentException;
  void markReportsFixed(List<String> reportIds, boolean state) throws IllegalArgumentException;
  void markReportsEMailed(List<String> reportIds, boolean state) throws IllegalArgumentException;
  
  void deleteReports(List<String> reportIds)throws IllegalArgumentException;
  
  
  void writeAppPackageShared(AppPackageShared appPackageShared) throws IllegalArgumentException;
  void addAppPackageShared(LoginInfo user, AppPackageShared appPackageShared) throws IllegalArgumentException;

  void writeAppUserShared(AppUserShared appUserShared) throws IllegalArgumentException;
  void addAppUserShared(LoginInfo user, AppUserShared appUserShared) throws IllegalArgumentException;
  
  List<MappingFileShared> getMappingFiles(String PACKAGE_NAME) throws IllegalArgumentException;
  
  void retraceReport(String REPORT_ID) throws IllegalArgumentException;
  
  void deleteMappings(List<Long> ids) throws IllegalArgumentException;
  void editMappingVersion(Long id, String version) throws IllegalArgumentException;

}
