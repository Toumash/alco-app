package pl.pcd.alcohol.webAPIHelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.Const;
import pl.pcd.alcohol.JSONTransmitter;
import pl.pcd.alcohol.Utils;
import pl.pcd.alcohol.R;
import pl.pcd.alcohol.ui.DB_MAIN_Activity;

public class Reporter extends AsyncTask<String, Void, String> {

    ProgressDialog progressDialog;
    Context context;

    public Reporter(Context context) {
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
        return JSONTransmitter.postJSON(json[0], Const.API.URL_JSON);
    }

    @Override
    protected void onPostExecute(@Nullable String x) {
        String json = Utils.substringBetween(x, "<json>", "</json>");
        if (x != null)
            if (Const.DEBUG) Log.i(DB_MAIN_Activity.TAG, json);
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