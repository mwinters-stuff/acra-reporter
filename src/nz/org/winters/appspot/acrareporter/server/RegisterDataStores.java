package nz.org.winters.appspot.acrareporter.server;
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

import nz.org.winters.appspot.acrareporter.store.ACRALog;
import nz.org.winters.appspot.acrareporter.store.AppPackage;
import nz.org.winters.appspot.acrareporter.store.AppUser;
import nz.org.winters.appspot.acrareporter.store.BasicErrorInfo;
import nz.org.winters.appspot.acrareporter.store.DailyCounts;
import nz.org.winters.appspot.acrareporter.store.MappingFile;
import nz.org.winters.appspot.acrareporter.store.MappingFileData;
import nz.org.winters.appspot.acrareporter.store.MappingFileInfo;

import com.googlecode.objectify.ObjectifyService;
// helper to register the datastore files.

public class RegisterDataStores
{
  public static void register()
  {
    ObjectifyService.register(ACRALog.class);
    ObjectifyService.register(AppPackage.class);
    ObjectifyService.register(AppUser.class);
    ObjectifyService.register(BasicErrorInfo.class);
    ObjectifyService.register(DailyCounts.class);
    ObjectifyService.register(MappingFile.class);
    ObjectifyService.register(MappingFileInfo.class);
    ObjectifyService.register(MappingFileData.class);
    ObjectifyService.register(SettingStore.class);

  }
}
