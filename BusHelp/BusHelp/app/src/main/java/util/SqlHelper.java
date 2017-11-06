package util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 创建历史记录的数据库
 */
public class SqlHelper extends SQLiteOpenHelper {
    public static final String CREATE_HISTORY="create table History(" +
            "id integer primary key autoincrement," +
            "start_address text," +
            "end_address text," +
            "start_latitude  text," +
            "start_longitude text," +
            "end_latitude text," +
            "end_longitude text)";


    public SqlHelper(Context context) {
        super(context, "history.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
