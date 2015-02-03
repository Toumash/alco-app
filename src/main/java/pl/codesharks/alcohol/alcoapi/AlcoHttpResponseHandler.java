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

import android.util.Log;
import com.loopj.android.http.TextHttpResponseHandler;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import pl.codesharks.alcohol.Config;
import pl.codesharks.alcohol.Utils;

public abstract class AlcoHttpResponseHandler extends TextHttpResponseHandler {
    /**
     * Always use substring
     */
    @Override
    public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
        JSONObject json = null;
        try {
            if (s != null) {
                String jsonString = this.substring(s);
                if (jsonString != null) {
                    json = new JSONObject(this.substring(s));
                }
                if (Config.DEBUG) Log.d("JSONParsing", "JSON:" + s);
            } else
                throw new JSONException("no response from server");
        } catch (JSONException e) {
            Log.e("JSONParsing", "Critical error, cannot create json from response");
        }

        this.onResponse(i);

        this.onFail(i, headers, json, throwable);

    }

    /**
     * Always use substring
     */
    @Override
    public void onSuccess(int i, Header[] headers, String s) {
        JSONObject json = null;
        try {
            if (s != null) {
                String jsonString = this.substring(s);
                if (jsonString != null) {
                    json = new JSONObject(this.substring(s));
                }
                Log.d("JSONParsing", "JSON:" + s);
            } else
                throw new JSONException("no response from server");
        } catch (JSONException e) {
            //noinspection ConstantConditions
            Log.e("JSONParsing", "Critical error, cannot create json from substring: " + s != null ? s : "");
        }

        this.onResponse(i);

        this.onOK(i, headers, json);
    }

    /**
     * @param json might be null!
     */
    public abstract void onOK(int i, Header[] headers, JSONObject json);

    /**
     * @param json      might be null
     * @param throwable error thrown by the loopjClient
     */
    public abstract void onFail(int i, Header[] headers, JSONObject json, Throwable throwable);

    /**
     * called always before each on
     */
    public abstract void onResponse(int httpCode);

    protected String substring(String s) {
        return Utils.substringBetween(s, "<json>", "</json>");
    }
}
