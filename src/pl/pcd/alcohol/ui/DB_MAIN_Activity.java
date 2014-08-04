package pl.pcd.alcohol.ui;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.*;
import pl.pcd.alcohol.webapi.Reporter;
import pl.pcd.alcohol.webapi.WebLogin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DB_MAIN_Activity extends UpdatingActivity {

    public static final String TAG = "DB_MainActivity";
    @NotNull
    protected Context context = this;
    DBMain db;
    Cursor cursor;
    ListView myList;
    EditText et_search;
    SharedPreferences sharedPreferences;
    @Nullable
    CursorAdapter myCursorAdapter;
    TextView warning;
    LinearLayout linear;
    ProgressDialog pd_DBdl;
    private GestureDetector gestureDetector;

    private void createNoRecordsWarning(@NotNull Context _context) {
        warning = new TextView(_context);
        warning.setText(R.string.no_records);
        warning.setGravity(Gravity.CENTER);
        warning.setTextColor(getResources().getColor(R.color.warning));
        //noinspection ResourceType
        warning.setId(404);
    }

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
                Intent x = new Intent(context, DB_USER_Activity.class);
                x.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(x);
                break;
            case R.id.menu_main_download:
                if (Utils.isConnected(context))
                    new DBUpdater().execute(Const.API.URL_MAIN);
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
        db = new DBMain(this);
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
        sharedPreferences = getSharedPreferences(Const.Prefs.Main.FILE, MODE_PRIVATE);
        createNoRecordsWarning(context);
        myList = (ListView) findViewById(R.id.main_db_list_view);
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
        myList.setOnItemClickListener(new onAlcoholClickListener());
        registerForContextMenu(myList);

        //showChangeLogOnFirstRun(sharedPreferences);

        gestureDetector = new GestureDetector(new SwipeGestureDetector());
        View.OnTouchListener gestureCallback = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, @NotNull MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        };
        linear.setOnTouchListener(gestureCallback);
        myList.setOnTouchListener(gestureCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (myList.getAdapter() == null)
            populateListFromDBAsync();
    }

    @Override
    public boolean onContextItemSelected(@NotNull MenuItem alcohol_item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) alcohol_item.getMenuInfo();
        int menuItemIndex = alcohol_item.getItemId();
        cursor = db.getRow(info.id);

        final int alcoholID = cursor.getInt(DBMain.COL_ID_ALC);
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
                                        json.put("action", Const.API.Actions.FLAG);
                                        json.put("id", alcoholID);
                                        //noinspection ConstantConditions
                                        json.put("content", input.getText().toString());
