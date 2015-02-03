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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import pl.codesharks.alcohol.Config;
import pl.codesharks.alcohol.Installation;
import pl.codesharks.alcohol.R;
import pl.codesharks.alcohol.Utils;
import pl.codesharks.alcohol.activity.base.ThemeActivity;
import pl.codesharks.alcohol.alcoapi.*;
import pl.codesharks.alcohol.exception.AlcoAPIException;
import pl.codesharks.alcohol.preferences.WebApi;

import java.io.UnsupportedEncodingException;


public class LoginActivity extends ThemeActivity {

    @NotNull
    static public String TAG = "LoginActivity";
    boolean isLogged = false;
    @NotNull
    Context context = this;
    @NotNull
    View.OnClickListener bt_register_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent registerIntent = new Intent(context, RegisterActivity.class);
            startActivity(registerIntent);
        }
    };
    Button bt_ok, bt_register;
    EditText et_username, et_password;
    String username, password;
    SharedPreferences sharedPreferences;
    ProgressDialog pd_connecting;
    AsyncHttpClient asyncHttpClient;

    private boolean validateInput(@NotNull String username, @NotNull String password) {
        if (username.length() < 3) return false;
        if (password.length() < 3) return false;

        return true;
    }

    @Override
    protected void onCreate(Bundle onSavedInstanceState) {
        super.onCreate(onSavedInstanceState);

        setContentViewWithTitle(context, R.layout.activ_login, R.string.login_activity);

        bt_ok = (Button) findViewById(R.id.login_bt_ok);
        bt_register = (Button) findViewById(R.id.login_bt_register);
        et_username = (EditText) findViewById(R.id.login_et_username);
        et_password = (EditText) findViewById(R.id.login_et_password);

        sharedPreferences = context.getSharedPreferences(WebApi.FILE, MODE_PRIVATE);

        bt_ok.setOnClickListener(new onLoginClickListener());
        bt_register.setOnClickListener(bt_register_onClickListener);
        if (sharedPreferences.getBoolean(WebApi.LOGGED, false)) {
            et_username.setText(sharedPreferences.getString(WebApi.LOGIN, ""));
        }

        this.asyncHttpClient = AlcoAPI.getAsyncHttpClient(context);
    }

    void login(String username, String password) {
        if (Utils.isConnected(context)) {
            loginToAlcoAPI(username, password);
        } else {
            Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show();
        }

    }

