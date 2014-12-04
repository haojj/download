package com.hly.component.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hly.component.download.db.DownloadDBModel;
import com.hly.component.download.db.DownloadInfo;
import com.hly.component.download.tools.T;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;

public class MDownloadManager {
    public static final String DOWNLOAD_DIR = "/download/";
    
    // 刚启动应用时也应该进行一次,类似开机启动的调用
    public static void startApp(Context context){
        context.startService(new Intent(context,
                TranscationService.class));
    }
    
    // 当前不开放保存路径的设置，暂时只允许wifi下使用
    public static long enqueueApk(Context context, String url, String title){
        return enqueue(context, url, "apk", title, Request.NETWORK_WIFI,0 ,0 ,0 ,0 ,0 ,
        		0 ,0 ,0 ,"0", "0", "1");//默认WIfi
    }
    
    public static long enqueue(Context context, String url, String mediaType, String title, int allowNet,
    		int iSpaceId, int contentId, 
    		int iPayType, int iProviderId, int iCustomerId, int iAppId,
    		int iSchedualId, int iFrame, String assistantId, 
    		String appkey, String adVersion){
        if (T.ckIsEmpty(url)) {
            throw new NullPointerException();
        }
        Uri uri = Uri.parse(url);
        Request request = new Request(uri, mediaType);
        request.setTitle(title);
        request.setMimeType(mediaType);
        request.setDestinationInExternalPublicDir(DOWNLOAD_DIR);
        request.setAllowedNetworkTypes(allowNet);
        request.setAdReportInfo(iSpaceId, contentId, iPayType, iProviderId, iCustomerId, iAppId, iSchedualId,
        		iFrame, assistantId, appkey, adVersion);
        return enqueue(context, request);
    }
    
    // -1 表示添加时数据库出问题，重试
    public static long enqueue(Context context, Request request){
        // 先保存到数据库
        DownloadDBModel dbModel = new DownloadDBModel(context);
        DownloadInfo info = request.toDownloadInfo();
        long id = dbModel.add(info);
        info.id = id;
        
        // 添加queue到service中
        if(id > 0) {
            TransactionBundle tBundle = new TransactionBundle(info);
            Intent i = new Intent(context, TranscationService.class);
            i.putExtras(tBundle.getBundle());
            context.startService(i);
        }
        return id;
    }
    
    public static class Request {
        public static final int NETWORK_MOBILE = 1 << 0; // 1
        public static final int NETWORK_WIFI = 1 << 1; // 2

        private Uri mUri;
        private String mUri_md5;
        private String mDestinationPath;
        private List<Pair<String, String>> mRequestHeaders = new ArrayList<Pair<String, String>>();
        private String mTitle;
        private String mDescription;
        private boolean mShowNotification = true;
        private String mMimeType;
        private boolean mRoamingAllowed = true;
        // default to all network types allowed
        private int mAllowedNetworkTypes = ~0; 
        private boolean mIsVisibleInDownloadsUi = true;

        //广告位Id
        public int iSpaceId = 0;
        //广告内容ID
        public int contentId = 0;
        //广告计费方式
        public int iPayType = 0;
        //广告主ID
        public int iProviderId = 0;
        //客户ID
        public int iCustomerId = 0;
        //APPID
        public int iAppId = 0;
        //广告排期
        public int iSchedualId = 0;
        // Int 轮播时内容显示在第几帧位上(直越小，显示越靠前)
        public int iFrame = 0;
        //装载广告的应用的appid
        public String assistantId = "0";
        //装载广告的应用的appkey
        public String appkey = "0";
        //广告SDK的版本
        public String adVersion = "3";
        /**
         * @param uri the HTTP URI to download.
         */
        public Request(Uri uri, String mediaType) {
            if (uri == null || T.ckIsEmpty(mediaType)) {
                throw new NullPointerException();
            }
            String scheme = uri.getScheme();
            if (scheme == null || !scheme.equals("http")) {
                throw new IllegalArgumentException("Can only download HTTP URIs: " + uri);
            }
            mUri = uri;
            mUri_md5 = T.getMD5String(uri.toString());
        }

        /**
         * Set the local destination for the downloaded file. Must be a file URI to a path on
         * external storage, and the calling application must have the WRITE_EXTERNAL_STORAGE
         * permission.
         *
         * By default, downloads are saved to a generated filename in the shared download cache and
         * may be deleted by the system at any time to reclaim space.
         *
         * @return this object
         */
        protected Request setDestinationUri(String path) {
            mDestinationPath = path;
            return this;
        }
        
        protected Request setDestinationInExternalFilesDir(Context context, String dirType,
                String subPath) {
            setDestinationFromBase(context.getExternalFilesDir(dirType), subPath);
            return this;
        }
        
        protected Request setDestinationInExternalPublicDir(String dirType) {
            setDestinationInExternalPublicDir(dirType, mUri_md5 + "." + mMimeType);
            return this;
        }
                
        protected Request setDestinationInExternalPublicDir(String dirType, String subPath) {
            setDestinationFromBase(Environment.getExternalStoragePublicDirectory(dirType), subPath);
            return this;
        }

