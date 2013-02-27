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
  
  
  List<AppPackage> getPackages(LoginInfo user) throws IllegalArgumentException;

  AppPackage getPackage(String PACKAGE_NAME) throws IllegalArgumentException;
  List<BasicErrorInfo> getBasicErrorInfo(String apppackage, ErrorListFilter elf);
  
  ACRALog getACRALog(String REPORT_ID) throws IllegalArgumentException;

  void deleteReport(String REPORT_ID) throws IllegalArgumentException;
  void markReportLookedAt(String REPORT_ID, boolean state) throws IllegalArgumentException;
  void markReportFixed(String REPORT_ID, boolean state) throws IllegalArgumentException;
  void markReportEMailed(String REPORT_ID, boolean state) throws IllegalArgumentException;

  void markReportsLookedAt(List<String> reportIds, boolean state) throws IllegalArgumentException;
  void markReportsFixed(List<String> reportIds, boolean state) throws IllegalArgumentException;
  void markReportsEMailed(List<String> reportIds, boolean state) throws IllegalArgumentException;
  
  void deleteReports(List<String> reportIds)throws IllegalArgumentException;
  
  
  void writeAppPackage(AppPackage appPackage) throws IllegalArgumentException;
  void addAppPackage(LoginInfo user, AppPackage appPackage) throws IllegalArgumentException;

  void writeAppUser(AppUser appUser) throws IllegalArgumentException;
  void addAppUser(LoginInfo user, AppUser appUser) throws IllegalArgumentException;
  void addAppUser(AppUser appUser) throws IllegalArgumentException;
  
  List<MappingFileInfo> getMappingFiles(String PACKAGE_NAME) throws IllegalArgumentException;
  
  void retraceReport(String REPORT_ID) throws IllegalArgumentException;
  
  void deleteMappings(List<Long> ids) throws IllegalArgumentException;
  void editMappingVersion(Long id, String version) throws IllegalArgumentException;

  void sendFixedEMail(LoginInfo user, List<String> reportIds, String bcc, String subject, String body) throws IllegalArgumentException;
  
  String findEMailAddresses(List<String> reportIds)  throws IllegalArgumentException;
  
  List<AppPackage> getPackageGraphDataTotals(LoginInfo user) throws IllegalArgumentException;
  List<DailyCounts> getLastMonthDailyCounts(LoginInfo user) throws IllegalArgumentException;
  List<DailyCounts> getPackageLastMonthDailyCounts(LoginInfo user, String PACKAGE_NAME) throws IllegalArgumentException;
  
}
