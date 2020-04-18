package net.dixq.unlimiteddiary.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    // データーベースのバージョン
    public static final int DATABASE_VERSION = 1;

    // データーベース名
    public static final String DATABASE_NAME = "UnlimitedDiaryDB.db";
    public static final String TABLE_NAME = "unlimiteddiarydb";
    public static final String _ID = "_id";
    public static final String COLUMN_NAME_TITLE = "filename";
    public static final String COLUMN_NAME_SUBTITLE = "json";

    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_TITLE + " TEXT," +
                    COLUMN_NAME_SUBTITLE + " INTEGER)";

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private SQLiteDatabase _db;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Lg.e("コンストラクタ");
        _db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Lg.e("onCreate");
        // テーブル作成
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // アップデートの判別
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}

