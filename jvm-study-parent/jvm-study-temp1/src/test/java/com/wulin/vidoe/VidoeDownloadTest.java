package com.wulin.vidoe;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

public class VidoeDownloadTest {
	public static boolean httpDownload(String httpUrl, String saveFile) {
	        // 1.下载网络文件
	        int byteRead;
	        URL url;
	        try {
	            url = new URL(httpUrl);
	        } catch (MalformedURLException e1) {
	            e1.printStackTrace();
	            return false;
	        }
	
	        try {
	            //2.获取链接
	            URLConnection conn = url.openConnection();
	            conn.setRequestProperty("type", "application/x-mpegURL");
	            //3.输入流
	            InputStream inStream = conn.getInputStream();
	            //3.写入文件
	            FileOutputStream fs = new FileOutputStream(saveFile);
	
	            byte[] buffer = new byte[1024];
	            while ((byteRead = inStream.read(buffer)) != -1) {
	                fs.write(buffer, 0, byteRead);
	            }
	            inStream.close();
	            fs.close();
	            return true;
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	            return false;
	        } catch (IOException e) {
	            e.printStackTrace();
	            return false;
	        }
	    }
	
	
	    @Test
	    public void httpDownload() {
//	        httpDownload("http://video.zhihuishu.com/zhs/ablecommons/demo/201806/dddee1c446314b84a26c74a8def3c3c7.mp4","F:\\resources\\temp\\temp4\\vidoe\\22.mp4");
	        httpDownload("https://video2.caomin5168.com/20190813/pllEPIre/index.m3u8","F:\\resources\\temp\\temp4\\vidoe\\33.mp4");
	    }
}




















