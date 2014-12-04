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

import android.content.Context;

/**
 * Transaction is an abstract class for notification transaction, send transaction
 * and other transactions described in MMS spec.
 * It provides the interfaces of them and some common methods for them.
 */
public abstract class Transaction extends Observable {
    private static final String TAG = "HJJ";
    private final int mServiceId;

    protected Context mContext;
    protected long mId;
    protected TransactionState mTransactionState;
    // 判断在完成后是否能够停止服务
    protected boolean mCanStopService = false;
    
    protected boolean isRunning = false;
    protected boolean isCancel = false;
    
    public long getId() {
        return this.mId;
    }
    
    public void stop(){
        if(isRunning) {
            isCancel = true;
        }
     }
    
    /**
     * Identifies push requests.
     */
    public static final int NOTIFICATION_TRANSACTION = 0;
    /**
     * Identifies deferred retrieve requests.
     */
    public static final int DOWNLOAD_TRANSACTION     = 1;

    public Transaction(Context context, int serviceId, long id) {
        mContext = context;
        mId = id;
        mTransactionState = new TransactionState();
        mServiceId = serviceId;
    }

    /**
     * Returns the transaction state of this transaction.
     *
     * @return Current state of the Transaction.
     */
    @Override
    public TransactionState getState() {
        return mTransactionState;
    }

    /**
     * An instance of Transaction encapsulates the actions required
     * during a MMS Client transaction.
     */
    public abstract void process();
    
    public abstract void reStart();
    
    public abstract int getProgress();

    public abstract void setAdReportInfo(int iSpaceId, int contentId, 
    		int iPayType, int iProviderId, int iCustomerId, int iAppId,
    		int iSchedualId, int iFrame, String assistantId, 
    		String appkey, String adVersion);
    /**
     * Used to determine whether a transaction is equivalent to this instance.
     *
     * @param transaction the transaction which is compared to this instance.
     * @return true if transaction is equivalent to this instance, false otherwise.
     */
    public boolean isEquivalent(Transaction transaction) {
        return getClass().equals(transaction.getClass())
                && mId == transaction.mId;
    }

    /**
     * Get the service-id of this transaction which was assigned by the framework.
     * @return the service-id of the transaction
     */
    public int getServiceId() {
        return mServiceId;
    }

    @Override
    public String toString() {
        return getClass().getName() + ": serviceId=" + mServiceId + " id=" + mId;
    }

    /**
     * Get the type of the transaction.
     *
     * @return Transaction type in integer.
     */
    abstract public int getType();
    
    /**
     * 用于失败
     */
    abstract public void makeFailure();
}
