package com.comtop;

import java.io.File;

import org.apache.http.Header;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.comtop.channel.test.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RangeFileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

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
		switch (view.getId()) {
			case R.id.download_click:

		}
		System.out.println(asyncHttpClient);
		File file = new File(getCacheDir().getAbsolutePath() + File.separator
				+ "temp1");
		Toast.makeText(FileDownLoadActivity.this,
				file.getAbsolutePath() + "  " + file.length(),
				Toast.LENGTH_LONG).show();
		// Header headers = new Header("Range", "bytes=" + file.length() + "-");
		RequestHandle requestHandle = asyncHttpClient.get(
				FileDownLoadActivity.this,
				"http://10.10.50.156:8080/fileUpload/FileDownload",
				new RangeFileAsyncHttpResponseHandler(file) {

					@Override
					public void onFailure(int arg0, Header[] arg1,
							Throwable arg2, File arg3) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onSuccess(int arg0, Header[] arg1, File arg2) {
						Toast.makeText(FileDownLoadActivity.this, arg2.getAbsolutePath(),
								Toast.LENGTH_LONG).show();

					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						System.out.println("onProgress   "
								+ " bytesWritten:"
								+ bytesWritten
								+ "  totalSize:"
								+ totalSize
								+ "  完成了："
								+ (int) ((double) bytesWritten
										/ (double) totalSize * 100));
						ProgressBar s = (ProgressBar) FileDownLoadActivity.this
								.findViewById(R.id.progressBar1);
						s.setProgress((int) ((double) bytesWritten
								/ (double) totalSize * 100));
					}

				});
	}

	public void pauseClick(View view) {
		asyncHttpClient.cancelRequests(FileDownLoadActivity.this, true);
	}
}
