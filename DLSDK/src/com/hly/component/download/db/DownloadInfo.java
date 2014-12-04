package com.hly.component.download.db;

import com.hly.component.download.DownloadConstants;


public class DownloadInfo {
    // 需要一个唯一标识的id    
    public long id = 0;
    // uri的md5值    
    public String md5 = "";
    // 下载标题
    public String title = "";
    // 下载文件的描述
    public String desc = "";
    // 下载文件的uri
    public String uri = "";
    // 重定向的uri,如果没有则为空
    public String redirect_uri = "";
    // 下载文件的类型
    public String media_type = "";
    // 本地保存的uri
    public String local_uri = "";
    // 下载的状态
    public int status = DownloadConstants.STATUS_PENDING;
    // 错误原因
    public int reason = 0;
    // 错误原因详情
    public String errMsg = "";
    // 下载的总大小
    public long total_bytes = 0;
    // 当前下载的大小
    public long current_bytes = 0;
    // last modify time
    public long lastmod_stamp = 0;
    // create download time
    public long create_stamp = 0;
    // 头文件，保存格式为header:value;header:value;
    public String header = "";
    // 删除文件标识(0,1删除)
    public int deleted = 0;
    // 什么网络下下载，为1时什么情况下都行
    public int acceptNet = 1;
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
}
