package io.transwarp.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ConfigBean {

	private String serviceName;
	private Vector<String> configFiles;
	private Map<String, Map<String, String>> configValues;
	
	public ConfigBean(String serviceName) {
		this.serviceName = serviceName;
		/* 存放配置文件名称信息，存入字符串为所在节点的hostname + ':' + 文件名 */
		this.configFiles = new Vector<String>();
		/* 存放配置文件内容信息，key为前一个变量存放的表示文件名称信息的字符串，value为文件内容 */
		this.configValues = new HashMap<String, Map<String, String>>();
	}
	
	public String getServiceName() {
		return this.serviceName;
	}
	public Vector<String> getConfigFiles() {
		return this.configFiles;
	}
	public Map<String, Map<String, String>> getConfigValues() {
		return this.configValues;
	}
	
	public void addConfigFile(String filename, Map<String, String> configValue) {
		this.configFiles.add(filename);
		this.configValues.put(filename, configValue);
	}
}
