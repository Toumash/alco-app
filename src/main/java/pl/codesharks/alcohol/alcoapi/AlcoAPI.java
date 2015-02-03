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

package pl.codesharks.alcohol.alcoapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.ResponseHandlerInterface;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;
import pl.codesharks.alcohol.preferences.WebApi;

import java.io.UnsupportedEncodingException;

public class AlcoAPI {
    static public boolean MOCK_SESSIONS = true;
    static public String MOCK_SESSION_TOKEN = "536ef53d2395e1e8da60208a8a83c716";

    static public AsyncHttpClient getAsyncHttpClient(Context context) {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

        PersistentCookieStore loopjCookieStore = new PersistentCookieStore(context);
        asyncHttpClient.setCookieStore(loopjCookieStore);

        return asyncHttpClient;
    }

    static private JSONObject createJSONBase() {
        JSONObject json = new JSONObject();
        try {
            json.put("api_token", ApiToken.TOKEN);
        } catch (JSONException e) {
            //impossible to reach
            Log.d("GetBasicJSON", "Generating error");
        }
        return json;
    }

    static public JSONObject getBasicJSON() {
        return createJSONBase();
    }

    static private String getSessionToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences(WebApi.FILE, Context.MODE_PRIVATE);
        return sp.getString(WebApi.SESSION_TOKEN, "");
    }

    static private JSONObject getJSONWithSession(String token) {
        JSONObject json = createJSONBase();
        try {
            json.put("session_token", token);
        } catch (JSONException e) {
            Log.e("JSON", "getBasicJSONWithSession() put session token to json string error - impossible");
        }
        return json;
    }

    static public JSONObject getJSONWithSession(Context context) {
        if (MOCK_SESSIONS) {
            return getJSONWithSession(MOCK_SESSION_TOKEN);
        } else {
            return getJSONWithSession(getSessionToken(context));
        }
    }

    static public StringEntity getStringEntity(JSONObject json) {
        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(json.toString());
        } catch (UnsupportedEncodingException e) {
            Log.d("JSON", "getStringEntity error:" + e.toString());
        }
        return stringEntity;
    }

    static public void post(Context context, AsyncHttpClient asyncHttpClient, String action, JSONObject json, int timeout, ResponseHandlerInterface responseHandlerInterface) {
        asyncHttpClient.setConnectTimeout(timeout / 2);
        asyncHttpClient.setTimeout(timeout);

        StringEntity stringEntity = AlcoAPI.getStringEntity(json);

        asyncHttpClient.post(context, APICfg.API_URL + action, stringEntity, null, responseHandlerInterface);
    }
}
