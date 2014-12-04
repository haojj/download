package com.hly.component.download.tools;

import android.R.integer;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;


public class NoticeResID extends ResID{
    
    public static int getProgressbarId(Context context){
    	int progress_bar_Id = 0;
        String packageName = context.getPackageName();
        Resources resources = context.getResources();
    
        progress_bar_Id = loadIdentifierResource(resources, "download_n_pb_progressbar", "id",
                    packageName);
        return progress_bar_Id;
    }
    
    public static int getTvProgress(Context context){
    	int tv_progress_Id = 0;
        String packageName = context.getPackageName();
        Resources resources = context.getResources();
    
        tv_progress_Id = loadIdentifierResource(resources, "download_n_tv_progress", "id",
                    packageName);
        return tv_progress_Id;
    }
    
    public static int getTvState(Context context){
    	int tv_state_id = 0;
        String packageName = context.getPackageName();
        Resources resources = context.getResources();
    
        tv_state_id = loadIdentifierResource(resources, "download_n_tv_state", "id",
                    packageName);
        return tv_state_id;
    }
    
    public static int getTvName(Context context){
    	int tv_name_id = 0;
        String packageName = context.getPackageName();
        Resources resources = context.getResources();
    
        tv_name_id = loadIdentifierResource(resources, "download_n_tv_name", "id",
                    packageName);
        return tv_name_id;
    }
    
    public static int getIconView(Context context){
    	int icon_imageview_id = 0;
        String packageName = context.getPackageName();
        Resources resources = context.getResources();
    
        icon_imageview_id = loadIdentifierResource(resources, "download_n_iv_icon", "id",
                    packageName);
        return icon_imageview_id;
    }
    
    public static int getIcon(Context context){
        int drawable_icon = 0;
        String packageName = context.getPackageName();
        Resources resources = context.getResources();
        
        drawable_icon = loadIdentifierResource(resources, "icon", "drawable",
                    packageName);

        return drawable_icon;
    }

	public static int getNoticeLayout(Context context) {
		// TODO Auto-generated method stub
		 int notice_layout = 0;
	        String packageName = context.getPackageName();
	        Resources resources = context.getResources();
	        
	        notice_layout = loadIdentifierResource(resources, "notification_downloading", "layout",
	                    packageName);

	        return notice_layout;
	}
}