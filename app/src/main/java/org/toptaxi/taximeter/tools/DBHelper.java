package org.toptaxi.taximeter.tools;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.toptaxi.taximeter.MainApplication;


public class DBHelper extends SQLiteOpenHelper {
    protected static String TAG = "#########" + DBHelper.class.getName();

    public DBHelper(Context context) {
        super(context, MainApplication.getInstance().getApplicationContext().getPackageName(), null, Constants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL = "create table messages(" +
                "id integer primary key" +
                ")";
        sqLiteDatabase.execSQL(SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS messages");
        onCreate(sqLiteDatabase);
    }
}
