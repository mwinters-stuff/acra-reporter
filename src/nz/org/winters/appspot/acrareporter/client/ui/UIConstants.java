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
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

//  private UIConstants                   constants     = (UIConstants) GWT.create(UIConstants.class);

public interface UIConstants extends Messages
{
  @Key("android")
  String android();

  @Key("grid.empty")
  String gridEmpty();

  @Key("errorlist.grid.usercrashdate")
  String errorListGridCrashDate();

  @Key("errorlist.grid.version")
  String errorListGridVersion();

  @Key("errorlist.show.all")
  String errorListShowAll();

  @Key("errorlist.show.new")
  String errorListShowNew();

  @Key("errorlist.show.lookedat")
  String errorListShowLookedAt();

  @Key("errorlist.show.notfixed")
  String errorListShowNotFixed();

  @Key("errorlist.show.fixed")
  String errorListShowFixed();
    
  
  @Key("errorlist.confirm.delete")
  String errorListConfirmDelete();

  @Key("acrareportview.error.fetch")
  String acraReportViewErrorFetch(String reportId);

  @Key("acrareportview.label.title")
  String acraReportViewLabelTitle(String reportId);

  @Key("acrareportview.label.report")
  String acraReportViewLabelReport();

  @Key("acrareportview.confirm.delete")
  String acraReportViewConfirmDelete();

  @Key("loadingview.label.loading")
  String loadingViewLabelLoading();

  @Key("apppackage.label.title")
  String appPackageLabelTitle(String appName, String packageName);

  @Key("email.label.send")
  String emailLabelSend(String appName);

  @Key("mappinglist.grid.date")
  String mappingListGridDate();

  @Key("mappinglist.grid.version")
  String mappingListGridVersion();

  @Key("mappinglist.label.title")
  String mappingListLabelTitle(String packageName);

  @Key("mappinglist.confirm.delete")
  String mappingListConformDelete();

  @Key("mappinglist.label.editmapping")
  String mappingListLabelEditMapping();

  @Key("mappingupload.alert.nofile")
  String mappingUploadAlertNofile();

  @Key("mappingupload.alert.noversion")
  String mappingUploadAlertNoVersion();

  @Key("mappingupload.alert.response")
  String mappingUploadAlertResponse(String value);

  @Key("mappingupload.label.title")
  String mappingUploadLabelTitle(String packageName);
  
  @Key("grid.name")
  String gridName();
  
  @Key("grid.value")
  String gridValue();

  @Key("packageedit.label.edit")
  String packageEditLabelEdit(String appName);
  
  @Key("packageedit.label.add")
  String packageEditLabelAdd();

  @Key("signup.wibble")
  String signupWibble();
  
  @Key("signup.alert.already")
  String signupAleryAlready();
  
  @Key("signup.alert.firstname")
  String signupAleryFirstname();
  
  @Key("signup.alert.lastname")
  String signupAleryLastname();
  
  @Key("signup.alert.town")
  String signupAleryTown();
  
  @Key("signup.alert.country")
  String signupAleryCountry();
  
  @Key("signup.alert.authusername")
  String signupAleryAuthUsername();
  
  @Key("signup.alert.authpassword")
  String signupAleryAuthPassword();



}
