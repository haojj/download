package com.hly.component.download.tools;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import tencent.ieg.mcross.MD5Util;
import tencent.ieg.mcross.McrossReport.Report;

import com.google.protobuf.micro.ByteStringMicro;
import com.hly.component.download.db.DownloadInfo;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FormatDownloadLog{
	
	private int logType=0;
	private int schedualId=0;
	private int spaceId=0;
	private int ScreenDpi=0;
	private int payType=0;
	private String jumpUrl="";
	private String picUrl="";
	private String open_id="";
	private String mMatId="";
	private String sessionId="";
	private long mOs=1;
	private String mOsVersion="";
	private String mTradeMark="" ;
	private String clientIp="";
	private String assistantId="0";
	private String appKey="0";
	private String ADVersion="0";
	private String resolution="";
	private long netStat= 0;
	private int iProviderId= 0;
	private int iCustomerId =0;
	private int iAppId =0;
	private int showType = 0 ;
	private long adTime =0;
	private int contentId=0;
	
	private long apkSize= 0;
	private long downloadSpeed=0;
	private long is_downloadSuccess=0;
	private long downloadCompleteTime=0;
	private int is_apkInstall=0;
	private long frame = 0;
	
	private final int sessionID_K;
	private final static int MAX_SESSION_K=500000000;
	private volatile static FormatDownloadLog instance;
	private MessageDigest md = null;
	public static FormatDownloadLog getInstance() {
		if (instance == null) {
			synchronized (FormatDownloadLog.class) {
				if (instance == null) {
					instance = new FormatDownloadLog();
				}
			}
		}
		return instance;
	}
	
	public FormatDownloadLog(){
		 super();
		 Random random = new Random();
		 sessionID_K = random.nextInt(MAX_SESSION_K);
		 mOsVersion = android.os.Build.VERSION.RELEASE;
		 mTradeMark = android.os.Build.MODEL;
	}
	
	public String getSessionId(){
		String originSessionId=mMatId+sessionID_K;
		String SessionId=md5_String(originSessionId);
		return SessionId;
	}
	
	private String getHostIp() {   
	    try { 
	        for (Enumeration<NetworkInterface> en = NetworkInterface 
	                .getNetworkInterfaces(); en.hasMoreElements();) { 
	            NetworkInterface intf = en.nextElement(); 
	            for (Enumeration<InetAddress> ipAddr = intf.getInetAddresses(); ipAddr 
	                    .hasMoreElements();) { 
	                InetAddress inetAddress = ipAddr.nextElement(); 
	                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
	                    //if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
	                        return inetAddress.getHostAddress().toString();
	                    }
	               }
	            } 
	    } catch (SocketException e) { 
	    	e.printStackTrace();
	    } catch (Exception e) { 
	    } 
	    return "null"; 
	} 
	
	
	public Report formReport(HashMap<String, Object> params)
	{	
		if(params.containsKey("spaceId")){
			spaceId = (Integer)params.get("spaceId");
		}
		if(params.containsKey("contentId")){
			contentId = (Integer)params.get("contentId");
		}
		if(params.containsKey("payType")){
			payType = (Integer)params.get("payType");
		}
		if(params.containsKey("providerId")){
			iProviderId = (Integer)params.get("providerId");
		}
		if(params.containsKey("customerId")){
			iCustomerId = (Integer)params.get("customerId");
		}
		if(params.containsKey("appId")){
			iAppId = (Integer)params.get("appId");
		}
		if(params.containsKey("schedualId")){
			schedualId = (Integer)params.get("schedualId");
		}
		if(params.containsKey("frame")){
			frame = (Integer)params.get("frame");
		}
		if(params.containsKey("assistantId")){
			assistantId = (String)params.get("assistantId");
		}
		if(params.containsKey("appkey")){
			appKey = (String)params.get("appkey");
		}
		if(params.containsKey("adVersion")){
			ADVersion = (String)params.get("adVersion");
		}
		
		if(params.containsKey("netStat")){
			netStat = (Long)params.get("netStat");
		}
		if(params.containsKey("matId")){
			mMatId = (String)params.get("matId");
		}
		sessionId=getSessionId();
		if(params.containsKey("screenDpi")){
			ScreenDpi = (Integer)params.get("screenDpi");
		}
		if(params.containsKey("resolution")){
			resolution = (String)params.get("resolution");
		}
		
		logType = (Integer)params.get("logType");
		Log.d("cloud", "report logtype=" + logType);
		if(params.containsKey("adtime")){
			adTime = (Long)params.get("adtime");
		}
		if(params.containsKey("download_filesize")){
			apkSize = (Long)params.get("download_filesize");
		}
		if(params.containsKey("download_status")){
			is_downloadSuccess = (Long)params.get("download_status");
		}
		if(params.containsKey("download_speed")){
			downloadSpeed = (Long)params.get("download_speed");
		}
		if(params.containsKey("download_timestamp")){
			downloadCompleteTime = (Long)params.get("download_timestamp");
		}
		
		clientIp=getHostIp();
		Report report = buildReport();
		
		return report;
	}
	
	private long ipToLong(String ipstr){
		// Parse IP parts into an int array
		int[] ip = new int[4];
		String[] parts = ipstr.split("\\.");
		if (parts.length < 4){
			//传入的ip字符串不合格
			return 0;
		}

		try {
			for (int i = 0; i < 4; i++) {
			    ip[i] = Integer.parseInt(parts[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		
		// Add the above IP parts into an int number representing your IP 
		// in a 32-bit binary form
		long ipNumbers = 0;
		for (int i = 0; i < 4; i++) {
		    ipNumbers += ip[i] << (24 - (8 * i));
		}
		
		return ipNumbers;
	}
	
	private Report buildReport() {
		Report reportBuilder = new Report();
		reportBuilder.setUintTimestamp(System.currentTimeMillis()/ 1000);
		reportBuilder.setUintType(logType);
		
		if(clientIp!=null&&!clientIp.equals("null"))
			{
			reportBuilder.setUintClientip(ipToLong(clientIp));
			}
		else{
			reportBuilder.setUintClientip(ipToLong("127.0.0.1"));;
		}
		
		reportBuilder.setUintAssistantid(Long.parseLong(assistantId));
		reportBuilder.setStrSdkversion(ByteStringMicro.copyFromUtf8(ADVersion));
		reportBuilder.setStrSessionid(ByteStringMicro.copyFromUtf8(sessionId));
		reportBuilder.setUintScheduleid(schedualId);
		reportBuilder.setUintPaytype(payType);
		reportBuilder.setUintShowtype(showType);
		reportBuilder.setUintSpaceid(spaceId);
		reportBuilder.setUintContentid(contentId);
		reportBuilder.setStrPicurl(ByteStringMicro.copyFromUtf8(picUrl));
		reportBuilder.setUintTimeinterval(adTime);
		reportBuilder.setStrDsturl(ByteStringMicro.copyFromUtf8(jumpUrl));
		reportBuilder.setUintPkgsize(apkSize);
		reportBuilder.setUintSpeed(downloadSpeed);
		reportBuilder.setUintDresult(is_downloadSuccess);
		reportBuilder.setUintDcomplete(downloadCompleteTime);
		reportBuilder.setUintInstall(is_apkInstall);
		reportBuilder.setUintOstype(mOs);
		reportBuilder.setStrOsversion(ByteStringMicro.copyFromUtf8(mOsVersion));
		reportBuilder.setStrTrademark(ByteStringMicro.copyFromUtf8(mTradeMark));
		if (mMatId == null){
			reportBuilder.setStrPhoneid(ByteStringMicro.copyFromUtf8("0"));
		}else{
			reportBuilder.setStrPhoneid(ByteStringMicro.copyFromUtf8(mMatId));
		}
		reportBuilder.setStrResolution(ByteStringMicro.copyFromUtf8(resolution));
		reportBuilder.setUintNettype(netStat);
		reportBuilder.setUintDpi(ScreenDpi);
		reportBuilder.setUintProviderid(iProviderId);
		reportBuilder.setUintCustomerid(iCustomerId);
		reportBuilder.setUintFrame(frame);

		String keySig = appKey + reportBuilder.getUintTimestamp()
				+ reportBuilder.getUintClientip()
				+ reportBuilder.getUintAssistantid()
				+ reportBuilder.getUintScheduleid()
				+ reportBuilder.getUintSpaceid() + "QAD_REPORT";
		reportBuilder
				.setStrKeysig(ByteStringMicro.copyFromUtf8(MD5Util.MD5(keySig)));

		String totalSig = "" + reportBuilder.getUintTimestamp()
				+ reportBuilder.getUintType() 
				+ reportBuilder.getUintClientip()
				+ reportBuilder.getUintAssistantid()
				+ reportBuilder.getStrSdkversion().toStringUtf8()
				+ reportBuilder.getStrSessionid().toStringUtf8()
				+ reportBuilder.getStrKeysig().toStringUtf8()
				+ reportBuilder.getUintScheduleid()
				+ reportBuilder.getUintPaytype()
				+ reportBuilder.getUintShowtype()
				+ reportBuilder.getUintSpaceid()
				+ reportBuilder.getUintContentid()
				+ reportBuilder.getStrPicurl().toStringUtf8()
				+ reportBuilder.getUintTimeinterval()
				+ reportBuilder.getStrDsturl().toStringUtf8()
				+ reportBuilder.getUintPkgsize()
				+ reportBuilder.getUintSpeed() 
				+ reportBuilder.getUintDresult()
				+ reportBuilder.getUintDcomplete()
				+ reportBuilder.getUintInstall()
				+ reportBuilder.getUintOstype()
				+ reportBuilder.getStrOsversion().toStringUtf8()
				+ reportBuilder.getStrTrademark().toStringUtf8()
				+ reportBuilder.getStrPhoneid().toStringUtf8()
				+ reportBuilder.getStrResolution().toStringUtf8()
				+ reportBuilder.getUintNettype() 
				+ reportBuilder.getUintDpi()
				+ reportBuilder.getUintProviderid()
				+ reportBuilder.getUintCustomerid()
				+ reportBuilder.getUintFrame()
				+ appKey;
		reportBuilder.setStrTotalsig(ByteStringMicro.copyFromUtf8(MD5Util
				.MD5(totalSig)));

		return reportBuilder;
	}
	
	private String md5_String(String originString){
		 try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		md.update(originString.getBytes()); // MD5加密算法只是对字符数组而不是字符串进行加密计算，得到要加密的对象
		byte[] bs = md.digest(); // 进行加密运算并返回字符数组
		String md5_String = HexUtil.bytes2HexStr(bs).toLowerCase(Locale.CHINA);
		return md5_String;
	}
	
	//加密函数
	public String encode(String str){
		//生成加密因子
		Log.d("cloud", "origin--->" + str);
		String key = "";
		Random random = new Random();
		for (int i = 0; i < 10; i++){
			char keychar = (char) random.nextInt(128);
			key += keychar;
		}
		
		//获取重复次数N
		int length = str.length();
		double klength = 10;
		int n = (int) Math.ceil(length/klength);
		
		//计算最终的加密因子
		String finalkey ="";
		for (int i = 0; i < n; i++){
			finalkey += key;
		}
		
		//进行异或操作
		byte[] strBytes = str.getBytes();
		byte[] keyBytes = finalkey.getBytes();
		int byteLength = strBytes.length;
		byte[] resultBytes = new byte[byteLength];
		for (int i = 0; i < byteLength; i++){
			resultBytes[i] = (byte) (strBytes[i] ^ keyBytes[i]);
		}
		
		//得到加密数据
		String encodeString = new String(resultBytes);
		String resultStr = encodeString + key;
		Log.d("cloud", "encode--->" + resultStr);
		return resultStr;
	}
}