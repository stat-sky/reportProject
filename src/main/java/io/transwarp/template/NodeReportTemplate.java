package io.transwarp.template;

import java.io.FileWriter;

import io.transwarp.bean.NodeBean;

import org.apache.log4j.Logger;

public abstract class NodeReportTemplate {

	private static Logger logger = Logger.getLogger(NodeReportTemplate.class);
	
	private NodeBean node;
	
	public NodeReportTemplate(NodeBean node) {
		this.node = node;
	}
	
	public void getReport(String dirPath) throws Exception {
		String goalPath = dirPath + node.getHostName() + ".txt";
		/* 建立输出流 */
		FileWriter writer = new FileWriter(goalPath);
		/* 写入节点信息 */
		try {
			String nodeInfo = this.getNodeInfo();
			writer.write(nodeInfo);
		}catch(Exception e) {
			logger.error("write node info error, " + e.getMessage());
		}
		/* 写入时间同步信息 */
		try {
			String ntpInfo = this.getNtpInfo();
			writer.write(ntpInfo);
		}catch(Exception e) {
			logger.error("write ntp info error, " + e.getMessage());
		}
		/* 写入Java路径信息 */
		try {
			String javaPath = this.getJavaPathInfo();
			writer.write(javaPath);
		}catch(Exception e) {
			logger.error("write java path info error, " + e.getMessage());
		}
		/* 写入jdk版本号信息 */
		try {
			String jdkVersion = this.getJdkInfo();
			writer.write(jdkVersion);
		}catch(Exception e) {
			logger.error("write jdk version error, " + e.getMessage());
		}
		/* 写入DNS信息 */
		try {
			String dnsInfo = this.getDnsInfo();
			writer.write(dnsInfo);
		}catch(Exception e) {
			logger.error("write dns info error, " + e.getMessage());
		}
		/* 写入防火墙信息 */
		try {
			String iptable = this.getIptableInfo();
			writer.write(iptable);
		}catch(Exception e) {
			logger.error("write iptable info error, " + e.getMessage());
		}
		try {
			String network = this.getNetworkInfo();
			writer.write(network);
		}catch(Exception e) {
			logger.error("write network info error, " + e.getMessage());
		}
		/* 写入hosts文件信息 */
		try {
			String hostInfo = this.getHostsInfo();
			writer.write(hostInfo);
		}catch(Exception e) {
			logger.error("write hosts info error, " + e.getMessage());
		}
		/* 写入内存信息 */
		try {
			String memory = this.getMemoryInfo();
			writer.write(memory);
		}catch(Exception e) {
			logger.error("write memory info error, " + e.getMessage());
		}
		/* 写入磁盘挂载信息 */
		try {
			String mount = this.getMountInfo();
			writer.write(mount);
		}catch(Exception e) {
			logger.error("write mount info error, " + e.getMessage());
		}
		/* 写入端口检测结果 */
		try {
			String port = this.getPortInfo();
			writer.write(port);
		}catch(Exception e) {
			logger.error("write port info error, " + e.getMessage());
		}
		/* 写入配置信息 */
		try {
			String configInfo = this.getServiceConfigInfo();
			writer.write(configInfo);
		}catch(Exception e) {
			logger.error("write service config error, " + e.getMessage());
		}
		/* 写入负载指标信息 */
		try {
			String metricInfo = this.getMetricInfo();
			writer.write(metricInfo);
		}catch(Exception e) {
			logger.error("write load metric error, " + e.getMessage());
		}
		/* 关闭输出流 */
		writer.flush();
		writer.close();
	}
	
	public abstract String getNodeInfo();
	public abstract String getNtpInfo();
	public abstract String getJavaPathInfo();
	public abstract String getJdkInfo();
	public abstract String getDnsInfo();
	public abstract String getIptableInfo();
	public abstract String getNetworkInfo();
	public abstract String getHostsInfo();
	public abstract String getMemoryInfo();
	public abstract String getMountInfo();
	public abstract String getPortInfo();
	public abstract String getServiceConfigInfo();
	public abstract String getMetricInfo();
}
