package com.hly.component.download.tools;

import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.hly.component.download.db.DownloadInfo;

import tencent.ieg.mcross.UploadLog;

public class ReportDownloadEvent {	
    // 1 展示统计id
    public static final int TYPE_AD_SHOW = 1;
    // 2 点击统计
    public static final int TYPE_AD_CLICK = 2;
    // 3 下载统计
    public static final int TYPE_AD_DOWNLOAD = 3;
    
	public static void upload(Context context, HashMap<String, Object> pa){
	    if(pa.containsKey("downLoadInfo")) {
	        DownloadInfo downloadInfo = (DownloadInfo)pa.get("downLoadInfo");
	        Log.d("cloud", "[contendId:" + downloadInfo.contentId + ",iSchedualId:" + downloadInfo.iSchedualId + ",iFrame:" 
	                + downloadInfo.iFrame + "]");
	    }
		UploadLog.upload(context, FormatDownloadLog.getInstance().formReport(pa).toByteArray());
	}
}
