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

package pl.pcd.alcohol.alcoapi;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.*;
import pl.pcd.alcohol.activity.DB_MAIN_Activity;

public class AlcoholReporter extends AsyncTask<String, Void, String> {

    ProgressDialog progressDialog;
    Context context;

    public AlcoholReporter(Context context) {
        super();
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setMessage(context.getString(R.string.sending));
        this.progressDialog.setCancelable(true);
        this.progressDialog.show();

    }

    @Nullable
    @Override
    protected String doInBackground(String... json) {
        return JSONTransmitter.postJSON(json[0], Const.API.URL_JSON, 7000, 10000);
    }

    @Override
    protected void onPostExecute(@Nullable String x) {
        String json = Utils.substringBetween(x, "<json>", "</json>");
        if (x != null)
            if (Cfg.DEBUG) Log.i(DB_MAIN_Activity.TAG, json);
        this.progressDialog.dismiss();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.getString("result").equals(Const.API.LoginResult.OK)) {
                Toast.makeText(context, android.R.string.ok, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.d("Reporter", e.toString());
        }
    }
}