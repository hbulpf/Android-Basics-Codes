package cn.itcast.download;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MutilDownLoad {
	// 线程数
	static int threadCount = 3;

	static String path = "http://192.168.1.29:8080/web/download/gui.exe";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// (1). 创建和服务器资源文件一样大小的空文件
		try {
			// 1. 初始化Url
			URL url = new URL(path);
			// 2. 通过Url获取Http连接
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			// 3. 设置请求参数和请求方式
			conn.setRequestMethod("GET");
			// 4. 获取返回码 200:成功 3xxx缓存 4xxx客户端错误 500服务器错误
			int code = conn.getResponseCode();
			// 5. 拿到从服务器端返回的资源文件的大小
			int fileLength = conn.getContentLength();
			if (code == 200) {
//				System.out.println("服务器资源文件的大小：" + fileLength);
//				RandomAccessFile raf = new RandomAccessFile(getFileName(), "rw");
//				// 重要设置文件的大小
//				raf.setLength(fileLength);
//				raf.close();
			}

			// (2).开启多个线程下载
			// 每个区块的大小
			int blockSize = fileLength / threadCount;

			for (int threadId = 0; threadId < threadCount; threadId++) {
				int startIndex = threadId * blockSize;
				int endIndex = (threadId + 1) * blockSize-1;
				// 最后一个线程
				if (threadId == threadCount - 1) {
					// 修正文件结束的位置
					endIndex = fileLength - 1;
				}
				// 开始线程
				new DownLoadThread(startIndex, endIndex, threadId).start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class DownLoadThread extends Thread {
		// 开始位置
		int startIndex;
		// 结束位置
		int endIndex;
		// 线程Id
		int threadId;

		public DownLoadThread(int startIndex, int endIndex, int threadId) {
			super();
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.threadId = threadId;
		}

		@Override
		public void run() {
			super.run();
			System.out.println("线程" + threadId + ": " + startIndex + " ~ "
					+ endIndex);
			 System.out.println("线程" + threadId + "工作量: "
			 +(endIndex-startIndex));

			try {
				// 1. 初始化Url
				URL url = new URL(path);
				// 2. 通过Url获取Http连接
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				// 3. 设置请求参数和请求方式
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(3000);
				//重要，设置每个线程请求服务器资源的大小
				conn.setRequestProperty("Range", "bytes="+startIndex + "-" + endIndex);
				// 4. 获取返回码 200:成功 3xxx缓存 4xxx客户端错误 500服务器错误
				// 206:表示部分请求成功
				int code = conn.getResponseCode();
				System.out.println("部分请求成功："+code);
				if (code == 206) {
					// 5. 拿到从服务器端返回的输入流
					InputStream is = conn.getInputStream();
					RandomAccessFile rf = new RandomAccessFile(getFileName(),
							"rw");
					// 重要，每个线程从他的开始位置写文件
					rf.seek(startIndex);
					byte[] buffer = new byte[1024 * 8];
					int len = -1;
					while ((len = is.read(buffer)) != -1) {
						rf.write(buffer, 0, len);
					}
					rf.close();
				}
				
				System.out.println("线程" + threadId + "干完活了！");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static String getFileName() {
		int index = path.lastIndexOf("/") + 1;
		return path.substring(index);
	}

}
