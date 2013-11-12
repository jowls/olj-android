package com.saltwatersoftware.onelinejournal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by j on 11/11/13.
 */
public class SqlOpenHelper extends SQLiteOpenHelper {
    public static final String DBNAME = "tasksdb.sqlite";
    public static final int VERSION =1;
    public static final String TABLE_NAME = "days";
    public static final String ID= "id";
    public static final String DATE="date";
    public static final String CONTENT="content";
    public static final String UPDATED_AT="updated_at";

    public SqlOpenHelper(Context context) {
        super(context, DBNAME, null, VERSION);

    }
    public void onCreate(SQLiteDatabase db) {
        createDatabase(db);
    }
    private void createDatabase(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(" +
                ID + " integer primary key autoincrement not null, " +
                DATE + " date unique not null, " +
                CONTENT + " content not null, " +
                UPDATED_AT + " datetime not null"
                + ");"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2)
    {
    }
}