/*                            JSONArray flags = new JSONArray();
                    flags.put(new JSONObject().put("id",alcoholID).put("info","xddddd"));
                    json.put("flag",flags);*/
                                        if (Cfg.DEBUG) Log.d(TAG, "sent json:\n" + json.toString());
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
                break;
        }
        return true;
    }

    @Deprecated
    private boolean showChangeLogOnFirstRun(@NotNull SharedPreferences sharedPreferences) {
        boolean firstRun = sharedPreferences.getBoolean(Const.Prefs.FIRST_RUN, true);

        if (firstRun) {
            AlertDialog.Builder changeLog = new AlertDialog.Builder(context);
            changeLog.setMessage(Utils.readResource(context, R.raw.changelog, false, "\n"))
                    .setTitle(R.string.whatsNew).setCancelable(false);
            changeLog.setPositiveButton(android.R.string.ok, null);
            changeLog.create().show();
            sharedPreferences.edit().putBoolean(Const.Prefs.FIRST_RUN, false).commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //myList.setAdapter(null);
        //myCursorAdapter = null;
        if (myList.getChildCount() == 0) Log.d(TAG, "Clearing List...<OK>");
        else Log.d(TAG, "Clearing List...<PARTLY OK> Ram not freed until app destroy");
    }

    @Override
    public void onCreateContextMenu(@NotNull ContextMenu menu, @NotNull View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.main_db_list_view) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            // title for context menu
            menu.setHeaderTitle(db.getRow(info.id).getString(DBMain.COL_NAME));
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
                cursor = db.getAllRows(new String[]{DBMain.KEY_ROWID, DBMain.KEY_ID_ALC, DBMain.KEY_NAME, DBMain.KEY_PRICE, DBMain.KEY_VOLUME, DBMain.KEY_PERCENT, DBMain.KEY_TYPE, DBMain.KEY_SUBTYPE}, DBMain.KEY_NAME + " ASC");

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
                        myList.setAdapter(myCursorAdapter);
                        myCursorAdapter.notifyDataSetChanged();
                        /* FILTERING function
                        ======================*/
                        myCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                            public Cursor runQuery(@NotNull CharSequence constraint) {
                                Log.d(TAG, "Querying by:" + constraint);

                                return db.filterByName(constraint.toString());
                            }
                        });

                        if (myList.getCount() == 0) {
                            //noinspection ResourceType
                            if (linear.findViewById(404) == null)
                                linear.addView(warning, 1);
                            Log.d(TAG, "Populating.. <NO RECORDS>");
                        } else if (linear.getChildCount() > 1) {
                            linear.removeView(warning);
                        }
                        Log.d(TAG, "Populating.. <SUCCESS>");
                    }
                });
            }
        }).run();

    }

    class onAlcoholClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Cursor c = db.getRow(id);

            Bundle b = new Bundle();
            b.putLong("id", c.getLong(c.getColumnIndexOrThrow(DBMain.KEY_ROWID)));

            Intent intent = new Intent(context, AlcoholInfoActivity.class);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    private class DBUpdater extends AsyncTask<String, Integer, String> {
        JSONToDBParser jsonParser;

        @Override
        protected void onPostExecute(final String result) {
            Log.d(TAG, "Initializing parser for JSON to fill DB with downloaded DATA");
            final String json = Utils.substringBetween(result, "<json>", "</json>");
            if (json != null) {
                //Operating on DB on UI thread is bad idea - THREADS! No more lag (0.3 s)
                this.jsonParser = new JSONToDBParser();
                jsonParser.execute(json);
                Log.d(TAG, "SERVER:" + Utils.substringBetween(result, "<content>", "</content>"));
            } else {
                pd_DBdl.cancel();
                Toast.makeText(context, "Pusta odpowiedz serwera", Toast.LENGTH_LONG).show();
            }
        }

        @NotNull
        @Override
        protected String doInBackground(@NotNull String... urls) {
            StringBuilder builder = new StringBuilder(5000);

            for (String url : urls) {

                DefaultHttpClient client = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse execute = client.execute(httpGet);
                    HttpEntity httpEntity = execute.getEntity();
                    InputStream content = httpEntity.getContent();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s;
                    while ((s = buffer.readLine()) != null) {
                        builder.append(s);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return builder.toString();
        }

        @Override
        protected void onPreExecute() {
            pd_DBdl = new ProgressDialog(context);
            pd_DBdl.setIndeterminate(true);
            pd_DBdl.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            //pd_DBdl.setTitle(R.string.downloading_db);
            pd_DBdl.setMessage(context.getResources().getString(R.string.downloading_db));
            pd_DBdl.setCanceledOnTouchOutside(false);
            pd_DBdl.show();
        }

        protected class JSONToDBParser extends AsyncTask<String, Void, Void> {

            protected void parseJSONStringToDB(@Nullable final String JSON) {
                try {
                    db.deleteAll();
                    JSONArray JA = new JSONArray(JSON);
                    JSONObject JO;
                    for (int i = 0; i < JA.length(); i++) {
                        JO = JA.getJSONObject(i);
                        DBMain.M_Alcohol alc = new DBMain.M_Alcohol(JO.getLong("ID"), JO.getString(DBMain.KEY_NAME), (float) JO.getDouble(DBMain.KEY_PRICE), JO.getInt(DBMain.KEY_TYPE), JO.getInt(DBMain.KEY_SUBTYPE), JO.getInt(DBMain.KEY_VOLUME), (float) JO.getDouble(DBMain.KEY_PERCENT), JO.getInt(DBMain.KEY_DEPOSIT));
                        db.insertRow(alc);

                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (db.getCount() == 0) {
                                //noinspection ResourceType
                                if (linear.findViewById(404) == null) linear.addView(warning);

                            } else //noinspection ResourceType
                                if (linear.findViewById(404) != null) {
                                    linear.removeView(warning);
                                }
                            // .requery() refreshes the data in listVise,
                            // so DO NOT DELETE IT
                            //noinspection deprecation
                            cursor.requery();
                            Log.d(TAG, "REQUERYING");

                            if (myCursorAdapter != null) {
                                myCursorAdapter.notifyDataSetChanged();
                            }

                        }
                    });
                } catch (JSONException e) {
                    Log.d(TAG, e.toString());
                }
            }

            @Nullable
            @Override
            protected Void doInBackground(String... json) {
                parseJSONStringToDB(json[0]);
                return null;
            }

            @Override
            protected void onPostExecute(Void x) {
                pd_DBdl.cancel();
            }
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
            Intent x = new Intent(context, DB_USER_Activity.class);
            startActivity(x);
        }
    }
}
