package io.transwarp.servlet;


import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.Queue;

import io.transwarp.bean.TableBean;
import io.transwarp.util.Constant;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.log4j.Logger;

public class TableCheckRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(TableCheckRunnable.class);
	
	private TableBean table;
	private String security;
	private String hdfsConfPath;
	private String namenodeIP;
	
	public TableCheckRunnable(TableBean table, String security, String hdfsConfPath, String namenodeIP) {
		this.table = table;
		this.security = security;
		this.hdfsConfPath = hdfsConfPath;
		this.namenodeIP = namenodeIP;
	}
	
	@Override
	public void run() {
		/* 加载配置 */
		Configuration config = new Configuration();
		try {
			config.addResource(new FileInputStream(new File(hdfsConfPath + "hdfs-site.xml")));
			logger.debug("load config is " + hdfsConfPath + "hdfs-site.xml");
			config.addResource(new FileInputStream(new File(hdfsConfPath + "core-site.xml")));
			logger.debug("load config is " + hdfsConfPath + "core-site.xml");
		}catch(Exception e) {
			logger.error("load hdfs config error, error message is " + e.getMessage());
		}
		/* 若安全为kerberos或all，则进行安全认证 */
		if(security.equals("kerberos") || security.equals("all")) {
			try {
				UserGroupInformation.setConfiguration(config);
				UserGroupInformation.loginUserFromKeytab("hdfs@TDH", Constant.hdfsKey);
			}catch(Exception e) {
				logger.error("security certificate error, error message is " + e.getMessage());
			}
		}
		
		/* 打开hdfs文件系统 */
		FileSystem fs = null;
		try {
			if(security.equals("kerberos") || security.equals("all")) {
				fs = FileSystem.get(config);
			}else {
				fs = FileSystem.get(URI.create("hdfs://" + this.namenodeIP + ":8020"), config, "hdfs");
			}
		}catch(Exception e) {
			logger.error("open file system error, error message is " + e.getMessage());
			return;
		}
		
		/* 循环遍历表空间 */
		Queue<String> queue = new LinkedList<String>();
		String dataPath = table.getTable_location();
		queue.offer(dataPath);
		while(!queue.isEmpty()) {
			String path = queue.poll();
			if(path == null) continue;
			Path hdfsPath = null;
			try {
				hdfsPath = new Path(path);
			}catch(Exception e) {
				logger.error("this path : " + path + " is not exists");
				continue;
			}
			long sizeDir = 0;
			try {
				FileStatus[] files = fs.listStatus(hdfsPath);
				for(FileStatus file : files) {
					if(file.isDirectory()) {
						queue.offer(file.getPath().toString());
					}else {
						long sizeFile = file.getLen();
						sizeDir += sizeFile;
						table.addFile(sizeFile);
					}
				}
			}catch(Exception e) {
				logger.error("check path " + path + " is error, error message is " + e.getMessage());
			}
			if(sizeDir != 0 && !path.equals(dataPath)) {
				table.addDir(sizeDir);
			}
		}
		/* 检测完成，计数器加1 */
		Information.successTask.incrementAndGet();
	}
}
