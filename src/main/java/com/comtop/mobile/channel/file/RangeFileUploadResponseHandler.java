/******************************************************************************
 * Copyright (C) 2014 ShenZhen ComTop Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳康拓普开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、复制、
 * 修改或发布本软件.
 *****************************************************************************/

package com.comtop.mobile.channel.file;

import org.apache.http.Header;

import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;

public abstract class RangeFileUploadResponseHandler extends
AsyncHttpResponseHandler {
	private long start;
	private String fileIdentity;
	private Context context;
	 
	@Override
	public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
		FileUtils
		.deleteTmpFile(context, fileIdentity);
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getStart() {
		return start;
	}

	public void setFileIdentity(String fileIdentity) {
		this.fileIdentity = fileIdentity;
	}

	public void setContext(Context context) {
		this.context = context;
	}
}
