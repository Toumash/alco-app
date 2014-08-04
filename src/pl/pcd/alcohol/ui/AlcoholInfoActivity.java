package pl.pcd.alcohol.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.*;
import pl.pcd.alcohol.webAPIHelper.RatingsDownloader;
import pl.pcd.alcohol.webAPIHelper.Reporter;
import pl.pcd.alcohol.webAPIHelper.WebLogin;

public class AlcoholInfoActivity extends TitleActivity {
    private static final int NUM_OPINIONS = 3;
    @NotNull
    Context context = this;
    TextView et_name, et_price, et_percent, et_volume, et_type, et_subtype;
    CheckBox cb_deposit;
    Button bt_more;
    RatingBar rb_rate;
    LinearLayout linear_for_ratings;
    @NotNull
    String TAG = "AlcoholInfo";
    long alcoholId;
    DBMain db;
    Resources mRes;
    SharedPreferences sharedPreferences;

    protected void openDB() {
        db = new DBMain(context);
        db.open();
    }

    protected void closeDB() {
        if (db != null)
            db.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeDB();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentViewWithTitle(context, R.layout.activ_alcohol_info, R.string.info);

        sharedPreferences = getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);

        et_name = (TextView) findViewById(R.id.alcoholinfo_et_name);
        et_price = (TextView) findViewById(R.id.alcoholinfo_et_price);
        et_percent = (TextView) findViewById(R.id.alcoholinfo_et_percent);
        et_volume = (TextView) findViewById(R.id.alcoholinfo_et_volume);
        et_type = (TextView) findViewById(R.id.alcoholinfo_et_type);
        et_subtype = (TextView) findViewById(R.id.alcoholinfo_et_subtype);
        bt_more = (Button) findViewById(R.id.alcoholinfo_bt_more);
        cb_deposit = (CheckBox) findViewById(R.id.alcoholinfo_cb_deposit);
        rb_rate = (RatingBar) findViewById(R.id.alcoholinfo_rb_rate);
        // lv_comments = (ListView) findViewById(R.id.alcoholinfo_lv_comments);
        linear_for_ratings = (LinearLayout) findViewById(R.id.alcoholinfo_linear_ratings);

        mRes = context.getResources();
        openDB();

