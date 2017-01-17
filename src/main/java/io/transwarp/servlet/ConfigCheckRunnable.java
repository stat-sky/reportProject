package io.transwarp.servlet;

import io.transwarp.bean.ConfigBean;
import io.transwarp.bean.NodeBean;
import io.transwarp.conn.ShellUtil;
import io.transwarp.util.Constant;
import io.transwarp.util.UtilTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.log4j.Logger;
import org.dom4j.Element;

public class ConfigCheckRunnable implements Runnable{

	private static Logger logger = Logger.getLogger(ConfigCheckRunnable.class);

	private Map<String, String> configMap;
	private String nodeUser;
	private String hostname;
	private String ipAddress;
	private String goalPath;
	
	public ConfigCheckRunnable(Map<String, String> configMap, NodeBean node, String nodeUser, String goalPath) {
		this.configMap = configMap;
		this.nodeUser = nodeUser;
		this.hostname = node.getHostName();
		this.ipAddress = node.getIpAddress();
		this.goalPath = goalPath;
	}
	
	@Override
	public void run() {
		logger.info("begin service config check of node : " + hostname);
		Map<String, ConfigBean> serviceConfigs = new HashMap<String, ConfigBean>();
		for(Iterator<String> servicenames = Information.services.keySet().iterator(); servicenames.hasNext(); ) {
			String servicename = servicenames.next();
			String configPath = configMap.get(servicename);
			ConfigBean configBean = serviceConfigs.get(servicename);
			if(configBean == null) {
				configBean = new ConfigBean(servicename);
			}
			/* 构建配置路径 */
			StringBuffer dirPath = new StringBuffer();
			dirPath.append(nodeUser).append("@").append(ipAddress).append(":")
					.append("/etc/").append(configPath).append("/conf/*");
			/* 构建本地存放路径和存放的文件夹 */
			StringBuffer savePath = new StringBuffer(goalPath);
			savePath.append(servicename).append("/").append(hostname).append("/");
			try {
				File saveDir = new File(savePath.toString());
				if(!saveDir.exists()) {
					saveDir.mkdirs();
				}
			}catch(Exception e) {
				logger.error("build folder error, error message is " + e.getMessage());
			}
			/* 将配置拷贝到指定文件夹下 */
			try {
				ShellUtil.scpFile(dirPath.toString(), savePath.toString());
			} catch (Exception e) {
				logger.error("scp config file error, error message is " + e.getMessage());
			}
			/* 分析配置文件 */
			Queue<String> queue = new LinkedList<String>();
			queue.offer(savePath.toString());
			while(!queue.isEmpty()) {
				String path = queue.poll();
				File file = new File(path);
				if(file.isDirectory()) {
					File[] children = file.listFiles();
					for(File child : children) {
						queue.offer(child.getAbsolutePath());
					}
				}else {
					if(path.endsWith(".xml")) {
						Map<String, String> result = analysisXml(path);
						if(result == null || result.size() == 0) continue;
						configBean.addConfigFile(UtilTool.getFileName(path), result);
					}else if(path.endsWith(".sh") || path.endsWith("-env")) {
						Map<String, String> result = analysisSh(path);
						if(result == null || result.size() == 0) continue;
						configBean.addConfigFile(UtilTool.getFileName(path), result);
					}
				}
			}
			serviceConfigs.put(servicename, configBean);
		}
		Information.configs.put(hostname, serviceConfigs);
		Information.successTask.incrementAndGet();
		logger.info("service config check of node : " + hostname + " is completed");
	}
	private Map<String, String> analysisXml(String filePath) {
		/* 存放分析结果 */
		Map<String, String> result = new HashMap<String, String>();
		/* 获取文件名 */
		String fileName = UtilTool.getFileName(filePath);
		/* 获取相关配置 */
		Element config = null;
		try {
			config = Constant.prop_config.getElement("topic", fileName);
		}catch(Exception e) {
			logger.error("get config of read service config error, error message is " + e.getMessage());
		}
		if(config == null) {
			return null;
		}
		/* 获取需要截取的部分 */
		String property = config.elementText("property");
		if(property == null || property.equals("")) return null;
		String[] items = property.split(";");
		/* 加载配置文件 */
		Configuration configuration = HBaseConfiguration.create();
		try {
			configuration.addResource(new FileInputStream(filePath));
		} catch (Exception e) {
			logger.error("load service config file error, error message is " + e.getMessage());
		}
		/* 获取需要的配置项 */
		for(String item : items) {
			String value = configuration.get(item);
			if(value != null) {
				result.put(item, value);
			}
		}
		return result;
	}
	@SuppressWarnings("resource")
	private Map<String, String> analysisSh(String filePath) {
		/* 存放分析结果 */
		Map<String, String> result = new HashMap<String, String>();
		/* 获取文件名 */
		String fileName = UtilTool.getFileName(filePath);
		/* 获取相关配置 */
		Element config = null;
		try {
			config = Constant.prop_config.getElement("topic", fileName);
		}catch(Exception e) {
			logger.error("get config of read service config error, error message is " + e.getMessage());
		}
		if(config == null) {
			return null;
		}
		/* 获取需要截取的参数项 */
		String parameter = config.elementText("property");
		if(parameter == null) return null;
		String[] items = parameter.split(";");
		/* 读取配置文件 */
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
		} catch (Exception e) {
			logger.error("read sh file error, error message is " + e.getMessage());
		}
		String line = null;
		while(true) {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				logger.error("read line value from sh file error, error message is " + e.getMessage());
			}
			if(line == null) break;
			/* 过滤空白行和注释行 */
			line = line.trim();
			if(line.startsWith("#") || line.equals("")) continue;
			/* 确定赋值语句的参数位置 */
			int beginIndex = line.indexOf(" ", 1);
			int endIndex = line.indexOf("=", 1);
			if(endIndex == -1) continue;
			if(beginIndex > endIndex || beginIndex == -1) {
				beginIndex = 0;
			}
			/* 获取文件中的参数值，并存入结果集 */
			String paramName = line.substring(beginIndex, endIndex);
			for(String item : items) {
				if(paramName.indexOf(item) != -1) {
					String value = result.get(item);
					if(value == null) value = "";
					try {
						value += line.substring(endIndex + 1).trim().replaceAll("\"", "");
					}catch(Exception e) {
						logger.error("get valu from file error, error message is " + e.getMessage());
					}
					result.put(item, value);
					break;
				}
			}
		}
		return result;
	}
	
	
}