package com.hly.component.download.db;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

@TargetApi(value = 14)
public class DownloadDBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DownloadDBHelper";
    private static DownloadDBHelper mDBhelper;

    private DownloadDBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }
    
    public static DownloadDBHelper getInstance(Context context) {
        if (mDBhelper == null) {
            synchronized (DownloadDBHelper.class) {
                if (mDBhelper == null) {
                    mDBhelper = new DownloadDBHelper(context);
                }
            }
        }
        return mDBhelper;
    }
    
    private DownloadDBHelper(Context ctx) {
        this(ctx, DbConfig.DB_NAME, null, DbConfig.DB_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(DownloadDBModel.getCreateTblSql());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade");
        try {
            db.execSQL(DownloadDBModel.getDropTblSql());
            db.execSQL(DownloadDBModel.getCreateTblSql());
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
    
    @SuppressLint("Override")
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onDowngrade");
        onUpgrade(db, oldVersion, newVersion);
    }


}
