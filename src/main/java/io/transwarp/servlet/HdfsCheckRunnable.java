package io.transwarp.servlet;

import java.util.List;

import io.transwarp.conn.ShellUtil;
import io.transwarp.util.Constant;
import io.transwarp.util.UtilTool;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class HdfsCheckRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(HdfsCheckRunnable.class);
	
	private String security;
	private String ipAddress;
	private String nodeUser;
	
	public HdfsCheckRunnable(String security, String ipAddress, String nodeUser) {
		this.security = security;
		this.ipAddress = ipAddress;
		this.nodeUser = nodeUser;
	}
	
	@Override
	public void run() {
		logger.info("begin hdfs check");
		/* 获取配置 */
		List<Element> configs = null;
		try {
			configs = Constant.prop_cluster.getAll();
		}catch(Exception e) {
			logger.error("get config of hdfs check error, error message is " + e.getMessage());
		}
		for(Element config : configs) {
			String itemName = config.elementText("name");
			String command = config.elementText("command");
			/* 根据安全类型来修改命令 */
			command = UtilTool.getCmdOfSecurity(command, security);
			/* 执行命令获取结果 */
			String result = null;
			try {
				result = ShellUtil.executeDist(command, nodeUser, ipAddress);
			}catch(Exception e) {
				logger.error("execute shell of hdfs check error, error cmd is " + command + ", error message is " + e.getMessage());
			}
			if(result == null) return;
			/* 将结果存入Information */
			Information.hdfsChecks.put(itemName, result);
		}
		/* 检测完成，计数器加1 */
		logger.info("check of hdfs is completed");
		Information.successTask.incrementAndGet();
	}
}
