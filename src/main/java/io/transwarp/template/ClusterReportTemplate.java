package io.transwarp.template;

import io.transwarp.bean.NodeBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.report.ServiceRoleMapReport;

import java.io.FileWriter;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class ClusterReportTemplate {
	
	private static Logger logger = Logger.getLogger(ClusterReportTemplate.class);
	
	private String tdh_version;
	private Map<String, NodeBean> nodes;
	private Map<String, ServiceBean> services;
	
	public ClusterReportTemplate(String tdh_version, Map<String, NodeBean> nodes, Map<String, ServiceBean> services) {
		this.tdh_version = tdh_version;
		this.nodes = nodes;
		this.services = services;
	}
	
	public void getReport(String path) throws Exception {

		/* 对获取的结果进行解析，格式化输出 */
		FileWriter writer = new FileWriter(path);
		/* 写入集群版本号 */
		try {
			writer.write("集群版本号为：" + tdh_version);
			writer.write("\n\n");			
		}catch(Exception e) {
			logger.error("write tdh version error");
		}

		/* 写入服务角色分布图 */
		ServiceRoleMapReport roleMap = new ServiceRoleMapReport(nodes, services);
		try {
			writer.write(roleMap.getRoleMap());
			writer.write("\n\n");			
		}catch(Exception e) {
			logger.error("write role map error");
		}

		/* 写入hdfs检测结果 */
		try {
			String hdfsInfo = getHDFSInfo();
			writer.write(hdfsInfo);
		}catch(Exception e) {
			logger.error("write hdfs info error, error message is " + e.getMessage());
		}
		/* 写入进程检测结果 */
		try {
			String processInfo = getProcessInfo();
			writer.write(processInfo);
		}catch(Exception e) {
			logger.error("write process info error, error message is " + e.getMessage());
		}
		/* 写入数据表检测结果 */
		try {
			String tableInfo = getTableInfo();
			writer.write(tableInfo);
		}catch(Exception e) {
			logger.error("write table info error, error message is " + e.getMessage());
		}

		/* 关闭输出流 */
		writer.flush();
		writer.close();	

	}
	
	public abstract String getHDFSInfo();
	public abstract String getProcessInfo();
	public abstract String getTableInfo();
}
