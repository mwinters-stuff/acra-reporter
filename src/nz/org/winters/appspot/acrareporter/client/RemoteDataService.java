package nz.org.winters.appspot.acrareporter.client;
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
import java.util.List;
import java.util.Map;

import nz.org.winters.appspot.acrareporter.shared.ACRALogShared;
import nz.org.winters.appspot.acrareporter.shared.AppPackageShared;
import nz.org.winters.appspot.acrareporter.shared.AppUserShared;
import nz.org.winters.appspot.acrareporter.shared.BasicErrorInfoShared;
import nz.org.winters.appspot.acrareporter.shared.DailyCountsShared;
import nz.org.winters.appspot.acrareporter.shared.ErrorListFilter;
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
  List<BasicErrorInfoShared> getBasicErrorInfo(String apppackage, ErrorListFilter elf);
  
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
  void addAppUser(AppUserShared appUserShared) throws IllegalArgumentException;
  
  List<MappingFileShared> getMappingFiles(String PACKAGE_NAME) throws IllegalArgumentException;
  
  void retraceReport(String REPORT_ID) throws IllegalArgumentException;
  
  void deleteMappings(List<Long> ids) throws IllegalArgumentException;
  void editMappingVersion(Long id, String version) throws IllegalArgumentException;

  void sendFixedEMail(LoginInfo user, List<String> reportIds, String bcc, String subject, String body) throws IllegalArgumentException;
  
  String findEMailAddresses(List<String> reportIds)  throws IllegalArgumentException;
  
  List<AppPackageShared> getPackageGraphDataTotals(LoginInfo user) throws IllegalArgumentException;
  List<DailyCountsShared> getLastMonthDailyCounts(LoginInfo user) throws IllegalArgumentException;
  List<DailyCountsShared> getPackageLastMonthDailyCounts(LoginInfo user, String PACKAGE_NAME) throws IllegalArgumentException;
  
}
