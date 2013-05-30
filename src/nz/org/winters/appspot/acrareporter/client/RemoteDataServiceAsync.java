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

import nz.org.winters.appspot.acrareporter.shared.ErrorListFilter;
import nz.org.winters.appspot.acrareporter.shared.LoginInfo;
import nz.org.winters.appspot.acrareporter.store.ACRALog;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.AppUser;
import nz.org.winters.appspot.acrareporter.store.BasicErrorInfo;
import nz.org.winters.appspot.acrareporter.store.DailyCounts;
import nz.org.winters.appspot.acrareporter.store.MappingFileInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;


public interface RemoteDataServiceAsync
{
  void retrace(String mapping, String error, AsyncCallback<String> callback);



  void retrace(Long mappingId, String error, AsyncCallback<String> callback);

  void getPackages(LoginInfo user,AsyncCallback<List<AppPackage>> callback);

  void getBasicErrorInfo(String apppackage, ErrorListFilter elf, AsyncCallback<List<BasicErrorInfo>> callback);

  void getACRALog(String REPORT_ID, AsyncCallback<ACRALog> callback);

  void deleteReport(String REPORT_ID, AsyncCallback<Void> callback);

  void markReportFixed(String REPORT_ID, boolean state, AsyncCallback<Void> callback);

  void markReportLookedAt(String REPORT_ID, boolean state, AsyncCallback<Void> callback);

  void retraceReport(String REPORT_ID, AsyncCallback<Void> callback);

  void markReportEMailed(String REPORT_ID, boolean state, AsyncCallback<Void> callback);

  void getPackage(String PACKAGE_NAME, AsyncCallback<AppPackage> callback);

  void markReportsLookedAt(List<String> reportIds, boolean state, AsyncCallback<Void> callback);

  void markReportsEMailed(List<String> reportIds, boolean state, AsyncCallback<Void> callback);

  void markReportsFixed(List<String> reportIds, boolean state, AsyncCallback<Void> callback);

  void writeAppPackage(AppPackage appPackage, AsyncCallback<Void> callback);

  void addAppPackage(LoginInfo user,AppPackage appPackage, AsyncCallback<Void> callback);

  void writeAppUser(AppUser appUser, AsyncCallback<Void> callback);

  void addAppUser(LoginInfo user, AppUser appUser, AsyncCallback<Void> callback);

  void getMappingFiles(String PACKAGE_NAME, AsyncCallback<List<MappingFileInfo>> callback);

  void deleteMappings(List<Long> ids, AsyncCallback<Void> callback);

  void editMappingVersion(Long id, String version, AsyncCallback<Void> callback);

  void deleteReports(List<String> reportIds, AsyncCallback<Void> callback);

  void sendFixedEMail(LoginInfo user, List<String> reportIds, String bcc, String subject, String body, AsyncCallback<Void> callback);

  void findEMailAddresses(List<String> reportIds, AsyncCallback<String> callback);

  void addAppUser(AppUser appUser, AsyncCallback<Void> callback);

  void getPackageGraphDataTotals(LoginInfo user, AsyncCallback<List<AppPackage>> callback);

  void getLastMonthDailyCounts(LoginInfo user, AsyncCallback<List<DailyCounts>> callback);

  void getPackageLastMonthDailyCounts(LoginInfo user, String PACKAGE_NAME, AsyncCallback<List<DailyCounts>> callback);



  void purgeReports(String PACKAGE_NAME, AsyncCallback<Void> callback);


}
