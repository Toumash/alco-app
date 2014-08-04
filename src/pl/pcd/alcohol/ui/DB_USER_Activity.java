package pl.pcd.alcohol.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.pcd.alcohol.*;
import pl.pcd.alcohol.Const.EditIntentExtras;
import pl.pcd.alcohol.webAPIHelper.WebLogin;


public class DB_USER_Activity extends TitleActivity {

    public static final String TAG = "DB_UserActivity";
    @NotNull
    final Context context = this;
    @NotNull
    final ListView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

            AlertDialog.Builder popup = new AlertDialog.Builder(context);
            popup.setMessage("WOW wybrałeś  " + db_user.getRow(id).getString(DBUser.COL_NAME) + " Koszt: " + db_user.getRow(id).getFloat(DBUser.COL_PRICE) + "zł WOW takie fajne");
            popup.setIcon(R.drawable.doge_easter_egg);
            popup.setTitle(R.string.doge);
            popup.setPositiveButton(android.R.string.ok, null);
            popup.create().show();

        }
    };
    DBUser db_user;
    Cursor cursor;
    ListView myList;
    GestureDetector gestureDetector;
    @Nullable
    private AlcoholCursorAdapter myCursorAdapter;
    private TextView warning;
    private LinearLayout linear;

    private void openDB() {
        db_user = new DBUser(this);
        db_user.open();
    }

    private void closeDB() {
        if (db_user != null)
            db_user.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
        myList.setAdapter(null);
        myCursorAdapter = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentViewWithTitle(context, R.layout.activ_user_list, R.string.my_proposals);

/*      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            android.app.ActionBar actionBar = getActionBar();
            if (actionBar != null)
                actionBar.setDisplayHomeAsUpEnabled(true);
        }*/
        createWarning(context);
        myList = (ListView) findViewById(R.id.user_db_list_view);
        linear = (LinearLayout) findViewById(R.id.linearRoot_listActivity);
        openDB();
        myList.setOnItemClickListener(onItemClickListener);
        registerForContextMenu(myList);

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
        populateListViewAsyncFromDB();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // myList.setAdapter(null);
        if (myList.getChildCount() == 0) Log.d(TAG, "Clearing List...<OK>");
        else Log.d(TAG, "Clearing List...<ERROR> Ram not freed until app destroy");
    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
        super.onCreateOptionsMenu(menu);
        // getMenuInflater().inflate(R.menu.user_list, menu);
        getSupportMenuInflater().inflate(R.menu.user_list, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                handleSendingDB();
            } else {
                Toast.makeText(context, R.string.not_logged_in, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull com.actionbarsherlock.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_user_editor:
                Intent x = new Intent(context, EditorActivity.class);
                x.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(x);
                break;
            case R.id.menu_user_settings:
                Intent s = new Intent(context, PrefsActivity.class);
                s.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(s);
                break;
            case R.id.menu_user_upload_data:
                handleSendingDB();
                break;
        }
        return true;
    }

    protected void handleSendingDB() {
        AlertDialog.Builder al = new AlertDialog.Builder(context);
        al.setMessage(R.string.are_you_sure);
        al.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Utils.isConnected(context)) {
                    if (db_user.getCount() > 0) {
                        new WebLogin(context, TAG) {
                            @Override
                            protected void onPostExecute(String r) {
                                super.onPostExecute(r);
                                if (this.result == Result.OK)
                                    uploadDBToServer(this.username, this.password);
                                else {
                                    Intent x = new Intent(context, LoginActivity.class);
                                    startActivityForResult(x, Const.REQUEST_LOGIN);
                                    Log.d(TAG, "Saved login or/and password incorrect");
                                }
                            }
                        }.execute();
                    } else {
                        Toast.makeText(context, R.string.no_records_to_send, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(context, R.string.no_internet, Toast.LENGTH_LONG).show();
                }
            }

        });
        al.setNegativeButton(android.R.string.no, null);
        al.show();
    }

    @Override
    public void onCreateContextMenu(@NotNull ContextMenu menu, @NotNull View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.user_db_list_view) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            // title for context menu
            menu.setHeaderTitle(db_user.getRow(info.id).getString(DBUser.COL_NAME));
            String[] menuItems = getResources().getStringArray(R.array.menu_user_db);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    public void uploadDBToServer(String username, String password) {
        Cursor c = db_user.getAllRows();
        if (c.getCount() > 0) {
            JSONObject JSON = new JSONObject();
            JSONArray alcArray = new JSONArray();
            JSONObject alcohol;
            try {
                JSON.put("action", Const.API.Actions.UPLOAD);
                JSON.put("login", username);
                JSON.put("password", password);

                do {
                    alcohol = new JSONObject();
/*
                    //WORKAROUND for DB strange floats
                    Float rawPrice = c.getFloat(DB_USER.COL_PRICE);
                    Float price = rawPrice;
                    String priceS = String.valueOf(rawPrice);
                    if (priceS.contains(".")) {
                        int index = priceS.indexOf(".");
                        if (priceS.length() > index + 3)
                            price = Float.valueOf(priceS.substring(0, index - 1) + "." + priceS.substring(index + 1, index + 2));
                    }
                    /////////////////////////////////*/
                    alcohol.put(DBUser.KEY_NAME, c.getString(DBUser.COL_NAME));
                    /*alcohol.put(DB_USER.KEY_PRICE, price);*/
                    alcohol.put(DBUser.KEY_PRICE, c.getDouble(DBUser.COL_PRICE));
                    alcohol.put(DBUser.KEY_TYPE, c.getInt(DBUser.COL_TYPE));
                    alcohol.put(DBUser.KEY_SUBTYPE, c.getInt(DBUser.COL_SUBTYPE));
                    alcohol.put(DBUser.KEY_VOLUME, c.getInt(DBUser.COL_VOLUME));
                    alcohol.put(DBUser.KEY_PERCENT, c.getDouble(DBUser.COL_PERCENT));
                    alcohol.put(DBUser.KEY_DEPOSIT, c.getInt(DBUser.COL_DEPOSIT));
                    Log.d("alcArray", alcohol.toString());
                    alcArray.put(alcohol);
                } while (c.moveToNext());
                JSON.put("alcohols", alcArray);
                Log.d(TAG, JSON.toString());
                new POST_USER_DB_JSONAsyncTask().execute(JSON.toString());


            } catch (JSONException e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Toast.makeText(context, R.string.no_records_to_send, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onContextItemSelected(@NotNull MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        Cursor cur = db_user.getRow(info.id);
        switch (menuItemIndex) {
            case Const.ContextMenu.KEY_EDIT:
                Log.d(TAG, "selected Editing of the " + cur.getString(DBUser.COL_NAME) + " record");

                Bundle extras = new Bundle();
                extras.putLong(EditIntentExtras.KEY_ID, cur.getLong(DBUser.COL_ROWID));
                extras.putString(EditIntentExtras.KEY_NAME, cur.getString(DBUser.COL_NAME));
                extras.putFloat(EditIntentExtras.KEY_PRICE, cur.getFloat(DBUser.COL_PRICE));
                extras.putInt(EditIntentExtras.KEY_TYPE, cur.getInt(DBUser.COL_TYPE));
                extras.putInt(EditIntentExtras.KEY_SUBTYPE, cur.getInt(DBUser.COL_SUBTYPE));
                extras.putInt(EditIntentExtras.KEY_VOLUME, cur.getInt(DBUser.COL_VOLUME));
                extras.putInt(EditIntentExtras.KEY_PERCENT, cur.getInt(DBUser.COL_PERCENT));
                extras.putInt(EditIntentExtras.KEY_DEPOSIT, cur.getInt(DBUser.COL_DEPOSIT));


                Intent edit = new Intent(context, EditorActivity.class);
                edit.putExtras(extras);
                startActivity(edit);
                break;
            case Const.ContextMenu.KEY_DELETE:

                AlertDialog.Builder b = new AlertDialog.Builder(context);
                b.setMessage(R.string.are_you_sure_delete);
                b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db_user.deleteRow(info.id);
                        myList.setAdapter(null);
                        populateListViewAsyncFromDB();
                    }
                });
                b.setNegativeButton(android.R.string.no, null);
                b.create().show();
                break;

        }
        return true;
    }

    /**
     * remember to initialize this in the onCreate()
     */
    private void createWarning(@NotNull Context _context) {
        warning = new TextView(_context);
        warning.setText(R.string.no_records);
        warning.setGravity(Gravity.CENTER);
        warning.setTextColor(getResources().getColor(R.color.warning));
    }

    private void populateListViewAsyncFromDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Populating list...");
                cursor = db_user.getAllRows(new String[]{DBUser.KEY_ROWID, DBUser.KEY_NAME, DBUser.KEY_TYPE, DBUser.KEY_SUBTYPE, DBUser.KEY_PRICE, DBUser.KEY_VOLUME, DBUser.KEY_PERCENT, DBUser.KEY_ALC_ID});

                //Setup mapping from cursor to view fields
               /* String[] fromFieldNames = new String[]
                        {DB_USER.KEY_ROWID, DB_USER.KEY_NAME, DB_USER.KEY_PRICE, DB_USER.KEY_VOLUME, DB_USER.KEY_PERCENT};

                int[] toViewIDs = new int[]{R.id.itemNumber, R.id.itemName, R.id.itemPrice, R.id.itemVolume, R.id.itemPercentage};
*/
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
                        if (myList.getCount() == 0) {
                            if (linear.getChildCount() < 2)
                                linear.addView(warning, 0);
                            //new AlertDialog.Builder(context).setMessage(R.string.no_records).setPositiveButton(android.R.string.ok, null).create().show();
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

    private class POST_USER_DB_JSONAsyncTask extends AsyncTask<String, Void, String> {
        @Nullable
        @Override
        protected String doInBackground(String... json) {
            Log.d(TAG, "Starting upload of JSON");
            return JSONTransmitter.postJSON(json[0], Const.API.URL_JSON);
        }

        @Override
        protected void onPostExecute(String r) {
            String response = Utils.substringBetween(r, "<json>", "</json>");
            JSONObject json;
            String result;
            try {
                json = new JSONObject(response);
                result = json.getString("result");
            } catch (JSONException e) {
                result = "error";
            }
            Toast.makeText(context, R.string.sent, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "JSON has been successfully sent!");

            if (result.equals("ok")) {
                db_user.deleteAll();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (db_user.getCount() == 0) {
                            if (linear.getChildCount() < 2) linear.addView(warning);

                        } else if (linear.getChildCount() > 1) {
                            linear.removeView(warning);
                        }
                        cursor.requery();
                        myCursorAdapter.notifyDataSetChanged();
                        Log.d(TAG, "REQUERYING");
                    }
                });
            } else if (result.equals(Const.API.LoginResult.LOGIN_PASSWORD)) {
                Toast.makeText(context, R.string.login_invalid_data, Toast.LENGTH_LONG).show();
            }
            if (result.equals("error")) {
                Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show();
            }
            Log.d(TAG, "Result:" + response);
            Log.d(TAG, "api content: " + Utils.substringBetween(r, "<content>", "</content>") + " ");
            //Log.d(TAG,r);
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

/*                // Left swipe
                if (diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    onLeftSwipe();
                }*/
                    // Right swipe
                else if (-diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    onRightSwipe();
                }
            } catch (Exception e) {
                Log.e("Gestures", "Error on gestures");
            }
            return false;
        }

        public void onRightSwipe() {
            Intent x = new Intent(context, DB_MAIN_Activity.class);
            startActivity(x);
        }
    }
}