/*    protected void loginToServer(String username, @NotNull String password) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", Action.LOGIN);
            //on web server everything is login, not username
            data.put("login", username);
            data.put("password", password);
            if (Utils.isConnected(context)) {
                new WebLogin().execute(data.toString());
            } else {
                Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }*/

    void loginToAlcoAPI(final String username, final String password) {
        pd_connecting = new ProgressDialog(context);
        pd_connecting.setMessage(getString(R.string.connecting));
        pd_connecting.setIndeterminate(true);
        pd_connecting.show();

        JSONObject json = AlcoAPI.getBasicJSON();
        try {
            json.put("login", username);
            // json.put("password", Encryption.md5Hash(password));
            //FIXME:Enable md5 hash sending
            json.put("password", (password));
            json.put("install_id", Installation.id(context));
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

        StringEntity entity = null;
        try {
            entity = new StringEntity(json.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.toString());
        }


        asyncHttpClient.post(context, APICfg.API_URL + NewActions.LOGIN, entity, null, new AlcoHttpResponseHandler() {
            @Override
            public void onOK(int i, Header[] headers, JSONObject json) {
                if (json == null) {
                    Log.e(TAG, "Login error. NULL JSON RESPONSE");
                    return;
                }
                try {
                    String result = json.getString("result");

                    //Toast.makeText(context, "RESULT:" + result, Toast.LENGTH_LONG).show();
                    if (Config.DEBUG) Log.d(TAG, "Json post result: " + json.toString());


                    AlertDialog.Builder alert = new AlertDialog.Builder(context);

                    String alert_content = "";


                    if (result.equals(ApiResult.RESULT_OK)) {
                        String session_token = json.getString("session_token");
                        if (session_token != null) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(WebApi.SESSION_TOKEN, session_token);
                            editor.putBoolean(WebApi.LOGGED, true);
                            editor.commit();

                            if (Config.DEBUG)
                                Toast.makeText(context, "session_token: " + session_token, Toast.LENGTH_SHORT).show();
                            if (Config.DEBUG)
                                Log.d(TAG, "session_token: " + session_token);

                            isLogged = true;
                            alert_content = getString(R.string.login_successful);
                        } else {
                            throw new AlcoAPIException("No session_token");
                        }
                    } else {
                        String error = json.getString("error_info");
                        if (error.equals(ApiResult.INVALID_LOGIN))
                            alert_content = getString(R.string.login_invalid_data);
                        else if (error.equals(ApiResult.ACCOUNT_NOT_ACTIVATED))
                            alert_content = getString(R.string.activate_your_accout);
                        else //if (error.equals(ApiResult.BAD_REQUEST))
                            alert_content = getString(R.string.error);

                        Log.d(TAG, "Error_info:" + error);
                    }


                    if (isLogged) {
                        Log.i(TAG, "Successfully logged in. Finishing login activity");
                        Toast.makeText(context, android.R.string.ok, Toast.LENGTH_LONG).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        alert.setMessage(alert_content);
                        alert.setCancelable(true);
                        alert.setNegativeButton(android.R.string.ok, null);
                        alert.show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, e.toString());
                } catch (AlcoAPIException e) {
                    Toast.makeText(context, R.string.server_error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, e.toString());
                }
            }

            @Override
            public void onFail(int i, Header[] headers, JSONObject json, Throwable throwable) {
                if (json != null)
                    Log.e(TAG, json.toString() + "");
                Log.e(TAG, throwable.toString());
                Toast.makeText(context, R.string.login_invalid_data, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(int httpCode) {
                pd_connecting.cancel();
            }
        });

    }

    class onLoginClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            username = et_username.getText().toString();
            password = et_password.getText().toString();

            if (validateInput(username, password)) {
                login(username, password);
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setMessage(getString(R.string.login_invalid_data));
                alert.setPositiveButton(android.R.string.ok, null);
                alert.show();
            }
        }
    }

   /* private class WebLogin extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            pd_connecting = new ProgressDialog(context);
            pd_connecting.setMessage(getString(R.string.connecting));
            pd_connecting.setIndeterminate(true);
            pd_connecting.show();

        }

        @Nullable
        @Override
        protected String doInBackground(String... strings) {
            if (Config.DEBUG) Log.i(TAG, "sent:" + strings[0]);
            return JSONTransmitter.postJSON(strings[0], Const.API.URL_JSON, 7000, 10000);
        }

        protected void onPostExecute(@Nullable String r) {
            pd_connecting.cancel();
            if (r != null) {
                String result = Utils.substringBetween(r, "<json>", "</json>");
                if (result != null) {
                    try {
                        JSONObject json = new JSONObject(result);
                        String code = json.getString("result");
                        //Toast.makeText(context, "RESULT:" + code, Toast.LENGTH_LONG).show();
                        Log.i(TAG, "Json post result: " + json.toString());

                        if (code.equals(ApiResult.OK)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(WebApi.LOGIN, username);
                            editor.putString(WebApi.PASSWORD, Encryption.encodeBase64(password));
                            editor.putBoolean(WebApi.LOGGED, true);
                            editor.commit();
                        }

                        AlertDialog.Builder alert = new AlertDialog.Builder(context);

                        String alert_content = "";

                        if (code.equals(ApiResult.OK)) {
                            alert_content = getString(R.string.login_successful);
                            isLogged = true;
                        } else if (code.equals(ApiResult.LOGIN_PASSWORD))
                            alert_content = getString(R.string.login_invalid_data);
                        else if (code.equals(ApiResult.ERROR))
                            alert_content = getString(R.string.error);
                        else if (code.equals(ApiResult.ACTIVATION))
                            alert_content = getString(R.string.activate_your_accout);

                        if (isLogged) {
                            Log.i(TAG, "Successfully logged in. Finishing login activity");
                            Toast.makeText(context, android.R.string.ok, Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            alert.setMessage(alert_content);
                            alert.setCancelable(true);
                            alert.setNegativeButton(android.R.string.ok, null);
                            alert.show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
                        if (Config.DEBUG) Log.e(TAG, e.toString());
                    }
                } else {
                    Toast.makeText(context, R.string.server_error, Toast.LENGTH_LONG).show();
                }
                if (Config.DEBUG) Log.i(TAG, "Whole response:" + r);
            } else {
                Toast.makeText(context, R.string.network_error, Toast.LENGTH_LONG).show();
            }
        }
    }*/
}
