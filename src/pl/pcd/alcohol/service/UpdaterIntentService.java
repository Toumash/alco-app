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

package pl.pcd.alcohol.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.*;
import pl.pcd.alcohol.dialog.UpdaterDialog;

public class UpdaterIntentService extends IntentService {

    final int NOTIFY_UPDATE_ID = 1945;
    NotificationManager mgr = null;
    Context context = this;
    UpdateChecker updateChecker;

    /**
     * A constructor is required, and must call the super IntentService(String)
     * constructor with a name for the worker thread.
     */
    public UpdaterIntentService() {
        super("UpdaterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Utils.isConnected(context)) {
            updateChecker = new UpdateChecker(context);
            updateChecker.execute();
        } else {
            if (Cfg.DEBUG) Log.d("Updater", "No internet connecion");
        }
    }

    protected void showUpdateNotification(String changelog) {
        Intent rqIntent = UpdaterDialog.createIntent(context, changelog);
        rqIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Notification notification = new Notification(R.drawable.ic_notification, getString(R.string.update_available), System.currentTimeMillis());
        // start notification
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, rqIntent, PendingIntent.FLAG_UPDATE_CURRENT);//|Intent.FLAG_FROM_BACKGROUND);
        //PendingIntent pIntent = PendingIntent.getActivity(this, 0, rqIntent, 0);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.setLatestEventInfo(this,
                getString(R.string.app_name), // TITLE
                getString(R.string.update_available),// SUMMARY
                pIntent);
        mgr.notify(NOTIFY_UPDATE_ID, notification);
    }

    public class UpdateChecker extends AsyncTask<Void, Void, Void> {
        public String description = "";
        Context ctx;
        SharedPreferences sharedPreferences;

        public UpdateChecker(@NotNull Context context) {
            super();
            this.ctx = context;
            sharedPreferences = context.getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);
        }

        @Override
        protected Void doInBackground(Void... integers) {
            if (isUpdateAvailable()) {
                if (Cfg.DEBUG)
                    Log.d("Updater", "Update Available, firing Notification");
                showUpdateNotification(this.description);
            } else {
                //if (Cfg.DEBUG)
                Log.d("Updater", "no update available");
            }
            return null;
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
                // String response  = JSONTransmitter.simpleGetRequest(Const.API.URL_VERSION);
                JSONObject rQ = new JSONObject();
                rQ.put("id", Installation.id(context));
                rQ.put("action", Const.API.Actions.UPDATE);

                String response = JSONTransmitter.postJSON(rQ.toString(), Const.API.URL_JSON, 10000, 15000);
                if (Cfg.DEBUG) Log.d("Updater", "api whole response:" + response);
                response = Utils.substringBetween(response, "<json>", "</json>");
                JSONObject json = new JSONObject(response);
                this.description = json.getString("info");
                // sharedPreferences.edit().putLong(Const.Prefs.Main.LAST_UPDATE_CHECK, this.date).commit();


                //noinspection PointlessBooleanExpression,ConstantConditions
                return Cfg.DEBUG || json.getInt("version") > currentVersion;
            } catch (JSONException e) {
                Log.e("Updater", e.toString());
            }
            return false;
        }
    }
}