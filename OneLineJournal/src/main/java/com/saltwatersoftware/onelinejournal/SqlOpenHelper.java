package com.saltwatersoftware.onelinejournal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by j on 11/11/13.
 */
public class SqlOpenHelper extends SQLiteOpenHelper {
    public static final String DBNAME = "oljdb.sqlite";
    public static final int VERSION =3; //bumped to 2 for release 1.02
    public static final String TABLE_NAME = "days";
    public static final String ID= "id";
    public static final String DATE="date";
    public static final String CONTENT="content";
    public static final String UPDATED_AT="updated_at";
    public static final String RAILS_ID="rails_id";

    public SqlOpenHelper(Context context) {
        super(context, DBNAME, null, VERSION);

    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        createDatabase(db);
    }
    private void createDatabase(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(" +
                ID + " integer primary key autoincrement not null, " +
                DATE + " date unique not null, " +
                CONTENT + " text not null, " +
                UPDATED_AT + " datetime, " +
                RAILS_ID + " integer not null"
                + ");"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2)
    {
/*        arg0.execSQL("alter table " + TABLE_NAME +
                " add column " + RAILS_ID + " integer not null;"
        );*/
        MainActivity.editor.putLong("db_updated", -1);
        MainActivity.editor.apply();
        onCreate(arg0);
    }
}