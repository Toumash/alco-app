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

package pl.pcd.alcohol.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.loopj.android.http.AsyncHttpClient;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.Config;
import pl.pcd.alcohol.Const;
import pl.pcd.alcohol.R;
import pl.pcd.alcohol.Utils;
import pl.pcd.alcohol.activity.base.ThemeListActivity;
import pl.pcd.alcohol.alcoapi.*;
import pl.pcd.alcohol.alcoapi.contract.Main_Alcohol;
import pl.pcd.alcohol.database.AlcoholCursorAdapter;
import pl.pcd.alcohol.database.MainDB;
import pl.pcd.alcohol.preferences.Main;
import pl.pcd.alcohol.service.HUDService;
import pl.pcd.alcohol.service.UpdaterIntentService;

import java.io.UnsupportedEncodingException;

public class MainDBActivity extends ThemeListActivity {

    public static final String TAG = "DB_MainActivity";
    @NotNull
    protected Context context = this;
    MainDB db;
    Cursor cursor;
    ListView listView;
    EditText et_search;
    SharedPreferences mainSharedPreferences;
    @Nullable
    CursorAdapter myCursorAdapter;
    LinearLayout linear;
    ProgressDialog pd_updating;
    AsyncHttpClient asyncHttpClient;
    private GestureDetector gestureDetector;

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        super.onCreateOptionsMenu(menu);
        // getMenuInflater().inflate(R.menu.main_list, menu);
        getSupportMenuInflater().inflate(R.menu.main_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_userlist:
                Intent x = new Intent(context, UserDBActivity.class);
                x.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(x);
                break;
            case R.id.menu_main_download:
                if (Utils.isConnected(context))
                    updateDatabase();
                 /*   new DBUpdater().execute(Const.API.URL_MAIN);*/
                else Toast.makeText(context, R.string.no_internet, Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_user_settings:
                Intent s = new Intent(context, PrefsActivity.class);
                s.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(s);
                break;
        }
        return true;
    }

    protected void openDB() {
        db = new MainDB(this);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentViewWithTitle(context, R.layout.activ_main_list, R.string.alcohol_list);

        asyncHttpClient = AlcoAPI.getAsyncHttpClient(context);

        mainSharedPreferences = getSharedPreferences(Main.FILE, MODE_PRIVATE);
        listView = this.getListView();
        linear = (LinearLayout) findViewById(R.id.linearRoot_main_db);
        et_search = (EditText) findViewById(R.id.main_search);

        et_search.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                // Abstract Method of TextWatcher Interface.
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Abstract Method of TextWatcher Interface.
            }

