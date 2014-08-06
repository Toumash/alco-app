package pl.pcd.alcohol.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.*;
import pl.pcd.alcohol.ui.base.ThemeActivity;

public class RegisterActivity extends ThemeActivity {
    @NotNull
    static public String TAG = "RegisterActivity";
    boolean isLogged = false;
    @NotNull
    Context context = this;
    Button bt_ok;
    EditText et_login, et_email, et_password;
    String username, email, password;
    @NotNull
    View.OnClickListener bt_ok_onClickListener = new View.OnClickListener() {
        @Override
        @SuppressWarnings({"ConstantConditions"})
        public void onClick(View view) {
            username = et_login.getText().toString();
            password = et_password.getText().toString();
            email = et_email.getText().toString();

            if (validateInput(username, password)) {

                register(username, email, password);

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

    @Override
    protected void onCreate(Bundle x) {
        super.onCreate(x);
        setContentViewWithTitle(context, R.layout.activ_register, R.string.register_title);
        bt_ok = (Button) findViewById(R.id.register_bt_ok);
        et_login = (EditText) findViewById(R.id.register_et_login);
        et_email = (EditText) findViewById(R.id.register_et_email);
        et_password = (EditText) findViewById(R.id.register_et_password);

        sharedPreferences = context.getSharedPreferences(Const.Prefs.WEB_API.FILE, MODE_PRIVATE);

        bt_ok.setOnClickListener(bt_ok_onClickListener);
    }

    private boolean validateInput(@NotNull String username, @NotNull String password) {
        if (username.length() < 3) return false;
        if (password.length() < 3) return false;

        return true;
    }


    protected void register(String login, String email, String password) {
        JSONObject data = new JSONObject();
        try {
            data.put("action", Const.API.Actions.REGISTER);
            data.put("login", login);
            data.put("email", email);
            data.put("password", password);
            if (Utils.isConnected(context)) {
                new WebRegister().execute(data.toString());
            } else {
                Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    protected class WebRegister extends AsyncTask<String, Void, String> {
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
            return JSONTransmitter.postJSON(strings[0], Const.API.URL_JSON);
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
                        Log.d(TAG, "Json post result: " + json.toString());

                        if (code.equals(Const.API.LoginResult.OK)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(Const.Prefs.WEB_API.LOGIN, username);
                            editor.putString(Const.Prefs.WEB_API.EMAIL, email);
                            editor.putString(Const.Prefs.WEB_API.PASSWORD, Encryption.encodeBase64(password));
                            editor.commit();
                        }

                        AlertDialog.Builder alert = new AlertDialog.Builder(context);

                        String alert_content = "";

                        if (code.equals(Const.API.LoginResult.OK)) {
                            alert_content = getString(R.string.registration_successfull);
                            isLogged = true;
                        } else if (code.equals(Const.API.LoginResult.LOGIN_PASSWORD)) {
                            alert_content = getString(R.string.login_invalid_data);
                        } else/* if (code.equals(Const.API.LoginResult.ERROR)) */ {
                            alert_content = getString(R.string.error);
                        }

                        alert.setTitle(alert_content);
                        JSONArray nfoArray = null;
                        try {
                            nfoArray = json.getJSONArray("nfo");
                        } catch (JSONException e) {
                            Log.i(TAG, e.toString());
                        }
                        if (nfoArray != null) {
                            String info = "";
                            for (int i = 0; i < nfoArray.length(); i++) {
                                String fo = nfoArray.getString(i);
                                if (fo.equals("EMAIL_IN_USE")) {
                                    info = getString(R.string.email_in_use);
                                    break;
                                }
                                info += fo;
                                info += '\n';

                            }
                            alert.setMessage(info);
                        }

                        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (isLogged) {
                                    RegisterActivity.this.finish();
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
