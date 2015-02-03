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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.codesharks.alcohol.R;
import pl.codesharks.alcohol.Utils;
import pl.codesharks.alcohol.activity.base.ThemeActivity;
import pl.codesharks.alcohol.dialog.UpdaterDialog;
import pl.codesharks.alcohol.preferences.Main;
import pl.codesharks.alcohol.preferences.WebApi;

public class PrefsActivity extends SherlockPreferenceActivity {

    public static final String TAG = "Prefs Activity";
    SharedPreferences sharedPreferences;
    SharedPreferences accountSharedPreferences;
    PreferenceCategory prefcat_account;
    @Nullable
    Preference pref_update;
    @Nullable
    Preference pref_about_app;
    @Nullable
    Preference pref_changeLog;
    @Nullable
    Preference pref_version;
    @Nullable
    Preference pref_reportIssue;
    @Nullable
    Preference pref_profile;
    Preference pref_logout_clone;
    @Nullable
    Preference pref_logout;
    @NotNull
    Context context = this;
    int hack_counter = 0;
    boolean isLogged = false;
    Preference pref_donate;


    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeActivity.setThemeToHackerStyle(context);

        super.onCreate(savedInstanceState);

        /* Changing the target file for settings
        *=======================================*/
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(Main.FILE);
        prefMgr.setSharedPreferencesMode(MODE_PRIVATE);

        addPreferencesFromResource(R.xml.preference);


        sharedPreferences = getSharedPreferences(Main.FILE, MODE_PRIVATE);

        accountSharedPreferences = getSharedPreferences(WebApi.FILE, MODE_PRIVATE);
        isLogged = accountSharedPreferences.getBoolean(WebApi.LOGGED, false);
        prefcat_account = (PreferenceCategory) findPreference("account");

        {
            pref_profile = findPreference("profile");
            pref_update = findPreference("Update");
            pref_about_app = findPreference("about");
            pref_donate = findPreference("donate");
            pref_changeLog = findPreference("ChangeLog");
            pref_version = findPreference("Version");
            pref_reportIssue = findPreference("reportIssue");
            pref_logout = findPreference("logout");
            pref_logout_clone = pref_logout;
        }

        pref_logout.setShouldDisableView(true);
        {
            pref_profile.setIntent(new Intent(context, ProfileViewActivity.class));
            pref_about_app.setIntent(new Intent(context, AboutActivity.class));
            pref_reportIssue.setIntent(new Intent(context, ReportBugActivity.class));
            String donate_url = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=C6JJP9JE5BUEQ";
            pref_donate.setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(donate_url)));
        }
        pref_update.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (Utils.isConnected(context)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(R.string.are_you_sure);
                    alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new UpdaterDialog.Updater(context).execute();
                        }
                    });
                    alertDialog.setNegativeButton(android.R.string.no, null);
                    alertDialog.show();
                } else {
                    Toast.makeText(context, getString(R.string.no_internet), Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        pref_changeLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder changeLog = new AlertDialog.Builder(context);
                changeLog.setMessage(Utils.readResource(context, R.raw.changelog, "\n"))
                        .setTitle(R.string.pref_changelog).setCancelable(false);
                changeLog.setPositiveButton(android.R.string.ok, null);
                changeLog.create().show();
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

                        pref_logout.setEnabled(false);
                    }
                });
                builder.setNegativeButton(R.string.nope, null);
                builder.show();

                return true;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        isLogged = accountSharedPreferences.getBoolean(WebApi.LOGGED, false);

        /* Deletes the logout button if the user is not logged,
         * and after login, when not restarting activity, it adds
         * that logout button*/
        if (!isLogged) {
            if (pref_logout != null)
                prefcat_account.removePreference(pref_logout);

        } else if (prefcat_account.findPreference("logout") == null) {
            if (pref_logout != null)
                prefcat_account.addPreference(pref_logout);
        }
        //pref_logout.setEnabled(isLogged);
    }

}

