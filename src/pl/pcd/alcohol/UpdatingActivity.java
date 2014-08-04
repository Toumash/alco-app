package pl.pcd.alcohol;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class UpdatingActivity extends TitleActivity {
    @Override
    protected void onCreate(Bundle x) {
        super.onCreate(x);
        if (Utils.isConnected(getApplicationContext())) {
            SharedPreferences sharedPreferences = getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);
            if (Const.DEBUG)
                Log.d("Updater", sharedPreferences.getBoolean(Const.Prefs.Main.AUTO_UPDATE, true) ? "AutoUpdater < ON >" : "AutoUpdater < OFF >");
            if (sharedPreferences.getBoolean(Const.Prefs.Main.AUTO_UPDATE, true)) {
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                long date = Long.valueOf(year + "" + "" + month + "" + day);
                if (sharedPreferences.getLong(Const.Prefs.Main.LAST_UPDATE_CHECK, 000) < date) {
                    if (Const.DEBUG) Log.i("Updater", "Checking for updates");
                    new UpdateChecker(UpdatingActivity.this, date).execute();

                } else {
                    if (Const.DEBUG) Log.i("Updater", "Update is not necessary");
                }
            }
        } else {
            if (Const.DEBUG) Log.d("Updater", "Not connected");
        }
    }


    public class UpdateChecker extends AsyncTask<Void, Integer, Void> {
        public String description = "";
        Context context;
        SharedPreferences sharedPreferences;
        long date;


        public UpdateChecker(Context context, long date) {
            super();
            this.context = context;
            sharedPreferences = getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);
            this.date = date;
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... integers) {
            if (isUpdateAvailable()) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(this.context);
                alert.setTitle(R.string.update_title);
                alert.setMessage(getString(R.string.update_changelog) + ":\n" + description);
                alert.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Const.DEBUG) Log.d("Updater", "firing update");
                        new Updater(context).execute();
                    }
                });
                alert.setNegativeButton(R.string.no_thanks, null);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alert.show();
                    }
                });
            } else {
                Log.d("Updater", "no update available");
            }
            return null;
        }

        protected boolean isUpdateAvailable() {
            PackageInfo pInfo = null;
            try {
                //noinspection ConstantConditions
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Updater", e.toString());
            }
            int currentVersion = (pInfo != null) ? pInfo.versionCode : 0;
            try {
                JSONObject json = new JSONObject(JSONTransmitter.downloadWebsite(Const.API.URL_VERSION));
                this.description = json.getString("info");
                sharedPreferences.edit().putLong(Const.Prefs.Main.LAST_UPDATE_CHECK, this.date).commit();
                return json.getInt("version") > currentVersion;
            } catch (JSONException e) {
                Log.e("Updater", e.toString());
            }
            return false;
        }
    }

    public class Updater extends AsyncTask<Void, Integer, Void> {
        SharedPreferences sharedPreferences;
        ProgressDialog progressDialog;
        Context context;

        public Updater(@NotNull Context context) {
            this.context = context;
            this.sharedPreferences = getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);
            this.progressDialog = new ProgressDialog(context);
            {
                this.progressDialog.setIndeterminate(false);
                this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                this.progressDialog.setMessage(getString(R.string.updating));
            }
        }

        @Override
        protected void onPreExecute() {
            this.progressDialog.show();
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... voids) {
            updateFromUrl(Const.API.URL_UPDATE);
            return null;
        }

        public void updateFromUrl(String apkUrl) {

            try {
                //TODO:ProgressBarNeeded
                URL url = new URL(apkUrl);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setConnectTimeout(3000);
                c.setReadTimeout(20000);
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();
                int lengthOfFile;
                try {
                    lengthOfFile = Integer.parseInt(c.getHeaderField("C-L"));//;c.getContentLength(); - not working. CUSTOM HEADER. SEE server update.php
                } catch (NumberFormatException e) {
                    lengthOfFile = -1;
                }

                File outputFile = new File(context.getExternalFilesDir(null), "UPDATE.apk");
                if (outputFile.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    outputFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();
                long total = 0;
                byte[] buffer = new byte[8192];
                int len1;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                    total += len1;
                    publishProgress((int) ((total * 100) / lengthOfFile));
                }
                fos.flush();
                is.close();
                fos.close();
                publishProgress(100);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
                context.startActivity(intent);
                sharedPreferences.edit().putBoolean(Const.Prefs.FIRST_RUN, true).commit();
            } catch (IOException e) {
                Log.d("Updater", e.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        @Override
        protected void onProgressUpdate(Integer... x) {
            super.onProgressUpdate();
            this.progressDialog.setProgress(x[0]);


        }

        @Override
        protected void onPostExecute(Void x) {
            this.progressDialog.dismiss();
            this.progressDialog.cancel();
        }
    }
}
