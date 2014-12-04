package com.hly.component.download.db;

import com.hly.component.download.tools.T;

import android.content.ContentValues;
import android.database.Cursor;


/**
 * 注意所有的db操作的方法都要加锁，锁DbManager的单例
 * @author erichua
 *
 */
public class BaseDBModel {

    public void putValues(ContentValues cv, String key, String value) {
        if (!T.ckIsEmpty(value)) {
            cv.put(key, value);
        } else {
            cv.put(key, "");
        }
    }
    
    public void putValues(ContentValues cv, String key, int value) {
        cv.put(key, value);
    }

    public void putValues(ContentValues cv, String key, long value) {
        cv.put(key, value);
    }
    
    public String getStringByName(Cursor c, String columnName) {
        return c.getString(c.getColumnIndex(columnName));
    }

    protected int getIntByName(Cursor c, String columnName) {
        return c.getInt(c.getColumnIndex(columnName));
    }

    protected long getLongByName(Cursor c, String columnName) {
        return c.getLong(c.getColumnIndex(columnName));
    }
}
