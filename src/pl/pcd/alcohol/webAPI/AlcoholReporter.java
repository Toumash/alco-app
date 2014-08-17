package pl.pcd.alcohol.webapi;

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