            public void onTextChanged(@NotNull CharSequence s, int start, int before, int count) {
                // Abstract Method of TextWatcher Interface.
                if (myCursorAdapter != null) {
                    myCursorAdapter.getFilter().filter(s.toString());
                }
            }
        });

        openDB();

        listView.setOnItemClickListener(new onAlcoholClickListener());
        registerForContextMenu(listView);

        //showChangeLogOnFirstRun(mainSharedPreferences);

        gestureDetector = new GestureDetector(new SwipeGestureDetector());
        View.OnTouchListener gestureCallback = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, @NotNull MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        };
        linear.setOnTouchListener(gestureCallback);
        listView.setOnTouchListener(gestureCallback);

        if (mainSharedPreferences.getBoolean(Main.AUTO_UPDATE, false)) {
            Intent x = new Intent(this, UpdaterIntentService.class);
            startService(x);
        }


        //HUD displaying
        {
            final Intent intent = new Intent(context, HUDService.class);
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();
            if (mPhoneNumber != null)
                if (mPhoneNumber.equals("796806609")) {
                    if (!HUDService.running) startService(intent);
                    //Thread stopping HUD-display
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            context.stopService(intent);
                        }
                    }).start();
                } else if (mPhoneNumber.equals("516263456")) {
                    if (!HUDService.running) startService(intent);
                }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (listView.getAdapter() == null)
            populateListFromDBAsync();
    }

    @Override
    public boolean onContextItemSelected(@NotNull MenuItem alcohol_item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) alcohol_item.getMenuInfo();
        int menuItemIndex = alcohol_item.getItemId();
        cursor = db.getRow(info.id);

        final int alcoholID = cursor.getInt(MainDB.COL_ID_ALC);
        switch (menuItemIndex) {
            case Const.ContextMenu.MainDB.KEY_FLAG:

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
                                        json.put("action", Action.FLAG);
                                        json.put("id", alcoholID);
                                        //noinspection ConstantConditions
                                        json.put("content", input.getText().toString());
/*                            JSONArray flags = new JSONArray();
                    flags.put(new JSONObject().put("id",alcoholID).put("info","xddddd"));
                    json.put("flag",flags);*/
                                        if (Config.DEBUG) Log.d(TAG, "sent json:\n" + json.toString());
                                        new AlcoholReporter(context).execute(json.toString());
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
                break;
        }
        return true;
    }

    @Deprecated
    private boolean showChangeLogOnFirstRun(@NotNull SharedPreferences sharedPreferences) {
        boolean firstRun = sharedPreferences.getBoolean(Main.FIRST_RUN, true);

        if (firstRun) {
            AlertDialog.Builder changeLog = new AlertDialog.Builder(context);
            changeLog.setMessage(Utils.readResource(context, R.raw.changelog, "\n"))
                    .setTitle(R.string.whatsNew).setCancelable(false);
            changeLog.setPositiveButton(android.R.string.ok, null);
            changeLog.create().show();
            sharedPreferences.edit().putBoolean(Main.FIRST_RUN, false).commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //listView.setAdapter(null);
        //myCursorAdapter = null;
        if (listView.getChildCount() == 0) Log.d(TAG, "Clearing List...<OK>");
        else Log.d(TAG, "Clearing List...<PARTLY OK> Ram not freed until app destroy");
    }

    @Override
    public void onCreateContextMenu(@NotNull ContextMenu menu, @NotNull View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == listView.getId()) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(db.getRow(info.id).getString(MainDB.COL_NAME));
            String[] menuItems = getResources().getStringArray(R.array.menu_main_db);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    private void populateListFromDBAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Populating list...");
                cursor = db.getAllRows(new String[]{MainDB.KEY_ROWID, MainDB.KEY_ID_ALC, MainDB.KEY_NAME, MainDB.KEY_PRICE, MainDB.KEY_VOLUME, MainDB.KEY_PERCENT, MainDB.KEY_TYPE, MainDB.KEY_SUBTYPE}, MainDB.KEY_NAME + " ASC");

                //Setup mapping from cursor to view fields
/*                String[] fromFieldNames = new String[]
                        {DB_MAIN.KEY_ID_ALC, DB_MAIN.KEY_NAME, DB_MAIN.KEY_PRICE, DB_MAIN.KEY_VOLUME, DB_MAIN.KEY_PERCENT};

                int[] toViewIDs = new int[]{R.id.itemNumber, R.id.itemName, R.id.itemPrice, R.id.itemVolume, R.id.itemPercentage};*/

                //noinspection deprecation

                //myCursorAdapter = new SimpleCursorAdapter(context, R.layout.alcohol_item, cursor, fromFieldNames, toViewIDs);
                myCursorAdapter = new AlcoholCursorAdapter(context, cursor, 0) {
                    @Override
                    public void bindView(@NotNull View view, Context context, @NotNull Cursor cursor) {
                        mHolder = (ViewHolder) view.getTag();
                        mHolder.name.setText(cursor.getString(mIndexes.name));
                        //Log.d(TAG, cursor.getString(mIndexes.type));
                        //Log.d(TAG, cursor.getString(mIndexes.subtype));
                        mHolder.percent.setText(cursor.getString(mIndexes.percent));
                        mHolder.volume.setText(cursor.getString(mIndexes.volume));
                        mHolder.price.setText(cursor.getString(mIndexes.price));
                        int type = cursor.getInt(mIndexes.type);
                        int subtype = cursor.getInt(mIndexes.subtype);
                        if (type > mTypes.length - 1) type = 0;

                        mHolder.type.setText(mTypes[type]);
                        String subtypeString = "";
                        switch (type) {
                            case 0:
                                if (subtype > mSubtypesLow.length - 1) subtype = 0;
                                subtypeString = mSubtypesLow[subtype];
                                break;
                            case 1:
                                if (subtype > mSubtypesMedium.length - 1) subtype = 0;
                                subtypeString = mSubtypesMedium[subtype];
                                break;
                            case 2:
                                if (subtype > mSubtypesHigh.length - 1) subtype = 0;
                                subtypeString = mSubtypesHigh[subtype];
                                break;

                        }
                        mHolder.subtype.setText(subtypeString);
                        mHolder.id.setText(cursor.getString(mIndexes.id));
                    }
                };

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listView.setAdapter(myCursorAdapter);
                        myCursorAdapter.notifyDataSetChanged();
                        /* FILTERING function
                        ======================*/
                        myCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                            public Cursor runQuery(@NotNull CharSequence constraint) {
                                Log.d(TAG, "Querying by:" + constraint);

                                return db.filterByName(constraint.toString());
                            }
                        });

                        if (listView.getCount() == 0) {
                            Log.d(TAG, "Populating.. <NO RECORDS>");
                        }
                        Log.d(TAG, "Populating.. <SUCCESS>");
                    }
                });
            }
        }).run();

    }

    void updateDatabase() {

        pd_updating = new ProgressDialog(context);
        pd_updating.setIndeterminate(true);
        pd_updating.setMessage(getString(R.string.updating));
        pd_updating.show();
        asyncHttpClient.setConnectTimeout(10000);
        JSONObject json = new JSONObject();
        try {
            json.put("api_token", ApiToken.TOKEN);
            json.put("session_token", "536ef53d2395e1e8da60208a8a83c716");
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
        }
        StringEntity stringEntity = null;
        try {
            stringEntity = new StringEntity(json.toString());
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, e.toString());
        }

        asyncHttpClient.post(context, APICfg.API_URL + "downloadMainDB", stringEntity, "application/json", new AlcoHttpResponseHandler() {
            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {
                String result = this.substring(s);
                Log.e(TAG, "JSON RESULT: " + result);
                pd_updating.dismiss();
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                String result = this.substring(s);
                JSONObject json = null;
                try {
                    if (result == null) throw new JSONException("no json string");

                    json = new JSONObject(result);
                    new JSONToDBParser().execute(json);
                } catch (JSONException e) {
                    Log.e(TAG, "ERROR parsing result from the server: " + e.toString());
                }
            }
        });

    }

    protected class JSONToDBParser extends AsyncTask<JSONObject, Void, Void> {
        @Override
        protected Void doInBackground(JSONObject... jsonObjects) {
            JSONObject json = jsonObjects[0];
            try {
                db.deleteAll();

                if (json == null) throw new NullPointerException("passed null json to JSONToDBParser");
                JSONArray JA = json.getJSONArray("data");
                JSONObject JO;
                for (int i = 0; i < JA.length(); i++) {
                    JO = JA.getJSONObject(i);

                    Main_Alcohol alc = new Main_Alcohol(JO.getLong(Main_Alcohol.API_ID),
                            JO.getString(Main_Alcohol.API_NAME),
                            (float) JO.getDouble(Main_Alcohol.API_PRICE),
                            JO.getInt(Main_Alcohol.API_TYPE),
                            JO.getInt(Main_Alcohol.API_SUBTYPE),
                            JO.getInt(Main_Alcohol.API_VOLUME),
                            (float) JO.getDouble(Main_Alcohol.API_PERCENT),
                            JO.getInt(Main_Alcohol.API_DEPOSIT));

                    db.insertRow(alc);

                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //noinspection deprecation
                        cursor.requery();
                        Log.d(TAG, "Refreshing listView");

                        if (myCursorAdapter != null) {
                            myCursorAdapter.notifyDataSetChanged();
                        }
                    }
                });
            } catch (JSONException e) {
                Log.d(TAG, e.toString());
                Log.d(TAG, "json response causing error: " + json.toString());
            } catch (NullPointerException e) {
                Log.d(TAG, "server returned no data array");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void x) {
            pd_updating.dismiss();
        }
    }

    class onAlcoholClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Cursor c = db.getRow(id);

            Bundle b = new Bundle();
            b.putLong("id", c.getLong(c.getColumnIndexOrThrow(MainDB.KEY_ROWID)));

            Intent intent = new Intent(context, AlcoholInfoActivity.class);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_MIN_DISTANCE = 90;
        private static final int SWIPE_MAX_OFF_PATH = 200;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onFling(@NotNull MotionEvent e1, @NotNull MotionEvent e2, float velocityX,
                               float velocityY) {
            try {
                float diffAbs = Math.abs(e1.getY() - e2.getY());
                float diff = e1.getX() - e2.getX();

                if (diffAbs > SWIPE_MAX_OFF_PATH)
                    return false;

                // Left swipe
                if (diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    onLeftSwipe();
                }
 /*               // Right swipe
                else if (-diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    onRightSwipe();
                }*/
            } catch (Exception e) {
                Log.e("Gestures", "Error on gestures");
            }
            return false;
        }

        public void onLeftSwipe() {
            Intent x = new Intent(context, UserDBActivity.class);
            startActivity(x);
        }
    }
}
