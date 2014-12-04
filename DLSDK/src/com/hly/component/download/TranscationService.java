package com.hly.component.download;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.hly.component.download.MDownloadManager.Request;
import com.hly.component.download.db.DownloadDBModel;
import com.hly.component.download.db.DownloadInfo;
import com.hly.component.download.tools.NoticeResID;
import com.hly.component.download.tools.ReportDownloadEvent;
import com.hly.component.download.tools.T;

import android.R.integer;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class TranscationService extends Service implements Observer{
    private static final String TAG = "HJJ";
    private static final String TYPE_INSTALL = "application/vnd.android.package-archive";
    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;
    private final ArrayList<Transaction> mProcessing  = new ArrayList<Transaction>();
    // 可用于网络切换时处理，部分不允许在wifi网络下下载
    private final ArrayList<Transaction> mPending  = new ArrayList<Transaction>();
    // TODO：当批量处理时，serviceid不会去关闭，后续优化该逻辑，所以退出的时间有stopself()
    private HashMap<Integer, HashSet<Integer>> mServiceIdMap = new HashMap<Integer, HashSet<Integer>>();
    // 用于处理notification
    private HashMap<Long, Notification> mNotificationMap = new HashMap<Long, Notification>();
    
    private ConnectivityManager mConnMgr;
    private NotificationManager mNotificationManager;
    
    private static final int EVENT_TRANSACTION_REQUEST = 1;
    private static final int EVENT_DATA_STATE_CHANGED = 2;
    private static final int EVENT_TRANSACTION_RESTART = 3;
    private static final int EVENT_QUIT = 100;
    
    private ConnectivityBroadcastReceiver mReceiver;
    private DownloadDBModel mDBModel;
    
    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                Log.w("raw", "!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)");
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo)
                intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            
            if (!networkInfo.isConnected()) {
                synchronized (mProcessing) {
                    for(int i=0; i<mProcessing.size(); i++) {
                        Transaction tr = mProcessing.get(i);
                        tr.stop();
                        mPending.add(mProcessing.remove(i));
                    }
                }
                return;
            }
            
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // pedding下的事务可以考虑在wifi下面执行
                TranscationService.this.mServiceHandler.processPendingTransaction();
            } else {
                // 部分非wifi下的事务需要停止,转入pedding进行处理
                synchronized (mProcessing) {
                    for(int i=0; i<mProcessing.size(); i++) {
                        Transaction tr = mProcessing.get(i);
                        if(((DownloadTransaction) tr).getAcceptNet() == Request.NETWORK_WIFI) {
                            tr.stop();
                            mPending.add(mProcessing.remove(i));
                        }
                    }
                } 
            }
        }
    }
    
    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("TransactionService");
        thread.start();

        mDBModel = new DownloadDBModel(getApplicationContext());
        
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mReceiver = new ConnectivityBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, intentFilter);
        
        // 获取当前网络状况
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
    private boolean isNetworkAvailable() {
        NetworkInfo info = mConnMgr.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }
        return info.isAvailable();
    }
    
    private boolean isWifiAvailable() {
        NetworkInfo info = mConnMgr.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }
        if(info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand.....");
        if (intent == null) {
            return Service.START_NOT_STICKY;
        }
        
        // 如果是从receiver中启动的，则先进行数据库的查询，得到transcation bundle
        // 另一种是程序直接startService传递过来的trascation
        ArrayList<TransactionBundle> bundleList = new ArrayList<TransactionBundle>();
        if (intent.getExtras() == null) {
            ArrayList<DownloadInfo> infos = mDBModel.queryAll();
            if (infos.size() == 0) {
                Log.d(TAG, "onStart: no pending messages. Stopping service.");
                stopSelfIfIdle(startId);
                return Service.START_NOT_STICKY;
            } else {
                for(int i=0; i < infos.size(); i++) {
                    TransactionBundle args = new TransactionBundle(infos.get(i));
                    // 成功且存在无需重新下载
                    if(args.getStatus() == DownloadConstants.STATUS_SUCCESSFUL) {
                        String localFileStr = args.getLocalUri();
                        File localFile = new File(localFileStr);
                        if(localFile.exists()) {
                        	Toast.makeText(getApplicationContext(), "已下载", Toast.LENGTH_SHORT).show();
                        	installApk(localFile);
                            return Service.START_NOT_STICKY;
                        }
                    }
                    bundleList.add(args);
                }
                launchTransaction(startId, bundleList);
            }
        } else {
            Log.v(TAG, "onStart: launch transaction...");
            // For launching NotificationTransaction and test purpose.
            TransactionBundle args = new TransactionBundle(intent.getExtras());
            if(args.getStatus() == DownloadConstants.STATUS_SUCCESSFUL) {
                String localFileStr = args.getLocalUri();
                Log.v(TAG, "localFileStr"+localFileStr);
                File localFile = new File(localFileStr);
                if(localFile.exists()) {
                	Toast.makeText(getApplicationContext(), "已下载", Toast.LENGTH_SHORT).show();
                	installApk(localFile);
                    stopSelfIfIdle(startId);
                    Log.d(TAG, "START_NOT_STICKY");
                    return Service.START_NOT_STICKY;
                }
            }
            
            bundleList.add(args);
            launchTransaction(startId, bundleList);
        }
        return Service.START_NOT_STICKY;
    }
    
    private void launchTransaction(int serviceId, ArrayList<TransactionBundle> txnBundleList) {
        boolean noNetwork = !isNetworkAvailable();
        if (noNetwork) {
            Log.w(TAG, "launchTransaction: no network error!");
            return;
        }
        // 如果是在pedding列表中，应该移除，并且重新开始
        Transaction t = null;
        synchronized (mProcessing) {
            for(TransactionBundle args : txnBundleList) {
                for (int i = 0; i < mPending.size(); i++) {
                    t = mPending.get(i);
                    if (t.getId() == args.getTransactionId()) {
                        ((DownloadTransaction) t).detach(TranscationService.this);
                        mPending.remove(i);
                        break;
                    }
                }
            }
        }
        Message msg = mServiceHandler.obtainMessage(EVENT_TRANSACTION_REQUEST);
        msg.arg1 = serviceId;
        msg.obj =  txnBundleList;

        Log.d(TAG, "launchTransaction: sending message " + msg);
        mServiceHandler.sendMessage(msg);
    }
    
    private void stopSelfIfIdle(int startId) {
        synchronized (mProcessing) {
            if (mProcessing.isEmpty() && mPending.isEmpty()) {
                stopSelf(startId);
            }
        }
    }
    
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        private String decodeMessage(Message msg) {
            if (msg.what == EVENT_QUIT) {
                return "EVENT_QUIT";
            } else if (msg.what == EVENT_TRANSACTION_REQUEST) {
                return "EVENT_TRANSACTION_REQUEST";
            } else if (msg.what == EVENT_TRANSACTION_REQUEST) {
                return "EVENT_TRANSACTION_REQUEST";
            } 
            return "unknown message.what";
        }

        @Override
        public void handleMessage(Message msg) {
            Transaction transaction = null;

            switch (msg.what) {
                case EVENT_QUIT:
                    getLooper().quit();
                    stopSelf();
                    return;
                case EVENT_DATA_STATE_CHANGED:
                    // 预留，后续可能会用到状态改变的情况
                    synchronized (mProcessing) {
                        if (mProcessing.isEmpty()) {
                            return;
                        }
                    }
                    return;
                case EVENT_TRANSACTION_RESTART:
                    Transaction tran = (Transaction) msg.obj;
                    int deleteFile = msg.arg2;
                    if(deleteFile == 1) {
                        if(tran instanceof DownloadTransaction) {
                            String local = ((DownloadTransaction) tran).getLocalUri();
                            if(!T.ckIsEmpty(local)) {
                                File f = new File(local);
                                if(f.exists()) {
                                    f.delete();
                                }
                            }
                        }
                    }
                    tran.reStart();
                    return;
                case EVENT_TRANSACTION_REQUEST:
                    int serviceId = msg.arg1;
                    int succeedInTranscation = 0;
                    try {
                        ArrayList<TransactionBundle> transactionBundles = (ArrayList<TransactionBundle>) msg.obj;
                        int size = transactionBundles.size();
                        for(int i = 0; i< size; i++) {
                            TransactionBundle b = transactionBundles.get(i);
                            int transactionType = b.getTransactionType();
                            Log.d(TAG, "transactionType:"+transactionType);
                            switch (transactionType) {
                                case Transaction.NOTIFICATION_TRANSACTION:
                                    transaction = null;
                                    continue;
                                case Transaction.DOWNLOAD_TRANSACTION:
                                    transaction = new DownloadTransaction(getApplicationContext(), serviceId, b.getTransactionId(),
                                            b.getUri(), b.getUriMD5(), b.getLocalUri(), b.getRedirectUri(), b.getTitle(), b.getAcceptNetType());
                                    if(size == 0) {
                                        transaction.mCanStopService = true;
                                    }
                                    transaction.setAdReportInfo(b.getSpaceId(), b.getContentId(), b.getPayType(),
                                    		b.getProviderId(), b.getCustomerId(), b.getAppId(), b.getSchedualId(),
                                    		b.getFrame(), b.getAssistantId(), b.getAppkey(), b.getAdVersion());
                                    break;
                                default:
                                    Log.w(TAG, "Invalid transaction type: " + serviceId);
                                    transaction = null;
                                    continue;
                            }
                            if (!processTransaction(transaction)) {
                                transaction = null;
                                continue;
                            } else {
                                succeedInTranscation++;
                            }
                        }
                    } catch (Exception ex) {
                        Log.w(TAG, "Exception occurred while handling message: " + msg, ex);
                        if (transaction != null) {
                            try {
                                transaction.detach(TranscationService.this);
                                if (mProcessing.contains(transaction)) {
                                    synchronized (mProcessing) {
                                        mProcessing.remove(transaction);
                                    }
                                }
                            } catch (Throwable t) {
                                Log.e(TAG, "Unexpected Throwable.", t);
                            } finally {
                                // Set transaction to null to allow stopping the
                                // transaction service.
                                transaction = null;
                            }
                        }
                    } finally {
                        if (succeedInTranscation == 0) {
                            stopSelf(serviceId);
                        }
                    }
                    return;
                default:
                    Log.w(TAG, "what=" + msg.what);
                    return;
            }
        }

        private void makeAllTransactionFailure() {
            synchronized (mProcessing) {
                while (!mPending.isEmpty()) {
                    Transaction tran = mPending.remove(0);
                    tran.makeFailure();
                    int serviceId = tran.getServiceId();
                    Log.d(TAG, "transaction make failure. transaction serviceId = "+serviceId);
                    tran.detach(TranscationService.this);
                    stopSelf(serviceId);
                }
            }
        }

        private void processPendingTransaction() {
            Transaction transaction = null;
            synchronized (mProcessing) {
                for(int i=0; i<mPending.size(); i++) {
                    transaction = mPending.remove(i);
                    for (Transaction t : mProcessing) {
                        if (t.isEquivalent(transaction)) {
                        	Toast.makeText(getApplicationContext(), "正在等待下载", Toast.LENGTH_SHORT).show();
                            Log.v(TAG, "Duplicated transaction: " + transaction.getServiceId());
                            continue;
                        }
                    }
                    mProcessing.add(transaction);
                    transaction.reStart();
                }
            }
        }

        private boolean processTransaction(Transaction transaction) throws IOException {
            if(transaction == null) {
                return false;
            }
			Log.d(TAG, "processTransaction:");
			boolean proeceed = false;
            // Check if transaction already processing
            synchronized (mProcessing) {
                for (Transaction t : mPending) {
                    if (t.isEquivalent(transaction)) {
                        Log.v(TAG, "Transaction already pending: " +
                                transaction.getServiceId());
                        return true;
                    }
                }
                for (Transaction t : mProcessing) {
                    if (t.isEquivalent(transaction)) {
                    	Toast.makeText(getApplicationContext(), "正在下载", Toast.LENGTH_SHORT).show();
                        Log.v(TAG, "Duplicated transaction: " + transaction.getServiceId());
                        return true;
                    }
                }
                
                if(isWifiAvailable()){
                    proeceed = true;
                    mProcessing.add(transaction);
                } else {
                    if(((DownloadTransaction) transaction).getAcceptNet() == Request.NETWORK_WIFI) {
                        mPending.add(transaction);
                    } else {
                        proeceed = true;
                        mProcessing.add(transaction);
                    }
                }
            }

            if(proeceed) {
                // Attach to transaction and process it
                transaction.attach(TranscationService.this);
                transaction.process();
            }
            return true;
        }
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "Destroying TransactionService");
        if (!mPending.isEmpty()) {
            Log.w(TAG, "TransactionService exiting with transaction still pending");
            for (int i=0; i<mPending.size() ;i++) {
                Transaction tran = mPending.remove(0);
                tran.stop();
                tran.detach(TranscationService.this);
            }
        }
        if(!mProcessing.isEmpty()) {
            Log.w(TAG, "TransactionService exiting with transaction still mProcessing");
            for (int i=0; i<mProcessing.size() ;i++) {
                Transaction tran = mPending.remove(0);
                tran.stop();
                tran.detach(TranscationService.this);
            }
        }
        mNotificationMap.clear();
        unregisterReceiver(mReceiver);
        mServiceHandler.sendEmptyMessage(EVENT_QUIT);
    }
    
    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    protected void installApk(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);

        /* 调用getMIMEType()来取得MimeType */
        String type = "application/vnd.android.package-archive";
        /* 设置intent的file与MimeType */
        intent.setDataAndType(Uri.fromFile(file), type);
        getApplicationContext().startActivity(intent);
    }
    
    @Override
    public void update(Observable observable) {
        // 此时要更新数据库中totalsize completesize,status，如果有错误还要更新错误 
        // TODO: 错误集锦
        if (observable instanceof DownloadTransaction) {
            DownloadTransaction tr = (DownloadTransaction) observable;
            int state = tr.mTransactionState.getState();
            int update = 0;
            if(state == TransactionState.FAILED) {
                update = mDBModel.updateStateById(tr.getId(), tr.getTotalSize(), tr.getCompleteSize(), DownloadConstants.STATUS_FAILED, 
                        DownloadConstants.ERROR_UNKNOWN, "");
            } else if(state == TransactionState.SUCCESS){
                update = mDBModel.updateStateById(tr.getId(), tr.getTotalSize(), tr.getCompleteSize(), DownloadConstants.STATUS_SUCCESSFUL, 
                        0, "");
                finishNotification(tr);
                installApk(new File(tr.getLocalUri()));
                cancelNotification(tr);
                
                reportDownloadInfo(tr);
                
            } else {
                update = mDBModel.updateStateById(tr.getId(), tr.getTotalSize(), tr.getCompleteSize(), DownloadConstants.STATUS_PENDING, 
                        0, "");
            }
            
            Log.d(TAG, "update:" + update);
            synchronized (mProcessing) {
                mProcessing.remove(tr);
                if(state == TransactionState.SUCCESS) {
                    tr.detach(TranscationService.this);
                    if(tr.mCanStopService) {
                        stopSelf(tr.getServiceId());
                    }
                } else {
                    tr.stop();
                    mPending.add(tr);
                }
            }
            
        }
    }

    private void reportDownloadInfo(DownloadTransaction tr){
    	
    	 HashMap<String, Object> params = new HashMap<String, Object>();
    	 long timestamp = System.currentTimeMillis() / 1000;
    	 params.put("spaceId", tr.iSpaceId);
    	 params.put("contentId", tr.contentId);
    	 params.put("payType", tr.iPayType);
    	 params.put("providerId", tr.iProviderId);
    	 params.put("customerId", tr.iCustomerId);
    	 params.put("appId", tr.iAppId);
    	 params.put("schedualId", tr.iSchedualId);
    	 params.put("frame", tr.iFrame);
    	 params.put("assistantId", tr.assistantId);
    	 params.put("appkey", tr.appkey);
    	 params.put("adVersion", tr.adVersion);
    	 
    	 params.put("netStat", getNetState());
    	 params.put("matId", getMatId());
    	 params.put("screenDpi", getScreenDpi());
    	 params.put("resolution", getScreenWidth() + "*" + getScreenHeight());
    	 
         params.put("logType", ReportDownloadEvent.TYPE_AD_DOWNLOAD);
         params.put("upload_filesize", tr.getCompleteSize());
         params.put("upload_status", 1L);
         //TODO: 速度后面再计算
         params.put("upload_speed", 20);
         params.put("upload_timestamp", timestamp);
         
         ReportDownloadEvent.upload(getApplicationContext(), params);
         
    }
    
    public void cancelNotification(DownloadTransaction tr){
    	int id = (int)tr.getId();
    	mNotificationManager.cancel(id);
    	if(mNotificationMap.containsKey(id)){
			mNotificationMap.remove(id);
		}
	}
    
    private long getNetState() {
    	ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()  
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
    	return (long)(networkInfo.getType());
    }
    
    private int getScreenWidth() {
        android.view.WindowManager manager = (android.view.WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getWidth();
    }

    private int getScreenHeight() {
        android.view.WindowManager manager = (android.view.WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        return manager.getDefaultDisplay().getHeight();
    }
    
    private String getMatId() {
    	return ((TelephonyManager)getApplicationContext().
    			getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }
    
    private int getScreenDpi() {
    	return getApplicationContext().getResources().
    			getDisplayMetrics().densityDpi;
    }
    
    private String getScreenResolution() {
    	return (getScreenWidth() + "*" + getScreenHeight());
    }
    
    @Override
    public void progress(Observable observable, int progressBytes) {
        if (observable instanceof DownloadTransaction) {
            DownloadTransaction tr = (DownloadTransaction) observable;
            int progress = tr.getProgress();
            long total = tr.getTotalSize();
            long completeSize = tr.getCompleteSize();
            // 此时要更新数据库中的totalsize completesize要更新
            Log.d("observable", "file:" + tr.getUri() + ",progress:" + progress + ", total:" + total + ", completeSize:" + completeSize);
            mDBModel.updateProgressById(tr.getId(), tr.getTotalSize(), tr.getCompleteSize(),
                    DownloadConstants.STATUS_RUNNING);
            sendNotification(tr);
        }
    }
    
    private void finishNotification(DownloadTransaction tr){
        if(mNotificationMap.containsKey(tr.getId())){
        	Log.d("raw", "finishNotification:"+tr.getLocalUri()+"id:"+tr.getId());
        	
        	int icon = NoticeResID.getIcon(getApplicationContext());
        	int progressBar = NoticeResID.getProgressbarId(getApplicationContext());
        	int tvProgress = NoticeResID.getTvProgress(getApplicationContext());
        	int tvState = NoticeResID.getTvState(getApplicationContext());
        	
        	
            Notification notification = mNotificationMap.get(tr.getId());
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            notification.icon = icon;
            notification.contentView.setViewVisibility(progressBar, View.GONE);
            notification.contentView.setViewVisibility(tvProgress, View.GONE);
            notification.contentView.setTextViewText(tvState, "下载完成点击安装");
            notification.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, getInstallIntent(tr.getLocalUri()), 0);
            mNotificationManager.notify((int) tr.getId(), notification);
            mNotificationMap.remove(tr.getId());
        }
    }
    
    private Intent getInstallIntent(String path){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(path)), TYPE_INSTALL);
        return intent;
    }
    
    private void sendNotification(DownloadTransaction tr){
        Notification notification = null;
        int progress = tr.getProgress();
        int icon = NoticeResID.getIcon(getApplicationContext());
    	int iconView = NoticeResID.getIconView(getApplicationContext());
    	int progressBar = NoticeResID.getProgressbarId(getApplicationContext());
    	int tvProgress = NoticeResID.getTvProgress(getApplicationContext());
    	int tvState = NoticeResID.getTvState(getApplicationContext());
    	int tvName = NoticeResID.getTvName(getApplicationContext());
    	int noticeLayout = NoticeResID.getNoticeLayout(getApplicationContext());
    	
        if(!mNotificationMap.containsKey(tr.getId())){
     	
            notification = new Notification(
                    android.R.drawable.stat_sys_download, tr.getTitle(), System.currentTimeMillis());
            notification.contentView = new RemoteViews(getPackageName(),
            		noticeLayout);
            notification.contentView.setTextViewText(tvName,
                    tr.getTitle());
            notification.contentView.setTextViewText(tvState,
                    "下载中……");
            notification.contentView.setImageViewBitmap(
            		iconView, BitmapFactory.decodeResource(getResources(), icon));
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            mNotificationMap.put(tr.getId(), notification);
        } else {
            notification = mNotificationMap.get(tr.getId());
        }
        
        notification.contentView.setProgressBar(
        		progressBar, 100, progress, false);
        notification.contentView.setTextViewText(
        		tvProgress, tr.getCompleteSize() + "/" + tr.getTotalSize());
        
        mNotificationManager.notify((int) tr.getId(), notification);
    }

    @Override
    public void urlChange(Observable observable) {
        if (observable instanceof DownloadTransaction) {
            DownloadTransaction tr = (DownloadTransaction) observable;
            tr.stop();
        
            // 此时要更新数据库中的url,redirecturl和totalsize completesize要更新
            int update = mDBModel.updateUrlChangeById(tr.getId(), tr.getTotalSize(), tr.getCompleteSize(), tr.getRedirectUri());
            
            // 重新开启下载
            Message msg = mServiceHandler.obtainMessage(EVENT_TRANSACTION_RESTART);
            msg.arg1 = tr.getServiceId();
            msg.arg2 = 1;
            msg.obj = tr;

            Log.d(TAG, "restart: sending message " + msg);
            // 3秒后再重新启动
            mServiceHandler.sendMessageDelayed(msg, 3*1000);
        }
    }

}
