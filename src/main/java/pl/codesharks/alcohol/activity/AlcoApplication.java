/******************************************************************************
 * Copyright 2014 CodeSharks                                                  *
 *                                                                            *
 * Licensed under the Apache License, Version 2.0 (the "License");            *
 * you may not use this file except in compliance with the License.           *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing, software        *
 * distributed under the License is distributed on an "AS IS" BASIS,          *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 * See the License for the specific language governing permissions and        *
 * limitations under the License.                                             *
 ******************************************************************************/

package pl.codesharks.alcohol.activity;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import pl.codesharks.alcohol.preferences.WebApi;

public class AlcoApplication extends Application {
    private static AlcoApplication singleton;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public SharedPreferences webApiSP;

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        webApiSP = getSharedPreferences(WebApi.FILE, MODE_PRIVATE);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public AlcoApplication getInstance() {
        return singleton;
    }

    public boolean isLogged() {
        webApiSP = getSharedPreferences(WebApi.FILE, MODE_PRIVATE);
        return webApiSP.getBoolean(WebApi.LOGGED, false) && webApiSP.getString(WebApi.SESSION_TOKEN, "").length() > 10;
    }

}