        private void setDestinationFromBase(File base, String subPath) {
            if (subPath == null) {
                throw new NullPointerException("subPath cannot be null");
            }
            
            
            mDestinationPath = new File(base, subPath).toString() ;
        }
        //广告的透传字段
        protected void setAdReportInfo(int iSpaceId, int contentId, 
        		int iPayType, int iProviderId, int iCustomerId, int iAppId,
        		int iSchedualId, int iFrame, String assistantId, 
        		String appkey, String adVersion){
        	this.iSpaceId = iSpaceId;
        	this.contentId = contentId;
        	this.iPayType = iPayType;
        	this.iProviderId = iProviderId;
        	this.iCustomerId= iCustomerId;
        	this.iAppId= iAppId;
        	this.iSchedualId= iSchedualId;
        	this.iFrame= iFrame;
        	this.assistantId = assistantId;
        	this.appkey = appkey;
        	this.adVersion = adVersion;
        }
        
        /**
         * Add an HTTP header to be included with the download request.  The header will be added to
         * the end of the list.
         * @param header HTTP header name
         * @param value header value
         * @return this object
         * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2">HTTP/1.1
         *      Message Headers</a>
         */
        public Request addRequestHeader(String header, String value) {
            if (header == null) {
                throw new NullPointerException("header cannot be null");
            }
            if (header.contains(":") || header.contains(";")) {
                throw new IllegalArgumentException("header may not contain ':'");
            }
            if (value == null) {
                value = "";
            }
            mRequestHeaders.add(Pair.create(header, value));
            return this;
        }

        /**
         * Set the title of this download, to be displayed in notifications (if enabled).  If no
         * title is given, a default one will be assigned based on the download filename, once the
         * download starts.
         * @return this object
         */
        public Request setTitle(String title) {
            mTitle = title;
            return this;
        }

        /**
         * Set a description of this download, to be displayed in notifications (if enabled)
         * @return this object
         */
        public Request setDescription(String description) {
            mDescription = description;
            return this;
        }

        /**
         * Set the MIME content type of this download.  This will override the content type declared
         * in the server's response.
         * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.7">HTTP/1.1
         *      Media Types</a>
         * @return this object
         */
        public Request setMimeType(String mimeType) {
            mMimeType = mimeType;
            return this;
        }

        /**
         * Control whether a system notification is posted by the download manager while this
         * download is running. If enabled, the download manager posts notifications about downloads
         * through the system {@link android.app.NotificationManager}. By default, a notification is
         * shown.
         *
         * If set to false, this requires the permission
         * android.permission.DOWNLOAD_WITHOUT_NOTIFICATION.
         *
         * @param show whether the download manager should show a notification for this download.
         * @return this object
         */
        public Request setShowRunningNotification(boolean show) {
            mShowNotification = show;
            return this;
        }

        /**
         * Restrict the types of networks over which this download may proceed.  By default, all
         * network types are allowed.
         * @param flags any combination of the NETWORK_* bit flags.
         * @return this object
         */
        public Request setAllowedNetworkTypes(int flags) {
            mAllowedNetworkTypes = flags;
            return this;
        }

        /**
         * Set whether this download may proceed over a roaming connection.  By default, roaming is
         * allowed.
         * @param allowed whether to allow a roaming connection to be used
         * @return this object
         */
        public Request setAllowedOverRoaming(boolean allowed) {
            mRoamingAllowed = allowed;
            return this;
        }

        /**
         * Set whether this download should be displayed in the system's Downloads UI. True by
         * default.
         * @param isVisible whether to display this download in the Downloads UI
         * @return this object
         */
        public Request setVisibleInDownloadsUi(boolean isVisible) {
            mIsVisibleInDownloadsUi = isVisible;
            return this;
        }

        private void encodeHttpHeaders(ContentValues values) {
            int index = 0;
            for (Pair<String, String> header : mRequestHeaders) {
                String headerString = header.first + ": " + header.second;
                values.put("" + index, headerString);
                index++;
            }
        }

        private void putIfNonNull(ContentValues contentValues, String key, Object value) {
            if (value != null) {
                contentValues.put(key, value.toString());
            }
        }
        
        DownloadInfo toDownloadInfo() {
            DownloadInfo info = new DownloadInfo();
            info.uri = mUri.toString();
            info.md5 = mUri_md5;
            info.title = mTitle;
            info.desc = mDescription;
            info.media_type = mMimeType;
            info.local_uri = mDestinationPath;
            info.create_stamp = info.lastmod_stamp = System.currentTimeMillis();
            info.acceptNet = mAllowedNetworkTypes;
            info.iSpaceId = iSpaceId;
            info.contentId = contentId;
            info.iPayType = iPayType;
            info.iProviderId = iProviderId;
            info.iCustomerId= iCustomerId;
            info.iAppId= iAppId;
            info.iSchedualId= iSchedualId;
            info.iFrame= iFrame;
            info.adVersion = adVersion;
            info.appkey = appkey;
            info.assistantId = assistantId;
            return info;
        }
    }
}
