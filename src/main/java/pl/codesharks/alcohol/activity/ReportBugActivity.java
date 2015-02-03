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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import pl.codesharks.alcohol.*;
import pl.codesharks.alcohol.activity.base.ThemeActivity;
import pl.codesharks.alcohol.alcoapi.Action;

public class ReportBugActivity extends ThemeActivity {
    Button bt_send;
    EditText et_title, et_description, et_user;
    Spinner sr_type, sr_priority;
    @NotNull
    String TAG = "BugReporter";
    @NotNull
    Context context = this;
    boolean isOk = false;

    @Override
    protected void onCreate(Bundle x) {
        super.onCreate(x);
        setContentViewWithTitle(this, R.layout.activ_report_bug, R.string.pref_report_issue);
        et_title = (EditText) findViewById(R.id.report_et_title);
        et_description = (EditText) findViewById(R.id.report_et_description);
        et_user = (EditText) findViewById(R.id.report_et_user);
        sr_type = (Spinner) findViewById(R.id.report_sr_type);
        sr_priority = (Spinner) findViewById(R.id.report_sr_priority);

        bt_send = (Button) findViewById(R.id.report_bt_send);
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = et_title.getText().toString().trim();
                String description = et_description.getText().toString().trim();
                String user = et_user.getText().toString().trim();
                String kind = sr_type.getSelectedItem().toString();
                String priority = sr_priority.getSelectedItem().toString();

                if (title.length() > 4) {
                    JSONObject request = new JSONObject();
                    try {
                        request.put("action", Action.ISSUE);
                        request.put("title", title);
                        if (request.length() > 0) request.put("description", description);
                        if (user.length() > 0) request.put("user", user);
                        request.put("kind", kind);
                        request.put("priority", priority);
                    } catch (JSONException e) {
                        if (Config.DEBUG) Log.e(TAG, e.toString());
                    }
                    new ReportUploader().execute(request.toString());
                } else {
                    Toast.makeText(context, R.string.report_title_too_short, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    class ReportUploader extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(getString(R.string.connecting));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Nullable
        @Override
        protected String doInBackground(String... strings) {
            return JSONTransmitter.postJSON(strings[0], Const.API.URL_JSON, 7000, 10000);
        }

        protected void onPostExecute(@Nullable String r) {

            progressDialog.cancel();
            if (r != null) {
                String result = Utils.substringBetween(r, "<json>", "</json>");
                if (result != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        String code = json.getString("result");
                        //Toast.makeText(context, "RESULT:" + code, Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Json post result: " + json.toString());


                        AlertDialog.Builder alert = new AlertDialog.Builder(context);

                        alert.setTitle(code);
                        if (code.equals("ok")) {
                            alert.setMessage(R.string.report_thanks_for);
                            isOk = true;
                        }


                        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (isOk) {
                                    ReportBugActivity.this.finish();
                                }
                            }
                        });

                        alert.show();

                    } catch (JSONException e) {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
                        Log.d(TAG, e.toString());
                    }
                } else {
                    Toast.makeText(context, R.string.server_error, Toast.LENGTH_LONG).show();
                }
                Log.d(TAG, "Whole response:" + r);
            } else {
                Toast.makeText(context, R.string.network_error, Toast.LENGTH_LONG).show();
            }
        }
    }
}
