package pl.pcd.alcohol.webAPIHelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.*;

public class WebLogin extends AsyncTask<Void, Void, String> {
    public boolean isLogged = false;
    public int result = 2;
    public boolean completed = false;
    public String TAG;
    protected SharedPreferences sharedPreferences;
    protected String username, password, encryptedPassword;
    protected Context context;
    protected JSONObject request;
    protected ProgressDialog progressDialog;

    public WebLogin(@NotNull Context context, String TAG) {
        super();
        this.context = context;
        this.TAG = TAG;
        this.sharedPreferences = context.getSharedPreferences(Const.Prefs.WEB_API.FILE, Context.MODE_PRIVATE);
        this.username = this.sharedPreferences.getString(Const.Prefs.WEB_API.LOGIN, "");
        this.password = Encryption.decodeBase64(this.sharedPreferences.getString(Const.Prefs.WEB_API.PASSWORD, ""));
        this.encryptedPassword = this.sharedPreferences.getString(Const.Prefs.WEB_API.PASSWORD, "");
        this.request = new JSONObject();

        try {
            this.request.put("action", Const.API.Actions.LOGIN);
            this.request.put("login", this.username);
            this.request.put("password", this.password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    protected String doInBackground(Void... strings) {
        return JSONTransmitter.postJSON(this.request.toString(), Const.API.URL_JSON);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(context.getString(R.string.loggin_in));
        progressDialog.show();
    }

    protected void onPostExecute(@Nullable String r) {
        if (r != null) {
            String result = Utils.substringBetween(r, "<json>", "</json>");
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    String code = json.getString("result");
                    //Toast.makeText(context, "RESULT:" + code, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Json post result: " + json.toString());


                    if (code.equals(Const.API.LoginResult.OK)) this.result = Result.OK;
                    else if (code.equals(Const.API.LoginResult.LOGIN_PASSWORD)) this.result = Result.LOGIN_PASSWORD;
                    else this.result = Result.ERROR;

                    if (code.equals(Const.API.LoginResult.OK)) {
                        isLogged = true;
                    }

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
        this.completed = true;
        this.progressDialog.dismiss();
    }

    public final class Result {
        public static final int OK = 0;
        public static final int LOGIN_PASSWORD = 1;
        public static final int ERROR = 2;
    }
}
