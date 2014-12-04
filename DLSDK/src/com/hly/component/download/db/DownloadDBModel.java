package com.hly.component.download.db;

import java.util.ArrayList;

import com.hly.component.download.DownloadConstants;
import com.hly.component.download.tools.T;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DownloadDBModel extends BaseDBModel {
	
	//广告排期
	public static final String COLUMN_ASSISTANT_ID= "assistantId";
	//广告排期
	public static final String COLUMN_APP_KEY = "appkey";
	//广告排期
	public static final String COLUMN_AD_VERSION = "adVersion";
	//广告排期
	public static final String COLUMN_SCHEDULE_ID = "scheduleId";
	//APPID
	public static final String COLUMN_APP_ID = "appId";
	//广告主ID
	public static final String COLUMN_PROVIDER_ID = "providerId";
	// Int 轮播时内容显示在第几帧位上(直越小，显示越靠前)
	public static final String COLUMN_FRAME = "frame";
	//广告位Id
	public static final String COLUMN_SPACE_ID = "spaceId";
	//广告内容ID
	public static final String COLUMN_CONTENT_ID = "contentId";
	//客户ID
	public static final String COLUMN_CUSTOMER_ID = "customerId";
	//广告计费方式
	public static final String COLUMN_PAY_TYPE = "payType";
    // TAG
    private static final String TAG = "HJJ";
    // 需要一个唯一标识的id    
    public final static String COLUMN_ID = "_id";
    // md5
    public final static String COLUMN_MD5 = "md5";
    // 下载标题
    public final static String COLUMN_TITLE = "title";
    // 下载文件的描述
    public final static String COLUMN_DESCRIPTION = "desc";
    // 下载文件的uri
    public final static String COLUMN_URI = "uri";
    // 重定向的uri,如果没有则为空
    public final static String COLUMN_REDIRECT_URI = "redirect_uri";
    // 下载文件的类型
    public final static String COLUMN_MEDIA_TYPE = "media_type";
    // 本地保存的uri
    public final static String COLUMN_LOCAL_URI = "local_uri";
    // 下载的状态
    public final static String COLUMN_STATUS = "status";
    // 错误原因说明
    public final static String COLUMN_REASON = "reason";
    // 错误原因说明
    public final static String COLUMN_ERROR_MSG = "err_msg";
    // 下载的总大小
    public final static String COLUMN_TOTAL_BYTES = "total_bytes";
    // 当前下载的大小
    public final static String COLUMN_CURRENT_BYTES = "current_bytes";
    // last modify time
    public final static String COLUMN_LAST_MODIFIED_TIMESTAMP = "lastmod_stamp";
    // create download time
    public static final String COLUMN_CREATE_TIMESTAMP = "create_stamp";
    // 头文件，保存格式为header:value;header:value;
    public static final String COLUMN_REQUEST_HEADER = "header";
    // 删除文件标识
    public static final String COLUMN_DELETED = "deleted";
    // 删除文件标识
    public static final String COLUMN_ACCEPT_NET = "accept_net";
    // TABLE名
	public final static String TBL_NAME = "download_info";
	
	private DownloadDBHelper dbhelper;

	public DownloadDBModel(Context context) {
	    this.dbhelper = DownloadDBHelper.getInstance(context);
	}
	
	public static String getCreateTblSql() {
		String createTblSql = "";
		createTblSql += "CREATE TABLE IF NOT EXISTS [" + TBL_NAME + "] (";
		createTblSql += "[" + COLUMN_ID + "] INTEGER PRIMARY KEY AUTOINCREMENT,";
		createTblSql += "[" + COLUMN_MD5 + "] NVARCHAR(128) NOT NULL,";
		createTblSql += "[" + COLUMN_TITLE + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_DESCRIPTION + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_URI + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_REDIRECT_URI + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_MEDIA_TYPE + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_LOCAL_URI + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_STATUS + "] INTEGER  NULL,";
		createTblSql += "[" + COLUMN_REASON + "] INTEGER  NULL,";
		
		createTblSql += "[" + COLUMN_ASSISTANT_ID + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_APP_KEY + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_AD_VERSION + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_SCHEDULE_ID + "] INTEGER  NULL,";
		createTblSql += "[" + COLUMN_APP_ID + "] INTEGER  NULL,";
		createTblSql += "[" + COLUMN_PROVIDER_ID + "] INTEGER  NULL,";
		createTblSql += "[" + COLUMN_FRAME + "] INTEGER  NULL,";
		createTblSql += "[" + COLUMN_SPACE_ID + "] INTEGER  NULL,";
		createTblSql += "[" + COLUMN_CONTENT_ID + "] INTEGER  NULL,";
		createTblSql += "[" + COLUMN_CUSTOMER_ID + "] INTEGER  NULL,";
		createTblSql += "[" + COLUMN_PAY_TYPE + "] INTEGER  NULL,";
		
		createTblSql += "[" + COLUMN_ERROR_MSG + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_TOTAL_BYTES + "] INTEGER  NULL,";
		createTblSql += "[" + COLUMN_CURRENT_BYTES + "] INTEGER  NULL,";
		createTblSql += "[" + COLUMN_LAST_MODIFIED_TIMESTAMP + "] TIMESTAMP  NULL,";
		createTblSql += "[" + COLUMN_CREATE_TIMESTAMP + "] TIMESTAMP  NULL,";
		createTblSql += "[" + COLUMN_REQUEST_HEADER + "] VARCHAR(256)  NULL,";
		createTblSql += "[" + COLUMN_ACCEPT_NET + "] INTEGER  DEFAULT 1,";
        createTblSql += "[" + COLUMN_DELETED + "] INTEGER  NULL";
        createTblSql += ")";
		Log.d(TAG, createTblSql);
		return createTblSql;
	}

	public static String getDropTblSql() {
		return "DROP TABLE IF EXISTS " + TBL_NAME;
	}

	public String getTableName() {
		return TBL_NAME;
	}

	public String getColumnValueById(String id, String columnName) {
		synchronized (dbhelper) {
			String matid = "";
			String selection = " " + COLUMN_ID + " = ? ";
			String[] selectionArgs = { id };
			try {
				SQLiteDatabase rDb = dbhelper.getReadableDatabase();
				Cursor c = rDb.query(TBL_NAME, null, selection, selectionArgs,
						null, null, null, null);
				if (c.getCount() > 0) {
					c.moveToFirst();
					matid = getStringByName(c, columnName);
				}
				c.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			    dbhelper.close();
			}
			return matid;
		}

	}

	private ContentValues getAllContentValues(DownloadInfo info) {
        ContentValues cv = new ContentValues();
        putValues(cv, COLUMN_CREATE_TIMESTAMP, info.create_stamp);
        putValues(cv, COLUMN_CURRENT_BYTES, info.current_bytes);
        putValues(cv, COLUMN_DELETED, info.deleted);
        putValues(cv, COLUMN_DESCRIPTION, info.desc);
        putValues(cv, COLUMN_LAST_MODIFIED_TIMESTAMP, info.lastmod_stamp);
        putValues(cv, COLUMN_LOCAL_URI, info.local_uri);
        putValues(cv, COLUMN_MD5, info.md5);
        putValues(cv, COLUMN_MEDIA_TYPE, info.media_type);
        putValues(cv, COLUMN_REASON, info.reason);
        putValues(cv, COLUMN_REDIRECT_URI, info.redirect_uri);
        putValues(cv, COLUMN_REQUEST_HEADER, info.header);
        putValues(cv, COLUMN_STATUS, info.status);
        putValues(cv, COLUMN_TITLE, info.title);
        putValues(cv, COLUMN_TOTAL_BYTES, info.total_bytes);
        putValues(cv, COLUMN_URI, info.uri);
        
        putValues(cv, COLUMN_SCHEDULE_ID, info.iSchedualId);
        putValues(cv, COLUMN_APP_ID, info.iAppId);
        putValues(cv, COLUMN_PROVIDER_ID, info.iProviderId);
        putValues(cv, COLUMN_FRAME, info.iFrame);
        putValues(cv, COLUMN_SPACE_ID, info.iSpaceId);
        putValues(cv, COLUMN_CONTENT_ID, info.contentId);
        putValues(cv, COLUMN_CUSTOMER_ID, info.iCustomerId);
        putValues(cv, COLUMN_PAY_TYPE, info.iPayType);
        putValues(cv, COLUMN_ASSISTANT_ID, info.assistantId);
        putValues(cv, COLUMN_APP_KEY, info.appkey);
        putValues(cv, COLUMN_AD_VERSION, info.adVersion);
        return cv;
	}

	private void getDownloadInfoByCursor(Cursor c, DownloadInfo tmpInfo){
	    if(c == null) {
            return ;
        }
	    c.moveToFirst();
	    tmpInfo.id = getLongByName(c, COLUMN_ID);
        tmpInfo.md5 = getStringByName(c, COLUMN_MD5);
        tmpInfo.title = getStringByName(c, COLUMN_TITLE);
        
        tmpInfo.desc = getStringByName(c, COLUMN_DESCRIPTION);
        tmpInfo.uri = getStringByName(c, COLUMN_URI);
        tmpInfo.redirect_uri = getStringByName(c, COLUMN_REDIRECT_URI);
        tmpInfo.media_type = getStringByName(c, COLUMN_MEDIA_TYPE);
        
        tmpInfo.local_uri = getStringByName(c, COLUMN_LOCAL_URI);
        tmpInfo.status = getIntByName(c, COLUMN_STATUS);
        tmpInfo.reason = getIntByName(c, COLUMN_REASON);
        tmpInfo.errMsg = getStringByName(c, COLUMN_ERROR_MSG);
        
        tmpInfo.total_bytes = getLongByName(c, COLUMN_TOTAL_BYTES);
        tmpInfo.current_bytes = getLongByName(c, COLUMN_CURRENT_BYTES);
        tmpInfo.lastmod_stamp = getLongByName(c, COLUMN_LAST_MODIFIED_TIMESTAMP);
        tmpInfo.create_stamp = getLongByName(c, COLUMN_CREATE_TIMESTAMP);
        tmpInfo.header = getStringByName(c, COLUMN_REQUEST_HEADER);
        tmpInfo.deleted = getIntByName(c, COLUMN_DELETED);
        tmpInfo.acceptNet = getIntByName(c, COLUMN_ACCEPT_NET);
        
        tmpInfo.iSchedualId = getIntByName(c, COLUMN_SCHEDULE_ID);
        tmpInfo.iAppId = getIntByName(c, COLUMN_APP_ID);
        tmpInfo.iProviderId = getIntByName(c, COLUMN_PROVIDER_ID);
        tmpInfo.iFrame = getIntByName(c, COLUMN_FRAME);
        tmpInfo.iSpaceId = getIntByName(c, COLUMN_SPACE_ID);
        tmpInfo.contentId = getIntByName(c, COLUMN_CONTENT_ID);
        tmpInfo.iCustomerId = getIntByName(c, COLUMN_CUSTOMER_ID);
        tmpInfo.iPayType = getIntByName(c, COLUMN_PAY_TYPE);
        
        tmpInfo.assistantId = getStringByName(c, COLUMN_ASSISTANT_ID);
        tmpInfo.appkey = getStringByName(c, COLUMN_APP_KEY);
        tmpInfo.adVersion = getStringByName(c, COLUMN_AD_VERSION);
	}
	
    private ArrayList<DownloadInfo> getAllDownloadInfos(Cursor c) {
        ArrayList<DownloadInfo> infos = new ArrayList<DownloadInfo>();
        if(c == null) {
            return infos;
        }
        DownloadInfo tmpInfo = null;
        while (c.moveToNext()) {
            tmpInfo = new DownloadInfo();
            tmpInfo.id = getLongByName(c, COLUMN_ID);
            tmpInfo.md5 = getStringByName(c, COLUMN_MD5);
            tmpInfo.title = getStringByName(c, COLUMN_TITLE);
            
            tmpInfo.desc = getStringByName(c, COLUMN_DESCRIPTION);
            tmpInfo.uri = getStringByName(c, COLUMN_URI);
            tmpInfo.redirect_uri = getStringByName(c, COLUMN_REDIRECT_URI);
            tmpInfo.media_type = getStringByName(c, COLUMN_MEDIA_TYPE);
            
            tmpInfo.local_uri = getStringByName(c, COLUMN_LOCAL_URI);
            tmpInfo.status = getIntByName(c, COLUMN_STATUS);
            tmpInfo.reason = getIntByName(c, COLUMN_REASON);
            tmpInfo.errMsg = getStringByName(c, COLUMN_ERROR_MSG);
            
            tmpInfo.total_bytes = getLongByName(c, COLUMN_TOTAL_BYTES);
            tmpInfo.current_bytes = getLongByName(c, COLUMN_CURRENT_BYTES);
            tmpInfo.lastmod_stamp = getLongByName(c, COLUMN_LAST_MODIFIED_TIMESTAMP);
            tmpInfo.create_stamp = getLongByName(c, COLUMN_CREATE_TIMESTAMP);
            tmpInfo.header = getStringByName(c, COLUMN_REQUEST_HEADER);
            tmpInfo.deleted = getIntByName(c, COLUMN_DELETED);
            tmpInfo.acceptNet = getIntByName(c, COLUMN_ACCEPT_NET);
            
            tmpInfo.iSchedualId = getIntByName(c, COLUMN_SCHEDULE_ID);
            tmpInfo.iAppId = getIntByName(c, COLUMN_APP_ID);
            tmpInfo.iProviderId = getIntByName(c, COLUMN_PROVIDER_ID);
            tmpInfo.iFrame = getIntByName(c, COLUMN_FRAME);
            tmpInfo.iSpaceId = getIntByName(c, COLUMN_SPACE_ID);
            tmpInfo.contentId = getIntByName(c, COLUMN_CONTENT_ID);
            tmpInfo.iCustomerId = getIntByName(c, COLUMN_CUSTOMER_ID);
            tmpInfo.iPayType = getIntByName(c, COLUMN_PAY_TYPE);     
            
            tmpInfo.assistantId = getStringByName(c, COLUMN_ASSISTANT_ID);
            tmpInfo.appkey = getStringByName(c, COLUMN_APP_KEY);
            tmpInfo.adVersion = getStringByName(c, COLUMN_AD_VERSION);
            infos.add(tmpInfo);
        }
        return infos;
    }
	
	public long isExisted(DownloadInfo info) {
	    long id = -1;
		synchronized (dbhelper) {
			String[] columns = null;
			String selection = " " + COLUMN_MD5 + " = ? and " + COLUMN_DELETED + " = 0";
			String[] selectionArgs = { String.valueOf(info.md5) };
			String groupBy = null;
			String having = null;
			String orderBy = null;
			String limit = null;
			Cursor cursor = null;
			try {
				SQLiteDatabase rDb = dbhelper.getReadableDatabase();
				cursor = rDb.query(TBL_NAME, columns, selection,
						selectionArgs, groupBy, having, orderBy, limit);
				if(cursor != null) {
				    int count = cursor.getCount();
				    if(count == 0) {
				        // 说明没有
				        id = 0;
				    } else if (count == 1) {
				        getDownloadInfoByCursor(cursor, info);
				        id = info.id;
	                } else {
	                    id = -count;
	                }
				} else {
				    id = 0;
				}
			} catch (Exception e) {
				e.printStackTrace();
				id = -1;
			} finally {
			    if(null != cursor) {
			        cursor.close();
			    }
			}
		}
		return id;
	}

	public int update(DownloadInfo info) {
		synchronized (dbhelper) {
			ContentValues values = getAllContentValues(info);
			try {
				String whereClause = " `" + COLUMN_ID + "` = ? ";
				String[] whereArgs = new String[] { String.valueOf(info.id) };
				SQLiteDatabase sqLiteDatabase = dbhelper.getWritableDatabase();
				return sqLiteDatabase.update(TBL_NAME, values, whereClause, whereArgs);
			} catch (Exception e) {
				return 0;
			} finally {
			    dbhelper.close();
			}
		}
	}

	// 添加是指首次启动
    public long add(DownloadInfo info) {
        long id = isExisted(info);
        if(id == 0) {
            id = this.insert(info);
        }
        return id;
    }

	public long insert(DownloadInfo info) {
	    long id = 0;
		synchronized (dbhelper) {
			try {
				SQLiteDatabase sqLiteDatabase = dbhelper.getWritableDatabase();
				id = sqLiteDatabase.insert(TBL_NAME, null, getAllContentValues(info));
			} catch (Exception e) {
			    Log.w(TAG, "Insert into AppInfo error, " + e.getMessage());
			} 
		}
		return id;
	}

	// 查询出所有未被删除的下载列表
	public ArrayList<DownloadInfo> queryAll(){
	    ArrayList<DownloadInfo> infos = new ArrayList<DownloadInfo>();
        synchronized (dbhelper) {
            String[] columns = null;
            String selection = " " + COLUMN_DELETED + " = ? and " + COLUMN_STATUS + " != " + DownloadConstants.STATUS_SUCCESSFUL;
            String[] selectionArgs = { "0" };
            String groupBy = null;
            String having = null;
            String orderBy = null;
            String limit = null;
            Cursor c = null;
            try {
                SQLiteDatabase rDb = dbhelper.getReadableDatabase();
                c = rDb.query(TBL_NAME, columns, selection,
                        selectionArgs, groupBy, having, orderBy, limit);
                infos.addAll(getAllDownloadInfos(c));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(c != null) {
                    c.close();
                }
            }
        }
        return infos;
	}
	
	public int updateUrlChangeById(long id, long totalSize, long completesize, String redirectUrl){
        synchronized (dbhelper) {
            try {
                SQLiteDatabase sqLiteDatabase = dbhelper.getWritableDatabase();
                String whereClause = " " + COLUMN_ID + " = ? ";
                String[] whereArgs = new String[] { String.valueOf(id) };
                ContentValues cv = new ContentValues();
                putValues(cv, COLUMN_LAST_MODIFIED_TIMESTAMP, System.currentTimeMillis());
                putValues(cv, COLUMN_REDIRECT_URI, redirectUrl);
                putValues(cv, COLUMN_TOTAL_BYTES, totalSize);
                putValues(cv, COLUMN_CURRENT_BYTES, completesize);
                return sqLiteDatabase.update(TBL_NAME, cv, whereClause, whereArgs);
            } catch (Exception e) {
                Log.w(TAG, "Insert into AppInfo error, " + e.getMessage());
                return 0;
            } 
        }
	}
	
    public int updateProgressById(long id, long totalSize, long completesize, int status) {
        synchronized (dbhelper) {
            try {
                SQLiteDatabase sqLiteDatabase = dbhelper.getWritableDatabase();
                String whereClause = " " + COLUMN_ID + " = ? ";
                String[] whereArgs = new String[] {
                    String.valueOf(id)
                };
                ContentValues cv = new ContentValues();
                putValues(cv, COLUMN_LAST_MODIFIED_TIMESTAMP, System.currentTimeMillis());
                putValues(cv, COLUMN_STATUS, status);
                putValues(cv, COLUMN_TOTAL_BYTES, totalSize);
                putValues(cv, COLUMN_CURRENT_BYTES, completesize);
                return sqLiteDatabase.update(TBL_NAME, cv, whereClause, whereArgs);
            } catch (Exception e) {
                Log.w(TAG, "Insert into AppInfo error, " + e.getMessage());
                return 0;
            }
        }
    }
	
    //totalsize completesize,status， reason, errmsg
    public int updateStateById(long id, long totalSize, long completesize, int status, int reason, String errmsg) {
        synchronized (dbhelper) {
            try {
                SQLiteDatabase sqLiteDatabase = dbhelper.getWritableDatabase();
                String whereClause = " " + COLUMN_ID + " = ? ";
                String[] whereArgs = new String[] {
                    String.valueOf(id)
                };
                ContentValues cv = new ContentValues();
                putValues(cv, COLUMN_LAST_MODIFIED_TIMESTAMP, System.currentTimeMillis());
                putValues(cv, COLUMN_ERROR_MSG, errmsg);
                putValues(cv, COLUMN_TOTAL_BYTES, totalSize);
                putValues(cv, COLUMN_CURRENT_BYTES, completesize);
                putValues(cv, COLUMN_STATUS, status);
                putValues(cv, COLUMN_REASON, reason);
                return sqLiteDatabase.update(TBL_NAME, cv, whereClause, whereArgs);
            } catch (Exception e) {
                Log.w(TAG, "Insert into AppInfo error, " + e.getMessage());
                return 0;
            }
        }
    }
    
	public void setUpdateTimeByAppId(String appId,String updateTime) {
		if(!T.ckIsEmpty(appId) && !T.ckIsEmpty(updateTime)){
			synchronized (dbhelper) {
				ContentValues cv = new ContentValues();
				putValues(cv, COLUMN_LAST_MODIFIED_TIMESTAMP, updateTime);
				try {
					String whereClause = " `" + COLUMN_ID + "` = ? ";
					String[] whereArgs = new String[] { appId };
					SQLiteDatabase sqLiteDatabase = dbhelper.getWritableDatabase();
					sqLiteDatabase.update(TBL_NAME, cv, whereClause, whereArgs);
				} catch (Exception e) {
					return;
				} finally {
				    dbhelper.close();
				}
			}
		}else{
		    Log.d(TAG, "appid or updateTime is null");
		}
	}
	
	public String getUpdateTimeByAppId(String appId) {
		String updateTime = "";
		if(!T.ckIsEmpty(appId)){
			synchronized (dbhelper) {
				String[] columns = null;
				String whereClause = " " + COLUMN_ID + " = ? ";
				String[] whereArgs = {appId};
				String groupBy = null;
				String having = null;
				String orderBy = null;
				String limit = null;
				try {
					SQLiteDatabase rDb = dbhelper.getReadableDatabase();
					Cursor c = rDb.query(TBL_NAME, columns, whereClause, whereArgs
							, groupBy, having, orderBy, limit);
					if (null != c) {
						c.moveToFirst();
						updateTime = getStringByName(c, COLUMN_LAST_MODIFIED_TIMESTAMP);
						c.close();
					}
				} catch (Exception e) {
				    Log.w(TAG, "getUpdateTimeByAppId cause exception");
					e.printStackTrace();
				} finally {
				    dbhelper.close();
				}
			}
		}
		return updateTime;
	}
	
}