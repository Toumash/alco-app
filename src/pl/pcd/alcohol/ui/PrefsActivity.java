package pl.pcd.alcohol.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.pcd.alcohol.Const;
import pl.pcd.alcohol.Utils;
import pl.pcd.alcohol.R;
import pl.pcd.alcohol.TitleActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PrefsActivity extends SherlockPreferenceActivity {

    public static final String TAG = "Prefs Activity";
    SharedPreferences sharedPreferences;
    SharedPreferences accountSharedPreferences;
    @Nullable
    Preference pref_update, pref_changeLog, pref_about, pref_version, pref_reportIssue, pref_profile, pref_logout;
    ProgressDialog pd_Update;
    @NotNull
    Context context = this;
    int hack_counter = 0;
    boolean isUserLogged = false;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
/* TITLE FOR OLD DEVICES
 * ==================== */
/*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
*/

        TitleActivity.setThemeToHackerStyle(context);

        super.onCreate(savedInstanceState);

        /* Changing the target file for settings
        *=======================================*/
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(Const.Prefs.Main.FILE);
        prefMgr.setSharedPreferencesMode(MODE_PRIVATE);


/* TITLE FOR OLD DEVICES
 * ==================== */
/*
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
            ((TextView) findViewById(R.id.tilebar_title)).setText(R.string.settings);

            Log.d("Title Manager", "Title Launched");
        }
*/

/*       CONTENT
 * ================== */
        addPreferencesFromResource(R.xml.preference);


        sharedPreferences = getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);

        accountSharedPreferences = getSharedPreferences(Const.Prefs.WEB_API.FILE, MODE_PRIVATE);
        isUserLogged = accountSharedPreferences.getBoolean(Const.Prefs.WEB_API.LOGGED, false);
        pref_update = findPreference("Update");
        pref_changeLog = findPreference("ChangeLog");
        pref_about = findPreference("AboutUs");
        pref_version = findPreference("Version");
        pref_reportIssue = findPreference("reportIssue");
        pref_profile = findPreference("profile");
        pref_logout = findPreference("logout");

        pref_reportIssue.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
/*                String url = "https://bitbucket.org/code-sharks/alcohol-site/issues";
                if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
                Intent lastProgressBarId = new Intent(Intent.ACTION_VIEW);
                lastProgressBarId.setData(Uri.parse(url));
                startActivity(lastProgressBarId);*/
                Intent x = new Intent(context, ReportBugActivity.class);
                startActivity(x);
                return true;
            }
        });

        pref_about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = Const.API.URL_ABOUT;
                // USEFUL: if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
            }
        });

        pref_changeLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder changeLog = new AlertDialog.Builder(context);
                changeLog.setMessage(Utils.readResource(context, R.raw.changelog, false, "\n"))
                        .setTitle(R.string.pref_changelog).setCancelable(false);
                changeLog.setPositiveButton(android.R.string.ok, null);
                changeLog.create().show();
                return true;
            }
        });

        pref_update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (Utils.isConnected(context)) {
                    new Updater(context).execute();

                } else {
                    Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        pref_version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (hack_counter == 4) {
                    if (sharedPreferences.getBoolean("hacker_style", false)) {
                        sharedPreferences.edit().putBoolean("hacker_style", false).commit();
                        Toast.makeText(context, android.R.string.ok, Toast.LENGTH_SHORT).show();
                    } else {
                        sharedPreferences.edit().putBoolean("hacker_style", true).commit();
                        final AlertDialog.Builder builder;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            builder = new AlertDialog.Builder(context, android.R.style.Theme_Holo_Dialog);
                        } else {
                            builder = new AlertDialog.Builder(context);
                        }
                        ImageView imageView = new ImageView(context);
                        imageView.setImageResource(R.drawable.hacked);
                        //imageView.setAdjustViewBounds(true);
                        builder.setView(imageView);
                        builder.setCancelable(false);
                        final AlertDialog alertDialog_hacked = builder.create();
                        alertDialog_hacked.show();
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialog_hacked.dismiss();
                            }
                        });
                    }
                }
                hack_counter++;
                return true;
            }
        });
        pref_logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.prefs_logout_do_you_dare);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        accountSharedPreferences.edit().clear().commit();
                    }
                });
                builder.setNegativeButton(R.string.nope, null);
                builder.show();

                return true;
            }
        });
        pref_profile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent x = new Intent(context, ProfileViewActivity.class);
                startActivity(x);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isUserLogged = accountSharedPreferences.getBoolean(Const.Prefs.WEB_API.LOGGED, false);
        //noinspection ConstantConditions
        pref_logout.setEnabled(isUserLogged);
    }

    class Updater extends AsyncTask<Void, Integer, Void> {
        SharedPreferences sharedPreferences;
        ProgressDialog progressDialog;
        Context context;
        File outputFile;

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
                publishProgress(100);


                //sharedPreferences.edit().putBoolean(Const.Prefs.FIRST_RUN, true).commit();
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
            super.onPostExecute(x);
            this.progressDialog.dismiss();
            this.progressDialog.cancel();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
            context.startActivity(intent);
            finish();
/*            setResult(900);    //900 is the self destruct code.
            finish();*/
        }
    }
}

