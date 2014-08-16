package pl.pcd.alcohol.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.*;
import pl.pcd.alcohol.activity.base.ThemeActivity;

public class ProfileViewActivity extends ThemeActivity {

    private static final int REQUEST_LOGIN = 2;
    @NotNull
    public String TAG = "ProfileViewActivity";
    @NotNull
    Context context = this;
    SharedPreferences sharedPreferences;
    TextView tv_login, tv_weight, tv_sex, tv_email, tv_rat_count;
    LinearLayout linearLayout;
    @Nullable
    ProfileDataDownloader profileDataDownloader = null;
    /**
     * prevents multiple ProgressBars. If you know better solution please solve it ; )
     */
    int lastProgressBarId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithTitle(context, R.layout.activ_profile_view, R.string.profile_title);


        sharedPreferences = getSharedPreferences(Const.Prefs.WEB_API.FILE, MODE_PRIVATE);

        tv_login = (TextView) findViewById(R.id.profile_view_login);
        tv_weight = (TextView) findViewById(R.id.profile_view_weight);
        tv_sex = (TextView) findViewById(R.id.profile_view_sex);
        tv_email = (TextView) findViewById(R.id.profile_view_email);
        tv_rat_count = (TextView) findViewById(R.id.profile_view_tv_ratings_count);
        linearLayout = (LinearLayout) findViewById(R.id.profile_view_linear);

        if (!sharedPreferences.getBoolean(Const.Prefs.WEB_API.LOGGED, false)) {
            Intent loginIntent = new Intent(context, LoginActivity.class);
            startActivityForResult(loginIntent, REQUEST_LOGIN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sharedPreferences.getBoolean(Const.Prefs.WEB_API.LOGGED, false)) {
            return;
        }
        updateData();
        if (Utils.isConnected(context)) {
            profileDataDownloader = new ProfileDataDownloader();
            profileDataDownloader.execute();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "User canceled logging in");
                finish();
            } else {
                Log.i(TAG, "User logged in");
            }
        }
    }

    protected void updateData() {
        tv_login.setText(sharedPreferences.getString(Const.Prefs.WEB_API.LOGIN, ""));
        tv_email.setText(sharedPreferences.getString(Const.Prefs.WEB_API.EMAIL, ""));
        tv_rat_count.setText(String.valueOf(sharedPreferences.getInt(Const.Prefs.WEB_API.RATINGS_COUNT, 0)));

        if (sharedPreferences.getFloat(Const.Prefs.WEB_API.WEIGHT, -1) == -1) {
            tv_weight.setText(getString(R.string.profile_no_data));
        } else {
            tv_weight.setText(String.valueOf(sharedPreferences.getFloat(Const.Prefs.WEB_API.WEIGHT, -1)) + " kg");
        }
        //sex
        {
            int rIdSex;
            switch (sharedPreferences.getInt(Const.Prefs.WEB_API.SEX, -1)) {
                case 0:
                    rIdSex = R.string.profile_female;
                    break;
                case 1:
                    rIdSex = R.string.profile_male;
                    break;
                default:
                    rIdSex = R.string.profile_no_data;
            }
            tv_sex.setText(getString(rIdSex));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.profile_view, menu);
        getSupportMenuInflater().inflate(R.menu.profile_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile_refresh:
                if (Utils.isConnected(context)) {
                    profileDataDownloader = new ProfileDataDownloader();
                    profileDataDownloader.execute();
                } else {
                    Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    protected class ProfileDataDownloader extends AsyncTask<Void, Void, String> {
        @Nullable
        ProgressBar progressBar = null;
        String request;

        public ProfileDataDownloader() {
            super();
            JSONObject rQ = new JSONObject();
            try {
                rQ.put("action", Const.API.Actions.PROFILE_DOWNLOAD);
                rQ.put("login", sharedPreferences.getString(Const.Prefs.WEB_API.LOGIN, ""));
                rQ.put("password", Encryption.decodeBase64(sharedPreferences.getString(Const.Prefs.WEB_API.PASSWORD, "")));

            } catch (JSONException e) {
                Log.d(TAG, e.toString());
            }
            this.request = rQ.toString();

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (findViewById(lastProgressBarId) == null) {
                progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
                progressBar.setIndeterminate(true);
                lastProgressBarId++;
                progressBar.setId(lastProgressBarId);

                progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                linearLayout.addView(progressBar, 0);
            }
        }

        @Nullable
        @Override
        protected String doInBackground(Void... z) {
            return JSONTransmitter.postJSON(this.request, Const.API.URL_JSON);
        }

        protected void onPostExecute(@Nullable String x) {
            super.onPostExecute(x);

            if (progressBar != null)
                linearLayout.removeView(progressBar);

            if (x != null) {
                try {
                    JSONObject result = new JSONObject(Utils.substringBetween(x, "<json>", "</json>"));
                    if (result.getString("result").equals("ok")) {

                        JSONObject profile = result.getJSONObject("profile");
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(Const.Prefs.WEB_API.SEX, profile.getInt("sex"));
                        editor.putFloat(Const.Prefs.WEB_API.WEIGHT, Float.parseFloat(profile.getString("weight")));
                        editor.putString(Const.Prefs.WEB_API.EMAIL, profile.getString("email"));
                        editor.putInt(Const.Prefs.WEB_API.RATINGS_COUNT, profile.getInt("rat_count"));
                        editor.commit();
                        if (Cfg.DEBUG) Log.d("PROFILE", profile.toString());
                        updateData();
                    } else {
                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
