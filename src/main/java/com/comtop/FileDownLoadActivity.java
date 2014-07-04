package com.comtop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.comtop.channel.test.R;
import com.comtop.mobile.channel.ChannelAsynHttpClient;
import com.comtop.mobile.channel.ChannelClientFactory;
import com.comtop.mobile.channel.RangeFileUploadResponseHandler;
import com.comtop.mobile.channel.file.FileUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RangeFileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

public class FileDownLoadActivity extends Activity {
	private EditText filePathText;
	/**
	 * asyncHttpClient客户端
	 */
	private AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	
	private ChannelAsynHttpClient channelAsynHttpClient = ChannelClientFactory.getInstance().initDefaultChannelClient("http://10.10.50.156:8080/channelServer", "*.mobileCall");
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_download);
		filePathText = (EditText) findViewById(R.id.uploadFilePath);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(com.comtop.channel.test.R.menu.main, menu);
		return true;
	}

	public void uploadRangClick(View view) {
//		this.uploadFile(filePathText.getText().toString().trim());
		channelAsynHttpClient.uploadRangeFile(FileDownLoadActivity.this, "demoRangeFileUpload.upload", filePathText.getText().toString().trim(),"name=xxx", new RangeFileUploadResponseHandler(){

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				 Toast.makeText(FileDownLoadActivity.this, "上传失败", Toast.LENGTH_LONG).show();
			}
			
			
			
			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				 super.onSuccess(arg0, arg1, arg2);
				 Toast.makeText(FileDownLoadActivity.this, "上传成功", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				ProgressBar s = (ProgressBar) FileDownLoadActivity.this
						.findViewById(R.id.progressBar_upload);
				s.setProgress((int) ((double) (bytesWritten + this.getStart())
						/ (double) (totalSize + this.getStart()) * 100));
			}
			
			
		});
	}

	public void uploadFile(final String filePath) {
		final File file = new File(filePath);
		if (!file.exists() || file.length() == 0) {
			Toast.makeText(FileDownLoadActivity.this, "文件不存在",
					Toast.LENGTH_LONG).show();
		} else {
			final RequestParams params = new RequestParams();
			params.put("fileIdentify",
					FileUtils.getFileIdentity(FileDownLoadActivity.this, file));
			asyncHttpClient
					.post(FileDownLoadActivity.this,
							"http://10.10.50.156:8080/fileUpload/RangFileUploadServlet",
							params, new TextHttpResponseHandler() {
								@Override
								public void onFailure(int statusCode,
										Header[] headers,
										String responseString,
										Throwable throwable) {
									Toast.makeText(FileDownLoadActivity.this,
											"获取上传文件信息失败", Toast.LENGTH_LONG)
											.show();
								}

								@Override
								public void onSuccess(int statusCode,
										Header[] headers, String responseString) {
									long start = Long.valueOf(responseString);
									try {
										FileDownLoadActivity.this
												.startUploadFile(file, start);
									} catch (IOException e) {
										Toast.makeText(
												FileDownLoadActivity.this,
												"文件上传异常", Toast.LENGTH_LONG)
												.show();
									}
								}
							});
		}

	}

	/**
	 * 获取已经上传文件的大小
	 * 
	 * @param file
	 *            需要上传的文件
	 * @return
	 * @throws IOException
	 */
	private void startUploadFile(final File file, final long start)
			throws IOException {
		final String fileIdentity = FileUtils.getFileIdentity(this, file);
		AsyncHttpResponseHandler progressHandler = new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseBody) {
				FileUtils
						.deleteTmpFile(FileDownLoadActivity.this, fileIdentity);
				Toast.makeText(FileDownLoadActivity.this, "上传成功啦",
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				error.printStackTrace();
				Toast.makeText(FileDownLoadActivity.this, "上传失败",
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				ProgressBar s = (ProgressBar) FileDownLoadActivity.this
						.findViewById(R.id.progressBar_upload);
				s.setProgress((int) ((double) (bytesWritten + start)
						/ (double) (totalSize + start) * 100));
			}
		};
		RequestParams params = new RequestParams();
		params.put("fileIdentify", fileIdentity);
		params.put("file", FileUtils.getRangFile(this, file, start));
		HttpEntity entity = params.getEntity(progressHandler);
		long lFileSize = entity.getContentLength();
		if (start == 0) {
			FileUtils.setFileSize(this, fileIdentity, lFileSize);
		} else {
			lFileSize = FileUtils.getFileSize(this, file);
		}
		asyncHttpClient.cancelRequests(FileDownLoadActivity.this, true);
		asyncHttpClient.post(
				FileDownLoadActivity.this,
				"http://10.10.50.156:8080/fileUpload/RangFileUploadServlet?fileIdentify="
						+ fileIdentity + "&&fileSize=" + lFileSize, entity,
				null, progressHandler);
	}
	
	public void downloadClick(View view) {
		File file = new File(getCacheDir().getAbsolutePath() + File.separator
				+ "client_world.exe");
		RequestParams params = new RequestParams();
		channelAsynHttpClient.downloadFile(this, "fileControl.download", params, new RangeFileAsyncHttpResponseHandler(file) {

			@Override
			public void onFailure(int arg0, Header[] arg1,
					Throwable arg2, File arg3) {
				Log.e("shi", "失败了");
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, File arg2) {
				Toast.makeText(FileDownLoadActivity.this,
						arg2.getAbsolutePath(), Toast.LENGTH_LONG)
						.show();
				filePathText.setText(arg2.getAbsolutePath());
				System.out.println(arg2.length());
			}

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				ProgressBar s = (ProgressBar) FileDownLoadActivity.this
						.findViewById(R.id.progressBar1);
				s.setProgress((int) ((double) bytesWritten
 						/ (double) totalSize * 100));
			}

		});
	}
	public void downloadClick1(View view) {
		File file = new File(getCacheDir().getAbsolutePath() + File.separator
				+ "client_world.exe");
		asyncHttpClient.get(
				FileDownLoadActivity.this,
				"http://10.10.50.156:8080/fileUpload/FileDownload",
				new RangeFileAsyncHttpResponseHandler(file) {

					@Override
					public void onFailure(int arg0, Header[] arg1,
							Throwable arg2, File arg3) {

					}

					@Override
					public void onSuccess(int arg0, Header[] arg1, File arg2) {
						Toast.makeText(FileDownLoadActivity.this,
								arg2.getAbsolutePath(), Toast.LENGTH_LONG)
								.show();
						filePathText.setText(arg2.getAbsolutePath());
					}

					@Override
					public void onProgress(int bytesWritten, int totalSize) {
						ProgressBar s = (ProgressBar) FileDownLoadActivity.this
								.findViewById(R.id.progressBar1);
//						s.setProgress((int) ((double) bytesWritten
//								/ (double) totalSize * 100));
					}

				});
	}

	public void uploadClick(View view) {
		RequestParams params = new RequestParams();
		File file = new File(filePathText.getText().toString().trim());
		if (file.exists()) {
			try {
				params.put("tttt", "ttttxxx");
				params.put("file", file);
				System.out.println("ttttt");

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			asyncHttpClient.post(FileDownLoadActivity.this,
					"http://10.10.50.156:8080/fileUpload/FileServlet?xxx=eee",
					params, new AsyncHttpResponseHandler() {

						@Override
						public void onSuccess(int statusCode, Header[] headers,
								byte[] responseBody) {
							Toast.makeText(FileDownLoadActivity.this, "上传成功",
									Toast.LENGTH_LONG).show();

						}

						@Override
						public void onFailure(int statusCode, Header[] headers,
								byte[] responseBody, Throwable error) {
							error.printStackTrace();
							Toast.makeText(FileDownLoadActivity.this, "上传失败",
									Toast.LENGTH_LONG).show();
						}

						@Override
						public void onProgress(int bytesWritten, int totalSize) {
							ProgressBar s = (ProgressBar) FileDownLoadActivity.this
									.findViewById(R.id.progressBar_upload);
							s.setProgress((int) ((double) bytesWritten
									/ (double) totalSize * 100));
						}

					});
		} else {
			Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG).show();
		}

	}

	public void uploadPauseClick(View view) {

	}

	public void pauseClick(View view) {
		channelAsynHttpClient.getAsyncHttpClient().cancelRequests(FileDownLoadActivity.this, true);
	}
}
