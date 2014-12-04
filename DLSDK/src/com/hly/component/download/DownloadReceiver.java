package com.hly.component.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// 开机启动一次services
public class DownloadReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent action) {
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context,
                    TranscationService.class));
        }
    }

}
