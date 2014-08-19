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

package pl.pcd.alcohol.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.pcd.alcohol.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdaterDialog extends Activity {
    Context context = this;
    AlertDialog alertDialog;

    public static Intent createIntent(Context context, String changelog) {

        Bundle bundle = new Bundle();
        bundle.putString("changelog", changelog);
        Intent intent = new Intent(context, UpdaterDialog.class);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    public void onNewIntent(Intent intent) {
        String changelog = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            changelog = bundle.getString("changelog");
        } else {
            Log.d("Updater", "Bundle error");
        }
        alertDialog.setMessage(context.getString(R.string.update_changelog) + ":\n" + changelog);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String changelog = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            changelog = bundle.getString("changelog");
        } else {
            Log.d("Updater", "Bundle error");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.update_title);
        builder.setMessage(context.getString(R.string.update_changelog) + ":\n" + changelog);
        builder.setCancelable(false);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Cfg.DEBUG) Log.d("Updater", "firing update");
                if (Utils.isConnected(context)) {
                    new Updater(context) {
                        @Override
                        protected void onPostExecute(Integer x) {
                            super.onPostExecute(x);
                            finish();
                        }
                    }.execute();
                } else {
                    Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }

    public static class Updater extends AsyncTask<Void, Integer, Integer> {
        public int R_OK = 0;
        public int R_ERROR = 1;
        SharedPreferences sharedPreferences;    // DLA FIRST RUNA
        ProgressDialog progressDialog;
        Context context;
        @Nullable
        Runnable error = null;
        File outputFile;

        public Updater(@NotNull Context context) {
            this.context = context;
            this.sharedPreferences = context.getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);  //FIRST RUN
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
        protected Integer doInBackground(Void... voids) {
            //noinspection AutoBoxing
            return updateFromUrl(Const.API.URL_UPDATE); // url to update from
        }

        /**
         * @param apkUrl url download
         * @return 0 if good, 1 if error
         */
        protected int updateFromUrl(String apkUrl) {

            try {
                URL url = new URL(apkUrl + "&id=" + Installation.id(context));
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
                    //noinspection AutoBoxing
                    publishProgress((int) ((total * 100) / lengthOfFile));
                }
                fos.flush();
                is.close();
                fos.close();

                //noinspection AutoBoxing
                publishProgress(100);
                sharedPreferences.edit().putBoolean(Const.Prefs.Main.FIRST_RUN, true).commit();

                return R_OK;
            } catch (IOException e) {
                Log.d("Updater", e.toString());
                return R_ERROR;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... x) {
            super.onProgressUpdate();

            //noinspection AutoUnboxing
            this.progressDialog.setProgress(x[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            this.progressDialog.dismiss();
            this.progressDialog.cancel();

            //noinspection AutoUnboxing
            if (result == R_OK) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
                context.startActivity(intent);
                return;
            }
            Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
        }

    }
}