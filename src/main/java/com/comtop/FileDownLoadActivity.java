package com.comtop;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity.Header;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.comtop.channel.test.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

public class FileDownLoadActivity extends Activity {

	/**
	 * asyncHttpClient客户端
	 */
	private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_download);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(com.comtop.channel.test.R.menu.main, menu);
		return true;
	}

	public void downloadClick(View view) {
		asyncHttpClient.get(FileDownLoadActivity.this, "http://10.10.50.10:18080/hudson/job/casClient/ws/target/csso-client-core-1.0.0.jar", new FileAsyncHttpResponseHandler(FileDownLoadActivity.this){
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				System.out.println("onProgress");
				ProgressBar s = (ProgressBar)FileDownLoadActivity.this.findViewById(R.id.progressBar1);
				s.setProgress( (int)((double)bytesWritten/(double)totalSize*100));
			}
		});
	}

	public void pauseClick(View view) {
		System.out.println("pauseClick");
	}
}
