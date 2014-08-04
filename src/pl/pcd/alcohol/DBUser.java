package pl.pcd.alcohol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DBUser {

    // Field Names:
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "NAME";
    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_SUBTYPE = "SUBTYPE";
    public static final String KEY_PRICE = "PRICE";
    public static final String KEY_VOLUME = "VOLUME";
    public static final String KEY_PERCENT = "PERCENT";
    public static final String KEY_DEPOSIT = "DEPOSIT";
    public static final String KEY_ALC_ID = "ALID";

    public static final String[] ALL_KEYS = new String[]{KEY_ROWID, KEY_NAME, KEY_TYPE, KEY_SUBTYPE, KEY_PRICE, KEY_VOLUME, KEY_PERCENT, KEY_DEPOSIT, KEY_ALC_ID};
    // Column Numbers for each Field Name:
    public static final int COL_ROWID = 0;
    public static final int COL_NAME = 1;
    public static final int COL_TYPE = 2;
    public static final int COL_SUBTYPE = 3;
    public static final int COL_PRICE = 4;
    public static final int COL_VOLUME = 5;
    public static final int COL_PERCENT = 6;
    public static final int COL_DEPOSIT = 7;
    public static final int COL_ALC_ID = 8;
    // DataBase info:
    public static final String DATABASE_NAME = "user_db";
    public static final String DATABASE_TABLE_ALCOHOLS = "USER_ALCOHOLS";
    //SQL statement to create database
    private static final String DATABASE_CREATE_SQL =
            "CREATE TABLE " + DATABASE_TABLE_ALCOHOLS
                    + " (" + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, "
                    + KEY_NAME + " CHAR NOT NULL, "
                    + KEY_TYPE + " INT DEFAULT(0),"
                    + KEY_SUBTYPE + " INT DEFAULT(0),"
                    + KEY_PRICE + " DOUBLE DEFAULT (0),"
                    + KEY_VOLUME + " INT NOT NULL DEFAULT(500), "
                    + KEY_PERCENT + " DOUBLE,"
                    + KEY_DEPOSIT + " INT,"
                    + KEY_ALC_ID + " INT AUTO INCREMENT"
                    + ");";

    public static final int DATABASE_VERSION = 4;
    private static final String TAG = "DB_USER_Adapter";
    private DatabaseHelper myDBHelper;
    @Nullable
    private SQLiteDatabase db;


    public DBUser(Context ctx) {
        myDBHelper = new DatabaseHelper(ctx);
    }

    public void execSQL(String sql) {
        db.execSQL(sql);
    }

    // Open the database connection.
    @NotNull
    public DBUser open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }

    // Add a new set of values to be inserted into the database.
    public long insertRow(String name, int type, int subtype, float price, int volume, int percent) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_TYPE, type);
        initialValues.put(KEY_SUBTYPE, subtype);
        initialValues.put(KEY_PRICE, price);
        initialValues.put(KEY_VOLUME, volume);
        initialValues.put(KEY_PERCENT, percent);
        // Insert the data into the database.
        return db.insert(DATABASE_TABLE_ALCOHOLS, null, initialValues);
    }

    public int getCount() {
        Cursor mCount = db.rawQuery("select count(*) from " + DATABASE_TABLE_ALCOHOLS, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();
        return count;
    }

    // Add a new set of values to be inserted into the database.
    public long insertRow(@NotNull U_Alcohol alcohol) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, alcohol.get_name());
        initialValues.put(KEY_TYPE, alcohol.get_type());
        initialValues.put(KEY_SUBTYPE, alcohol.get_subtype());
        initialValues.put(KEY_PRICE, alcohol.get_price());
        initialValues.put(KEY_VOLUME, alcohol.get_volume());
        initialValues.put(KEY_PERCENT, alcohol.get_percent());
        initialValues.put(KEY_DEPOSIT, alcohol.get_deposit());
        // Insert the data into the database.
        return db.insert(DATABASE_TABLE_ALCOHOLS, null, initialValues);
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        return db.delete(DATABASE_TABLE_ALCOHOLS, where, null) != 0;
    }

    public void deleteAll() {
        Cursor c = getAllRows();
        long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
        if (c.moveToFirst()) {
            do {
                deleteRow(c.getLong((int) rowId));
            } while (c.moveToNext());
        }
        c.close();
    }

    // Return all data in the database.
    public Cursor getAllRows() {
        String where = null;
        Cursor c = db.query(true, DATABASE_TABLE_ALCOHOLS, ALL_KEYS, where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getAllRows(String[] keys) {
        String where = null;
        Cursor c = db.query(true, DATABASE_TABLE_ALCOHOLS, keys, where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    public Cursor getAllRows(String[] keys, String orderBy) {
        String where = null;
        Cursor c = db.query(true, DATABASE_TABLE_ALCOHOLS, keys, where, null, null, null, orderBy, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Get a specific row (by rowId)
    public Cursor getRow(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = db.query(true, DATABASE_TABLE_ALCOHOLS, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    @Nullable
    public String getNameFromID(long rowId) {
        String where = KEY_ROWID + "=" + rowId;
        Cursor c = db.query(true, DATABASE_TABLE_ALCOHOLS, new String[]{KEY_NAME},
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c.getString(COL_NAME);
    }

    // Get a specific rows (by name)
    public Cursor getRow(String name) {
        String where = KEY_NAME + "=" + name;
        Cursor c = db.query(true, DATABASE_TABLE_ALCOHOLS, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    /**
     * Query a database by a passed parameter
     *
     * @param where e.g. DBAdapter.KEY_PRICE <= 5
     * @return cursor with data
     */
    public Cursor query(String where) {
        Cursor c = db.query(true, DATABASE_TABLE_ALCOHOLS, ALL_KEYS,
                where, null, null, null, null, null);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, String name, float price, int volume, int percent) {
        String where = KEY_ROWID + "=" + rowId;
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_NAME, name);
        newValues.put(KEY_PRICE, price);
        newValues.put(KEY_VOLUME, volume);
        newValues.put(KEY_PERCENT, percent);
        // Insert it into the database.
        return db.update(DATABASE_TABLE_ALCOHOLS, newValues, where, null) != 0;
    }

    // Change an existing row to be equal to new data.
    public boolean updateRow(long rowId, @NotNull U_Alcohol alcohol) {
        String where = KEY_ROWID + "=" + rowId;
        ContentValues newValues = new ContentValues();
        newValues.put(KEY_NAME, alcohol.get_name());
        newValues.put(KEY_PRICE, alcohol.get_price());
        newValues.put(KEY_TYPE, alcohol.get_type());
        newValues.put(KEY_SUBTYPE, alcohol.get_subtype());
        newValues.put(KEY_VOLUME, alcohol.get_volume());
        newValues.put(KEY_PERCENT, alcohol.get_percent());
        newValues.put(KEY_DEPOSIT, alcohol.get_deposit());
        // Insert it into the database.
        return db.update(DATABASE_TABLE_ALCOHOLS, newValues, where, null) != 0;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(@NotNull SQLiteDatabase _db) {
            _db.execSQL(DATABASE_CREATE_SQL);
        }

        @Override
        public void onUpgrade(@NotNull SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ALCOHOLS);

            // Recreate new database:
            onCreate(_db);
        }
    }


    public static class U_Alcohol {
        private String _name;
        private float _price;
        private int _volume;
        private float _percent;
        private int _type;
        private int _subtype;
        private int _deposit;
        private int _alcID;

        public U_Alcohol(String name, float price, int volume, float percent, int type, int subtype, int deposit) {
            _name = name;
            _price = price;
            _volume = volume;
            _percent = percent;
            _type = type;
            _subtype = subtype;
            _deposit = deposit;

        }

        public int get_alcID() {
            return _alcID;
        }

        public void set_alcID(int _alcID) {
            this._alcID = _alcID;
        }

        public int get_deposit() {
            return _deposit;
        }

        public void set_deposit(int _deposit) {
            this._deposit = _deposit;
        }

        public int get_subtype() {
            return _subtype;
        }

        public void set_subtype(int _subtype) {
            this._subtype = _subtype;
        }

        public int get_type() {
            return _type;
        }

        public void set_type(int _type) {
            this._type = _type;
        }

        public String get_name() {
            return _name;
        }

        public void set_name(String _name) {
            this._name = _name;
        }

        public float get_percent() {
            return _percent;
        }

        public void set_percent(float _percent) {
            this._percent = _percent;
        }

        public int get_volume() {
            return _volume;
        }

        public void set_volume(int _volume) {
            this._volume = _volume;
        }

        public float get_price() {
            return _price;
        }

        public void set_price(float _price) {
            this._price = _price;
        }

        public static final class Type {
            public static final int NISKOPROCENTOWY = 0;
            public static final int SREDNIOPROCENTOWY = 1;
            public static final int WYSOKOPROCENTOWY = 2;
        }

    }
}

