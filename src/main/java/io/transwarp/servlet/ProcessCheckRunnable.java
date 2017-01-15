package io.transwarp.servlet;

import io.transwarp.conn.ShellUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class ProcessCheckRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(ProcessCheckRunnable.class);
	
	private String ipAddress;
	private String nodeUser;
	private Element config;
	private String topic;
	
	public ProcessCheckRunnable(String ipAddress, String nodeUser, Element config, String topic) {
		this.ipAddress = ipAddress;
		this.nodeUser = nodeUser;
		this.config = config;
		this.topic = topic;
	}
	
	@Override
	public void run() {
		/* 存放检测结果，参数依次为 检测项，检测结果 */
		Map<String, String> answer = new HashMap<String, String>();
		/* 获取检测项 */
		@SuppressWarnings("unchecked")
		List<Element> properties = config.elements();
		for(Element property : properties) {
			/* 获取执行命令 */
			String command = property.elementText("command");
			/* 执行命令获取结果 */
			String result = null;
			try {
				result = ShellUtil.executeDist(command, nodeUser, ipAddress, 2000);
			}catch(Exception e) {
				logger.error("process check error, error message is " + e.getMessage());
			}
			if(result == null) continue;
			String itemName = property.elementText("name");
			answer.put(itemName, result.trim());
		}
		Information.processChecks.put(topic, answer);
		
		/* 检测完成，计数器加1 */
		logger.info("process of " + topic + " is completed");
		Information.successTask.incrementAndGet();
	}
}
