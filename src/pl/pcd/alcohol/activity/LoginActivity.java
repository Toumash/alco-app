package pl.pcd.alcohol.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.*;
import pl.pcd.alcohol.activity.base.ThemeActivity;


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
    @NotNull
    View.OnClickListener bt_ok_onClickListener = new View.OnClickListener() {
        @Override
        @SuppressWarnings({"ConstantConditions"})
        public void onClick(View view) {
            username = et_username.getText().toString();
            password = et_password.getText().toString();

            if (validateInput(username, password)) {
                loginToServer(username, password);
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setMessage(getString(R.string.login_invalid_data));
                alert.setPositiveButton(android.R.string.ok, null);
                alert.show();
            }
        }
    };
    SharedPreferences sharedPreferences;
    ProgressDialog pd_connecting;

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

        sharedPreferences = context.getSharedPreferences(Const.Prefs.WEB_API.FILE, MODE_PRIVATE);

        bt_ok.setOnClickListener(bt_ok_onClickListener);
        bt_register.setOnClickListener(bt_register_onClickListener);
        if (sharedPreferences.getBoolean(Const.Prefs.WEB_API.LOGGED, false)) {
            et_username.setText(sharedPreferences.getString(Const.Prefs.WEB_API.LOGIN, ""));
            et_password.setText(Encryption.decodeBase64(sharedPreferences.getString(Const.Prefs.WEB_API.PASSWORD, "")));
        }
    }

    protected void loginToServer(String username, @NotNull String password) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", Const.API.Actions.LOGIN);
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
    }

    private class WebLogin extends AsyncTask<String, Void, String> {
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
            if (Cfg.DEBUG) Log.i(TAG, "sent:" + strings[0]);
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

                        if (code.equals(Const.API.LoginResult.OK)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(Const.Prefs.WEB_API.LOGIN, username);
                            editor.putString(Const.Prefs.WEB_API.PASSWORD, Encryption.encodeBase64(password));
                            editor.putBoolean(Const.Prefs.WEB_API.LOGGED, true);
                            editor.commit();
                        }

                        AlertDialog.Builder alert = new AlertDialog.Builder(context);

                        String alert_content = "";

                        if (code.equals(Const.API.LoginResult.OK)) {
                            alert_content = getString(R.string.login_successful);
                            isLogged = true;
                        } else if (code.equals(Const.API.LoginResult.LOGIN_PASSWORD))
                            alert_content = getString(R.string.login_invalid_data);
                        else if (code.equals(Const.API.LoginResult.ERROR))
                            alert_content = getString(R.string.error);
                        else if (code.equals(Const.API.LoginResult.ACTIVATION))
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
                        if (Cfg.DEBUG) Log.e(TAG, e.toString());
                    }
                } else {
                    Toast.makeText(context, R.string.server_error, Toast.LENGTH_LONG).show();
                }
                if (Cfg.DEBUG) Log.i(TAG, "Whole response:" + r);
            } else {
                Toast.makeText(context, R.string.network_error, Toast.LENGTH_LONG).show();
            }
        }
    }
}
