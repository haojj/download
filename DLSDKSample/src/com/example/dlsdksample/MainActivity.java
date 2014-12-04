package com.example.dlsdksample;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.hly.component.download.MDownloadManager;

public class MainActivity extends Activity implements OnClickListener {
	private String url1 = "http://download.rdm.ext.wsd.com/dailybuild/DownLoadArtifactServlet?jobId=6484&buildId=null&hudsonBuildId=8&artifactRealPath=bin/MSDKSample-development-2.3.1.8_48607.apk&fileName=MSDKSample-development-2.3.1.8_48607.apk&key=1416480556198";
	private String url2 = "http://mdc.html5.qq.com/d/directdown.jsp?channel_id=21380";
	private String url3 = "http://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.load_main);
		
        //ApkDownloadManager.getInstance().setKalaDownloadListener(kalaDownloadListener);
        //ApkDownloadManager.getInstance().setOnDownloadNumChangeListener(onDownloadNumChangeListener);
        
        Button btn1 = (Button) findViewById(R.id.button1);
        Button btn2 = (Button) findViewById(R.id.button2);
        Button btn3 = (Button) findViewById(R.id.button3);
        Button btn4 = (Button) findViewById(R.id.button4);
        Button btn5 = (Button) findViewById(R.id.button5);
        Button btn6 = (Button) findViewById(R.id.button6);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        
        MDownloadManager.startApp(this);
	}

		
	@Override
	public void onClick(View v) {
        	if(v.getId()== R.id.button1)
            {
                /*Intent intent = new Intent(this, DownloadService.class);
                intent.putExtra(DownloadService.KEY_CMD, DownloadService.CMD_START_DOWNLOAD);
                DownloadInfo info = new DownloadInfo(new Identify(url1, 0, 1));
                intent.putExtra(DownloadService.KEY_IDENTIFY, info);
                startService(intent);*/
                String url = "http://113.17.172.24/dlied5.myapp.com/myapp/ssgame/10000145_1415340188_SSGame_Android_v1.7.0.41107.protect.apk?mkey=546f20850ddbbf41&f=a00e&p=.apk";
                MDownloadManager.enqueueApk(MainActivity.this, url, "button1");
            }
        	else if (v.getId()== R.id.button2)
            {
                /*Intent intent = new Intent(this, DownloadService.class);
                intent.putExtra(DownloadService.KEY_CMD, DownloadService.CMD_START_DOWNLOAD);
                DownloadInfo info = new DownloadInfo(new Identify(url2, 0, 1));
                intent.putExtra(DownloadService.KEY_IDENTIFY, info);
                startService(intent);*/
                String url = "http://dlied5.myapp.com/myapp/vxd/2017_1416216846_VXDGame_Android_Public_1.6.90.1_full.apk";
                MDownloadManager.enqueueApk(MainActivity.this, url, "button2");
   
            }
        	else if (v.getId()== R.id.button3)
            {
                String url = "http://dlied5.qq.com/wegame/werun/BreezeGame_Android-2014-10-30_14-51-19-CI-47-ChannelID-2017.apk";
                MDownloadManager.enqueueApk(MainActivity.this, url, "button3");
                /*Intent intent = new Intent(this, DownloadService.class);
                intent.putExtra(DownloadService.KEY_CMD, DownloadService.CMD_START_DOWNLOAD);
                DownloadInfo info = new DownloadInfo(new Identify(url3, 0, 1));
                intent.putExtra(DownloadService.KEY_IDENTIFY, info);
                startService(intent);*/
  
            }
        	else if (v.getId()== R.id.button4)
            {
                /*Intent intent = new Intent(this, DownloadService.class);
                intent.putExtra(DownloadService.KEY_CMD, DownloadService.CMD_QUITE_DOWNLOAD);
                startService(intent);*/
                String url = "http://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk";
                url = "http://mdc.html5.qq.com/d/directdown.jsp?channel_id=21380";
                MDownloadManager.enqueueApk(MainActivity.this, url, "button4");
  
            }
        	else if (v.getId()== R.id.button5)
            {
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    String dataBaseDir = "/data/data/" + getPackageName()
                            + "/databases";
                    String sdcardDir = Environment
                            .getExternalStorageDirectory().toString()
                            + "/ADDownSDK/" + System.currentTimeMillis();
                    File destFile = new File(sdcardDir);

                    if (!destFile.exists()) {
                        destFile.mkdirs();
                    }
                    try {
                        FileUtils.copyDirectiory(dataBaseDir, sdcardDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "SD卡不存在",
                            Toast.LENGTH_SHORT).show();
                }

            }
        	else if (v.getId()== R.id.button6)
            {
            	android.os.Process.killProcess(android.os.Process.myPid());
            }
     }
	
}
