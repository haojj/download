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

import com.hly.component.download.db.DownloadInfo;

import android.os.Bundle;

/**
 * A wrapper around the Bundle instances used to start the TransactionService.
 * It provides high-level APIs to set the information required for the latter to
 * instantiate transactions.
 */
public class TransactionBundle {
    public static final String TRANSACTION_TYPE = "type";
    public static final String TRANSACTION_ID = "id";
    public static final String TRANSACTION_URI = "uri";
    private static final String TRANSACTION_REDIRECT_URI = "redirect_uri";
    private static final String TRANSACTION_LOCAL_URI = "local_uri";
    private static final String TRANSACTION_URI_MD5 = "md5";
    private static final String TRANSACTION_ACCEPT_NET = "accept_net";
    private static final String TRANSACTION_TITLE = "title";
    private static final String TRANSACTION_STATUS = "status";
    
    private static final String TRANSACTION_SPACE_ID = "spaceId";
    private static final String TRANSACTION_CONTENT_ID = "contentId";
    private static final String TRANSACTION_PAY_TYPE = "iPayType";
    private static final String TRANSACTION_PROVIDER_ID = "iProviderId";
    private static final String TRANSACTION_CUSTOMER_ID = "iCustomerId";
    private static final String TRANSACTION_APP_ID = "iAppId";
    private static final String TRANSACTION_SCHEDUAL_ID = "iSchedualId";
    private static final String TRANSACTION_FRAME = "iFrame";
    private static final String TRANSACTION_ASSISTANT_ID = "assistantId";
    private static final String TRANSACTION_APP_KEY = "appkey";
    private static final String TRANSACTION_AD_VERSION = "adVersion";
    
    private final Bundle mBundle;
    
    public TransactionBundle(DownloadInfo info) {
        mBundle = new Bundle();
        mBundle.putInt(TRANSACTION_TYPE, Transaction.DOWNLOAD_TRANSACTION);
        mBundle.putLong(TRANSACTION_ID, info.id);
        mBundle.putString(TRANSACTION_URI, info.uri);
        mBundle.putString(TRANSACTION_REDIRECT_URI, info.redirect_uri);
        mBundle.putString(TRANSACTION_LOCAL_URI, info.local_uri);
        mBundle.putString(TRANSACTION_URI_MD5, info.md5);
        mBundle.putInt(TRANSACTION_ACCEPT_NET, info.acceptNet);
        mBundle.putString(TRANSACTION_TITLE, info.title);
        mBundle.putInt(TRANSACTION_STATUS, info.status);
        
        mBundle.putInt(TRANSACTION_SPACE_ID, info.iSpaceId);
        mBundle.putInt(TRANSACTION_CONTENT_ID, info.contentId);
        mBundle.putInt(TRANSACTION_PAY_TYPE, info.iPayType);
        mBundle.putInt(TRANSACTION_PROVIDER_ID, info.iProviderId);
        mBundle.putInt(TRANSACTION_CUSTOMER_ID, info.iCustomerId);
        mBundle.putInt(TRANSACTION_APP_ID, info.iAppId);
        mBundle.putInt(TRANSACTION_SCHEDUAL_ID, info.iSchedualId);
        mBundle.putInt(TRANSACTION_FRAME, info.iFrame);
        mBundle.putString(TRANSACTION_ASSISTANT_ID, info.assistantId);
        mBundle.putString(TRANSACTION_APP_KEY, info.appkey);
        mBundle.putString(TRANSACTION_AD_VERSION, info.adVersion);
        
    }

    public TransactionBundle(Bundle bundle) {
        mBundle = bundle;
    }

    public Bundle getBundle() {
        return mBundle;
    }

    public long getTransactionId() {
        return mBundle.getLong(TRANSACTION_ID);
    }
    
    public int getAcceptNetType() {
        return mBundle.getInt(TRANSACTION_ACCEPT_NET);
    }
    
    public int getStatus() {
        return mBundle.getInt(TRANSACTION_STATUS);
    }
    
    public int getTransactionType() {
        return mBundle.getInt(TRANSACTION_TYPE);
    }

    public String getTitle() {
        return mBundle.getString(TRANSACTION_TITLE);
    }
    
    public String getUri() {
        return mBundle.getString(TRANSACTION_URI);
    }
    
    public String getRedirectUri() {
        return mBundle.getString(TRANSACTION_REDIRECT_URI);
    }
    
    public String getLocalUri() {
        return mBundle.getString(TRANSACTION_LOCAL_URI);
    }

    public String getUriMD5() {
        return mBundle.getString(TRANSACTION_URI_MD5);
    }
    
    public int getSpaceId() {
        return mBundle.getInt(TRANSACTION_SPACE_ID);
    }
    public int getContentId() {
        return mBundle.getInt(TRANSACTION_CONTENT_ID);
    }
    public int getPayType() {
        return mBundle.getInt(TRANSACTION_PAY_TYPE);
    }
    public int getProviderId() {
        return mBundle.getInt(TRANSACTION_PROVIDER_ID);
    }
    public int getCustomerId() {
        return mBundle.getInt(TRANSACTION_CUSTOMER_ID);
    }
    public int getAppId() {
        return mBundle.getInt(TRANSACTION_APP_ID);
    }
    public int getSchedualId() {
        return mBundle.getInt(TRANSACTION_SCHEDUAL_ID);
    }
    public int getFrame() {
        return mBundle.getInt(TRANSACTION_FRAME);
    }
    public String getAssistantId() {
        return mBundle.getString(TRANSACTION_ASSISTANT_ID);
    }
    public String getAppkey() {
        return mBundle.getString(TRANSACTION_APP_KEY);
    }
    public String getAdVersion() {
        return mBundle.getString(TRANSACTION_AD_VERSION);
    }
}