        rb_rate.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float r, boolean b) {
                handleRating(r);
            }
        });

        bt_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MoreRatingsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("id", alcoholId);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        Bundle xtr = getIntent().getExtras();

        if (xtr != null) {
            fillFieldsFromDB(xtr.getLong("id"));
            if (sharedPreferences.getBoolean(Const.Prefs.Main.COMMENTS_AUTO_REFRESH, true)) {
                handleFetchingComments();
            } else {
                if (findViewById(111) != null)
                    linear_for_ratings.removeView(findViewById(111));
                TextView tv = new TextView(context);
                tv.setText(R.string.alcoholinfo_ratings_autoload_off);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                tv.setId(111);
                linear_for_ratings.addView(tv);
            }
        } else {
            finish();
        }
    }

    private void handleFetchingComments() {
        if (Utils.isConnected(context)) {
            RatingsDownloader downloader = new RatingsDownloader(alcoholId, NUM_OPINIONS) {
                @Nullable
                ProgressBar progressBar;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    this.progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
                    this.progressBar.setIndeterminate(true);
                    if (findViewById(111) != null)
                        linear_for_ratings.removeView(findViewById(111));
                    this.progressBar.setId(111);
                    linear_for_ratings.addView(this.progressBar, 0);
                }

                @Override
                protected void onPostExecute(Void x) {
                    super.onPostExecute(x);
                    if (this.result.equals(Const.API.LoginResult.OK)) {
                        linear_for_ratings.removeAllViews();
                        LayoutInflater vi =
                                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        int i = 0;
                        for (Rating rating : ratingList) {
                            View view = vi.inflate(R.layout.rating_item, null);
                            TextView author = (TextView) view.findViewById(R.id.comment_author);
                            TextView content = (TextView) view.findViewById(R.id.comment_content);
                            TextView date = (TextView) view.findViewById(R.id.comment_date);
                            RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating_rating);

                            author.setText(rating.author);
                            content.setText(rating.content);
                            date.setText(rating.date);
                            ratingBar.setRating(rating.rating);
                            view.setId(100 + i);
                            linear_for_ratings.addView(view);
                            i++;
                        }
                        if (ratingList.size() < NUM_OPINIONS) {
                            bt_more.setEnabled(false);
                        } else {
                            bt_more.setEnabled(true);
                        }
                        //lv_comments.setAdapter(new RatingsAdapter(context, ratingList));
                    } else if (this.result.equals("timeout")) {
                        TextView error = new TextView(context);
                        error.setText(R.string.network_error);
                        error.setGravity(Gravity.CENTER_HORIZONTAL);
                        error.setId(111);
                        if (findViewById(111) != null)
                            linear_for_ratings.removeView(findViewById(111));
                        linear_for_ratings.addView(error, 0);
                        bt_more.setEnabled(true);
                    } else if (this.result.equals("no_comments")) {
                        TextView error = new TextView(context);
                        error.setText(R.string.alcoholinfo_no_ratings);
                        error.setGravity(Gravity.CENTER_HORIZONTAL);
                        error.setId(111);
                        if (findViewById(111) != null)
                            linear_for_ratings.removeView(findViewById(111));
                        linear_for_ratings.addView(error, 0);
                        bt_more.setEnabled(false);
                    }
                }
            };
            downloader.execute();
        } else {
            if (findViewById(111) != null)
                linear_for_ratings.removeView(findViewById(111));
            TextView tv = new TextView(context);
            tv.setText(R.string.no_internet);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            tv.setId(111);
            linear_for_ratings.addView(tv, 0);
        }
    }

    private void fillFieldsFromDB(long id) {
        Cursor c = db.getRow(id);

        /*  Do not remove this if statement, I donnt know why, but without it theres exception thrown
            That is strange since db.getRow(long) already moves Cursor c to first position */
        if (c.moveToFirst()) {
            if (Const.DEBUG) Log.d("DB", "Columns Count: " + c.getColumnCount());
            if (Const.DEBUG) Log.d("DB", "Rows Count: " + c.getCount());
            alcoholId = c.getLong(c.getColumnIndexOrThrow(DBMain.KEY_ID_ALC));
            et_name.setText(c.getString(c.getColumnIndexOrThrow(DBMain.KEY_NAME)));
            et_price.setText(String.valueOf(c.getFloat(c.getColumnIndexOrThrow(DBMain.KEY_PRICE)) + "zł"));
            et_percent.setText(String.valueOf(c.getFloat(c.getColumnIndexOrThrow(DBMain.KEY_PERCENT)) + "%"));
            et_volume.setText(String.valueOf(c.getInt(c.getColumnIndexOrThrow(DBMain.KEY_VOLUME)) + "ml"));

            cb_deposit.setChecked(c.getInt(c.getColumnIndexOrThrow(DBMain.KEY_DEPOSIT)) == 1);

            et_type.setText(mRes.getStringArray(R.array.typy)[c.getInt(c.getColumnIndexOrThrow(DBMain.KEY_TYPE))]);

            String[] subtypes;
            int arrayID;
            switch (c.getInt(DBMain.COL_TYPE)) {
                case 0:
                    arrayID = R.array.niskoprocentowe;
                    break;
                case 1:
                    arrayID = R.array.srednioprocentowe;
                    break;
                case 2:
                    arrayID = R.array.wysokoprocentowe;
                    break;
                default:
                    arrayID = R.array.niskoprocentowe;
            }
            subtypes = mRes.getStringArray(arrayID);
            et_subtype.setText(subtypes[c.getInt(c.getColumnIndexOrThrow(DBMain.KEY_SUBTYPE))]);
        }
        c.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //getMenuInflater().inflate(R.menu.alcohol_info, menu);
        getSupportMenuInflater().inflate(R.menu.alcohol_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_alcoholinfo_flag:
                handleReporting();
                break;
            case R.id.menu_alcoholinfo_refresh:
                handleFetchingComments();
                break;
            case R.id.menu_alcoholinfo_settings:
                Intent intent = new Intent(context, PrefsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    protected void handleReporting() {
        new WebLogin(context, TAG) {
            @Override
            protected void onPostExecute(String x) {
                super.onPostExecute(x);
                if (this.result != Result.OK) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivityForResult(intent, Const.REQUEST_LOGIN);
                    Log.d(TAG, "Saved login or/and password incorrect");
                    return;
                } else {
                    final String username = this.username;
                    final String password = this.password;
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);

                    //alert.setTitle(R.string.flag);
                    alert.setTitle(R.string.flag);

                    final EditText input = new EditText(context);
                    input.setLines(6);
                    //input.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    input.setHint(R.string.give_us_additional_info);
                    alert.setView(input);


                    alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            JSONObject json = new JSONObject();
                            try {
                                json.put("login", username);
                                json.put("password", password);
                                json.put("action", Const.API.Actions.FLAG);
                                json.put("id", alcoholId);
                                //noinspection ConstantConditions
                                json.put("content", input.getText().toString());
/*                            JSONArray flags = new JSONArray();
                    flags.put(new JSONObject().put("id",alcoholID).put("info","xddddd"));
                    json.put("flag",flags);*/
                                if (Const.DEBUG) Log.d(TAG, "sent json:\n" + json.toString());
                                new Reporter(context).execute(json.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    alert.setNegativeButton(android.R.string.cancel, null);
                    alert.show();
                }
            }
        }.execute();
    }

    protected void handleRating(final float r) {
        new WebLogin(context, TAG) {
            @Override
            protected void onPostExecute(String x) {
                super.onPostExecute(x);
                if (this.result != Result.OK) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivityForResult(intent, Const.REQUEST_LOGIN);
                    Log.d(TAG, "Saved login or/and password incorrect");
                    return;
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    View dialogLayout = getLayoutInflater().inflate(R.layout.ad_rate, null);
                    alertDialog.setView(dialogLayout);
                    final RatingBar rtBar = (RatingBar) dialogLayout.findViewById(R.id.alcoholinfo_ad_ratingBar);
                    final EditText et_opinion = (EditText) dialogLayout.findViewById(R.id.alcoholinfo_ad_et_opinion);
                    final String login = this.username;
                    final String password = this.password;
                    rtBar.setRating(r);
                    alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int rating = (int) rtBar.getRating();
                            String opinion = et_opinion.getText().toString();
                            JSONObject json = new JSONObject();

                            try {
                                json.put("action", Const.API.Actions.RATE);
                                json.put("login", login);
                                json.put("password", password);
                                json.put("id", alcoholId);
                                json.put("content", opinion);
                                json.put("rate", rating);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            new AsyncTask<String, Void, String>() {
                                ProgressDialog progressDialog;

                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    this.progressDialog = new ProgressDialog(context);
                                    progressDialog.setIndeterminate(true);
                                    progressDialog.setCancelable(false);
                                    progressDialog.setMessage(getString(R.string.sending));
                                    progressDialog.show();
                                }

                                @Nullable
                                @Override
                                protected String doInBackground(String... json) {
                                    return JSONTransmitter.postJSON(json[0], Const.API.URL_JSON);
                                }

                                @Override
                                protected void onPostExecute(@Nullable String s) {
                                    super.onPostExecute(s);
                                    progressDialog.cancel();
                                    if (s != null) {
                                        if (Const.DEBUG) Log.d(TAG, s);
                                        String result = Utils.substringBetween(s, "<json>", "</json>");
                                        JSONObject jsonObject;
                                        try {
                                            jsonObject = new JSONObject(result);
                                            if (jsonObject.getString("result").equals("ok")) {
                                                Toast.makeText(context, android.R.string.ok, Toast.LENGTH_LONG).show();
                                            } else {
                                                Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Toast.makeText(context, R.string.error, Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }.execute(json.toString());
                        }
                    });
                    alertDialog.show();
                }


            }


        }.execute();
    }
}
