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

package pl.pcd.alcohol.activity.base;

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
import pl.pcd.alcohol.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

@Deprecated
public class UpdatingActivity extends ThemeListActivity {
    @Override
    protected void onCreate(Bundle x) {
        super.onCreate(x);
        final SharedPreferences sharedPreferences = getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);

        if (Utils.isConnected(getApplicationContext())) {

            if (Cfg.DEBUG)
                Log.d("Updater", sharedPreferences.getBoolean(Const.Prefs.Main.AUTO_UPDATE, true) ? "AutoUpdater < ON >" : "AutoUpdater < OFF >");
            if (sharedPreferences.getBoolean(Const.Prefs.Main.AUTO_UPDATE, true)) {
                Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                int hour = c.get(Calendar.HOUR_OF_DAY);
                long date = Long.valueOf(year + "" + "" + month + "" + day + "" + hour);
                if (sharedPreferences.getLong(Const.Prefs.Main.LAST_UPDATE_CHECK, 000) < date) {
                    if (Cfg.DEBUG) Log.i("Updater", "Checking for updates");
                    new UpdateChecker(UpdatingActivity.this, date).execute();

                } else {
                    if (Cfg.DEBUG) Log.i("Updater", "Update is not necessary");
                }
            }
        } else {
            if (Cfg.DEBUG) Log.d("Updater", "Not connected");
        }

        if (!sharedPreferences.getBoolean(Const.Prefs.Main.INSTALLATION_REGISTERED, false)) {
            String id = Installation.id(getApplicationContext());
            JSONObject object = new JSONObject();
            try {
                object.put("action", Const.API.Actions.REGISTER_INSTALLATION);
                object.put("id", id);
            } catch (JSONException e) {
                Log.d("Installation", e.toString());
            }

            new AsyncTask<String, Void, String>() {

                @Nullable
                @Override
                protected String doInBackground(String... json) {
                    return JSONTransmitter.postJSON(json[0], Const.API.URL_JSON, 7000, 10000);
                }

                @Override
                protected void onPostExecute(String x) {
                    super.onPostExecute(x);
                    String result = Utils.substringBetween(x, "<json>", "</json>");
                    if (result != null) {
                        try {
                            JSONObject obj = new JSONObject(result);
                            if (obj.getString("result").equals("ok")) {
                                Log.d("Installation", "Install Reg: < OK >");
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean(Const.Prefs.Main.INSTALLATION_REGISTERED, true);
                                editor.commit();
                            } else {
                                Log.d("Installation", "Install Reg : < ERROR >");
                            }
                        } catch (JSONException e) {
                            Log.d("Installation response", e.toString());
                        }

                    }
                }
            }.execute(object.toString());
        }
    }

    public static class Updater extends AsyncTask<Void, Integer, Void> {
        SharedPreferences sharedPreferences;
        ProgressDialog progressDialog;
        Context context;
        @Nullable
        Runnable error = null;
        File outputFile;

        public Updater(@NotNull Context context) {
            this.context = context;
            this.sharedPreferences = context.getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);
            this.progressDialog = new ProgressDialog(context);
            {
                this.progressDialog.setIndeterminate(false);
                this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                this.progressDialog.setMessage(context.getString(R.string.updating));
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

                outputFile = new File(context.getExternalFilesDir(null), "UPDATE.apk");
                if (outputFile.exists()) {
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
                publishProgress(100);/*
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
                context.startActivity(intent);*/
                sharedPreferences.edit().putBoolean(Const.Prefs.Main.FIRST_RUN, true).commit();
            } catch (IOException e) {
                Log.d("Updater", e.toString());
                error = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
                    }
                };
            }
        }

        @Override
        protected void onProgressUpdate(Integer... x) {
            super.onProgressUpdate();
            this.progressDialog.setProgress(x[0]);


        }

        @Override
        protected void onPostExecute(Void x) {
            if (error != null) error.run();
            this.progressDialog.dismiss();
            this.progressDialog.cancel();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
            context.startActivity(intent);
        }
    }

    public static class UpdateChecker extends AsyncTask<Void, Integer, Void> {
        public String description = "";
        Context ctx;
        SharedPreferences sharedPreferences;
        long date;
        @Nullable
        AlertDialog.Builder alert = null;

        public UpdateChecker(@NotNull Context context, long date) {
            super();
            this.ctx = context;
            sharedPreferences = context.getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);
            this.date = date;
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... integers) {
            if (isUpdateAvailable()) {
                alert = new AlertDialog.Builder(this.ctx);
                alert.setTitle(R.string.update_title);
                alert.setMessage(ctx.getString(R.string.update_changelog) + ":\n" + description);
                alert.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (Cfg.DEBUG) Log.d("Updater", "firing update");
                        new Updater(ctx).execute();
                    }
                });
                alert.setNegativeButton(R.string.no_thanks, null);

            } else {
                Log.d("Updater", "no update available");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void xd) {
            super.onPostExecute(xd);
            if (alert != null)
                alert.show();
        }

        protected boolean isUpdateAvailable() {
            PackageInfo pInfo = null;
            try {
                //noinspection ConstantConditions
                pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Updater", e.toString());
            }
            int currentVersion = (pInfo != null) ? pInfo.versionCode : 0;
            try {
                JSONObject json = new JSONObject(JSONTransmitter.simpleGetRequest(Const.API.URL_VERSION));
                this.description = json.getString("info");
                sharedPreferences.edit().putLong(Const.Prefs.Main.LAST_UPDATE_CHECK, this.date).commit();
                return json.getInt("version") > currentVersion;
            } catch (JSONException e) {
                Log.e("Updater", e.toString());
            }
            return false;
        }
    }
}
