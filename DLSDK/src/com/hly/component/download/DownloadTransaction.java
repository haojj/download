/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hly.component.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;

import com.hly.component.download.MDownloadManager.Request;
import com.hly.component.download.tools.T;

import android.content.Context;
import android.util.Log;

/**
 * The SendTransaction is responsible for sending multimedia messages
 * (M-Send.req) to the MMSC server.  It:
 *
 * <ul>
 * <li>Loads the multimedia message from storage (Outbox).
 * <li>Packs M-Send.req and sends it.
 * <li>Retrieves confirmation data from the server  (M-Send.conf).
 * <li>Parses confirmation message and handles it.
 * <li>Moves sent multimedia message from Outbox to Sent.
 * <li>Notifies the TransactionService about successful completion.
 * </ul>
 */
public class DownloadTransaction extends Transaction implements Runnable {
    private static final String TAG = "HJJ";
    private static final boolean DEBUG = false;
    private static final int CONNECT_TIMEOUT = 15 * 1000;
    private static final int READ_TIMEOUT = 6 * 1000;
    
    
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
    
    private Thread mThread;
    private String mUri;
    private String mLocalUri;
    private String mUriMd5;
    private String mRedirectUri;
    private String mTitle;
    private long totalSize = 0;
    private long completeSize = 0;
    private String mQueryUri;
    private String mTempLocalUri;
    private int mAcceptNet = Request.NETWORK_WIFI;
    
    public DownloadTransaction(Context context, int transId, long id, String uri, String uri_md5, String localUri, 
            String redirectUri, String title, int acceptNet) {
        super(context, transId, id);
        this.mUri = uri;
        this.mUriMd5 = uri_md5;
        this.mLocalUri = localUri;
        this.mRedirectUri = redirectUri;
        this.mTitle = title;
        this.mAcceptNet = acceptNet;
        this.mTempLocalUri = mLocalUri + "_tmp";
    }

  //广告的透传字段
    @Override
    public void setAdReportInfo(int iSpaceId, int contentId, 
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
    
    public int getAcceptNet() {
        return this.mAcceptNet;
    }
    
    public long getTotalSize() {
        return this.totalSize;
    }

    public long getCompleteSize() {
        return this.completeSize;
    }

    public String getUri() {
        return this.mUri;
    }

    public String getUriMd5() {
        return this.mUriMd5;
    }

    public String getLocalUri() {
        return this.mLocalUri;
    }

    public String getRedirectUri() {
        return this.mRedirectUri;
    }

    public String getQueryUri() {
        return this.mQueryUri;
    }

    public String getTitle() {
        return this.mTitle;
    }
    
    @Override
    public void process() {
        isRunning = true;
        isCancel = false;
        mThread = new Thread(this);
        mThread.start();
    }
    
    public HttpURLConnection getHttpConnetion(String url) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestMethod("GET");
        return connection;
    }
    
    private void getRedirectUrl(final String url) throws ClientProtocolException, IOException{
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.setRedirectHandler(new RedirectHandler(){

            @Override
            public URI getLocationURI(HttpResponse response, HttpContext context)
                    throws ProtocolException {
                return null;
            }

            @Override
            public boolean isRedirectRequested(HttpResponse response,
                    HttpContext context) {
                if(302 == response.getStatusLine().getStatusCode() || 301 == response.getStatusLine().getStatusCode()){
                    Header[] headers =  response.getHeaders("Location");
                    if(headers != null && headers.length > 0){
                        mQueryUri = headers[0].getValue();
                        Log.d(TAG, "mQueryUri:" + mQueryUri);
                        if(!T.ckIsEmpty(mQueryUri) && !mQueryUri.equals(mRedirectUri)){
                            mRedirectUri = mQueryUri;
                            notifyUrlChange();
                        }
                    }
                }
                return false;
            }});
        HttpGet request = new HttpGet(url);
        httpClient.execute(request);
    }
    
    public void run() {
        // TODO 注意将此处设为Daemon进程
        InputStream is = null;
        HttpURLConnection conn = null;
        RandomAccessFile randomFile = null;
        File tmpFile = null;
        try {
            // 此处处理下载的uri
            tmpFile = new File(mTempLocalUri);
            File parentFile = tmpFile.getParentFile();
            if(!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if(!tmpFile.exists()) {
                tmpFile.createNewFile();
            }
            randomFile = new RandomAccessFile(mTempLocalUri, "rw");
            long fileLength = randomFile.length(); 
            completeSize = fileLength;
            
            if(isCancel) {
                return;
            }
            String connUrl = mUri;
            // 此处做一次跳转的查询，如果查询到的结果与uri一致则逻辑不变，如果不一致，则删除临时文件重新开始
            // getRedirectUrl(connUrl);
            if(!T.ckIsEmpty(mRedirectUri)) {
                connUrl = mRedirectUri;
            }
            conn = getHttpConnetion(connUrl);
            conn.setRequestProperty("range","bytes=" + fileLength + "-"); 
            conn.connect(); 
            
            int contentLength = conn.getContentLength();
            totalSize = completeSize + contentLength;
            if (contentLength == -1 || contentLength > 0) {
                // 将写文件指针移到文件尾。   
                randomFile.seek(fileLength);
                byte[] buffer = new byte[8192];
                is = conn.getInputStream();
                int length = -1;
                while ((length = is.read(buffer)) != -1) {
                    if(isCancel) {
                        return;
                    }
                    randomFile.write(buffer, 0, length);
                    completeSize += length;
                    notifyProgress(length);
                }
            }
            mTransactionState.setState(TransactionState.SUCCESS);
        } catch (Throwable t) {
            Log.w(TAG, Log.getStackTraceString(t));
        } finally {
            isRunning = false;
            isCancel = false;
            try {
                if (randomFile != null) {
                    randomFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(conn != null) {
                conn.disconnect();
            }
            
            if (mTransactionState.getState() != TransactionState.SUCCESS) {
                mTransactionState.setState(TransactionState.FAILED);
                Log.e(TAG, "Delivery failed.");
            } else {
                if (tmpFile == null) {
                    mTransactionState.setState(TransactionState.FAILED);
                } else {
                    File localFile = new File(this.mLocalUri);
                    boolean flag = tmpFile.renameTo(localFile);
                    if (flag) {
                        Log.d(TAG, "rename pic succ：" + this.mLocalUri);
                    } else {
                        mTransactionState.setState(TransactionState.FAILED);
                        Log.d(TAG, "rename pic failed：" + this.mLocalUri);
                    }
                }
            }
            notifyObservers();
        }
    }

    private int mSumProgress = 0;
    @Override
    public void notifyProgress(int progressBytes) {
        mSumProgress += progressBytes;
        if(mSumProgress > (int)(totalSize * 0.01) ) {
            mSumProgress = 0;
            super.notifyProgress(progressBytes);
        }
    }
    
    @Override
    public int getType() {
        return DOWNLOAD_TRANSACTION;
    }

    @Override
    public void makeFailure() {
    }

    @Override
    public int getProgress() {
        if(totalSize == 0) {
            return 0;
        }
        return (int) ((float) completeSize * 100 / totalSize );
    }

    @Override
    public void reStart() {
        isCancel = false;
        isRunning = true;
        completeSize = totalSize = 0;
        mThread = new Thread(this);
        mThread.start();
    }

}
