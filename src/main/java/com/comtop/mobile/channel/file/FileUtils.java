/******************************************************************************
 * Copyright (C) 2014 ShenZhen ComTop Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳康拓普开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、复制、
 * 修改或发布本软件.
 *****************************************************************************/

package com.comtop.mobile.channel.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;

import org.apache.http.HttpEntity;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.loopj.android.http.RequestParams;

/**
 * 文件公用类
 * 
 * @author zhaoqunqi
 *
 */
public class FileUtils {

	/**
	 * 保存文件大小的临时文件
	 */
	private static final String FIZE_SIZE_NAME = "file.size";
	
	/**
	 * 字节流大小
	 */
	public static final int BUFFER_SIZE = 4096;

	/**
	 * 获取文件剩余部分
	 * 
	 * @param context
	 *            上下文
	 * @param file
	 *            文件
	 * @param start
	 *            开始位置
	 * @return file从start位置后的文件流
	 * @throws IOException 
	 */
	public static File getRangFile(Context context, File file, long start) throws IOException {
		if(start ==0){
			return file;
		}
		String fileIdentity = getFileIdentity(context, file);
		StringBuffer filePath = getFileTmpDir(context, fileIdentity);
		filePath.append(new Date().getTime());
		File tmpFile = new File(filePath.toString());
		InputStream in = null;
		OutputStream buffer = null;
		byte[] tmp = new byte[BUFFER_SIZE];
		int l = 0;
		try {
			in = new FileInputStream(file);
			in.skip(start);
			buffer = new FileOutputStream(tmpFile);
			while ((l = in.read(tmp)) != -1) {
				buffer.write(tmp, 0, l);
			}
			buffer.flush();
		} finally {
			closeStream(in, buffer);
		}
		return tmpFile;
	}

	
	
	/**
	 * 获取文件整体流的长度
	 * 
	 * @param context
	 *            上下文
	 * @param fileIdentity
	 *            文件标示
	 * @return 文件长度
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static long getFileSize(Context context, File file) throws IOException {
		String fileIdentity = getFileIdentity(context, file);
		StringBuffer filePath = getFileTmpDir(context, fileIdentity);
		filePath.append(FIZE_SIZE_NAME);
		File fileSizeFile = new File(filePath.toString());
		if (fileSizeFile.exists()) {
			return Long.valueOf(getFileContent(fileSizeFile).trim());
		}else{
			RequestParams params = new RequestParams();
			params.put("fileIdentify", fileIdentity);
			params.put("file", file);
			HttpEntity entity = params.getEntity(null);
			setFileSize(context, fileIdentity, entity.getContentLength());
			return entity.getContentLength();
		}
	}
	
	/**
	 * 删除临时文件
	 * 
	 * @param context 上下文
	 * @param fileIdentity 文件标示
	 */
	public static void deleteTmpFile(Context context, String fileIdentity){
		StringBuffer sb  = getFileTmpDir(context, fileIdentity);
		File dirFile = new File(sb.toString());
		if(dirFile.exists()){
			if(dirFile.isDirectory()){
				 File[] files = dirFile.listFiles(); 
				 for (File file : files) {
					 file.delete();
				 }
			}
			 dirFile.delete();
		}
	}
	/**
	 * 获取文件的内容信息
	 * 
	 * @param file 文件
	 * @return 文件内容字串
	 * @throws IOException
	 */
	public static String getFileContent(File file) throws IOException {
		StringBuffer sb = new StringBuffer(64);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				sb.append(tempString);
			}
		} finally {
			closeStream(reader,null);
		}
		return sb.toString();
	}
	
	/**
	 * 设置文件的大小，将文件大小写入临时文件中
	 * 
	 * @param context 上下文
	 * @param fileIdentity 文件标示
	 * @param fileSize 文件大小
	 * @throws IOException
	 */
	public static void setFileSize(Context context, String fileIdentity,
			long fileSize) throws IOException {
		StringBuffer filePath = getFileTmpDir(context, fileIdentity);
		filePath.append(FIZE_SIZE_NAME);
		File fileSizeFile = new File(filePath.toString());
		if (fileSizeFile.exists()) {
			fileSizeFile.delete();
		}
		fileSizeFile.createNewFile();
		FileWriter writer = null;
		try {
		    writer = new FileWriter(fileSizeFile);
	        writer.write(String.valueOf(fileSize));
	        writer.flush();
		}finally{
			 closeStream(null, writer);
		}
	}
	
	/**
	 * 获取临时文件目录
	 * 
	 * @param context 上下问
	 * @param fileIdentity 文件标示
	 * @return context.getCacheDir()+fileIdentity目录
	 */
	private static StringBuffer getFileTmpDir(Context context,
			String fileIdentity) {
		StringBuffer fileDir = new StringBuffer(32);
		fileDir.append(context.getCacheDir().getAbsolutePath());
		fileDir.append(File.separator);
		fileDir.append(fileIdentity);
		File file = new File(fileDir.toString());
		if(!file.exists()){
			file.mkdirs();
		}
		fileDir.append(File.separator);
		return fileDir;
	}

	/**
	 * 获取文件的唯一标示
	 * 
	 * @param context
	 *            上下文，需要读取手机状态权限
	 * @param file
	 *            文件
	 * @return 文件唯一标示：设备Id+文件的hashcode+文件大小
	 */
	public static String getFileIdentity(Context context, File file) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		StringBuilder sb = new StringBuilder(120);
		sb.append(tm.getDeviceId()); // 设备Id
		sb.append(file.hashCode()); // 文件的hashcode
		sb.append(file.length()); // 文件大小
		return sb.toString();
	}
	
	/**
	 * 关闭资源
	 * 
	 * @param in
	 *            输入流
	 * @param out
	 *            输出流
	 */
	public static void closeStream(InputStream in, OutputStream out) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				in = null;
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				out = null;
			}
		}
	}

	/**
	 * 关闭资源
	 * 
	 * @param in
	 *            输入流
	 * @param out
	 *            输出流
	 */
	public static void closeStream(Reader in, Writer out) {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				in = null;
			}
		}
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				out = null;
			}
		}
	}
}
