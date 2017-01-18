package io.transwarp.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import io.transwarp.bean.ConfigBean;
import io.transwarp.bean.MetricBean;
import io.transwarp.bean.NodeBean;
import io.transwarp.template.NodeReportTemplate;
import io.transwarp.util.Constant;
import io.transwarp.util.PrintToTableUtil;
import io.transwarp.util.UtilTool;

public class NodeReportOfCentOS extends NodeReportTemplate{
	
	private static Logger logger = Logger.getLogger(NodeReportOfCentOS.class);
	
	private NodeBean node;
	private Map<String, String> nodeCheck;
	private Map<String, ConfigBean> configBeans;
	private Map<String, String> portChecks;
	private Map<String, MetricBean> metrics;
	
	public NodeReportOfCentOS(NodeBean node, Map<String, String> nodeCheck, Map<String, ConfigBean> configBeans, Map<String, String> portChecks, Map<String, MetricBean> metrics) {
		super(node);
		this.node = node;
		this.nodeCheck = nodeCheck;
		this.configBeans = configBeans;
		this.portChecks = portChecks;
		this.metrics = metrics;
	}

	@Override
	public String getNodeInfo() {
		/* 用于生成表格的缓存 */
		List<String[]> maps = new ArrayList<String[]>();
		maps.add(new String[]{"检测项", "值"});
		/* 添加rest api获取的节点信息 */
		maps.add(new String[]{"isManager", node.getIsManaged()});
		maps.add(new String[]{"hostname", node.getHostName()});
		maps.add(new String[]{"ipAddress", node.getIpAddress()});
		maps.add(new String[]{"clusterName", node.getClusterName()});
		maps.add(new String[]{"rackName", node.getRackName()});
		maps.add(new String[]{"status", node.getStatus()});
		maps.add(new String[]{"numCores", node.getNumCores()});
		maps.add(new String[]{"totalPhysMemBytes", node.getTotalPhysMemBytes()});
		maps.add(new String[]{"cpu", node.getCpu()});
		maps.add(new String[]{"osType", node.getOsType()});

		/* 添加shell语句查询的节点信息 */
		/* 获取对应配置 */
		Element configOfOS = Constant.prop_nodeCheck.getElement("topic", "OS");
		@SuppressWarnings("unchecked")
		List<Element> prop = configOfOS.element("properties").elements();
		for(Element element : prop) {
			String parameter = element.elementText("parameter");
			maps.add(new String[]{parameter, nodeCheck.get(parameter)});
		}	
		/* 生成输出字符串 */
		StringBuffer answer = new StringBuffer("节点信息：\n");
		/* 拼接表格输出 */
		try {
			answer.append(PrintToTableUtil.printToTable(maps, 50));
		}catch(Exception e) {
			logger.error("build table of node info error, error message is " + e.getMessage());
		}
		answer.append("\n");
		
		return answer.toString();
	}

	@Override
	public String getNtpInfo() {
		/* 用于生成表格 */
		List<String[]> maps = new ArrayList<String[]>();
		/* 添加ntp检测信息 */
		String ntp_result = this.nodeCheck.get("NTP");
		String[] ntp_lines = ntp_result.split("\n");
		for(String line : ntp_lines) {
			String[] items = line.trim().split("\\s+");
			if(items.length > 1) {
				maps.add(items);
			}
		}
		/* 生成输出字符串，拼接表格 */
		StringBuffer answer = new StringBuffer("时间同步信息：\n");
		try {
			answer.append(PrintToTableUtil.printToTable(maps, 10));
		}catch(Exception e) {
			logger.error("build table of ntp info error, error message is " + e.getMessage());
		}
		answer.append("\n");
		return answer.toString();
	}

	@Override
	public String getJavaPathInfo() {
		StringBuffer answer = new StringBuffer("java 路径:\n");
		answer.append(UtilTool.retract(this.nodeCheck.get("JAVA_HOME"), "  "));
		answer.append("\n");
		return answer.toString();
	}

	@Override
	public String getJdkInfo() {
		StringBuffer answer = new StringBuffer("jdk version: \n");
		answer.append(UtilTool.retract(this.nodeCheck.get("jdk_version"), "  "));
		answer.append("\n");
		return answer.toString();
	}

	@Override
	public String getDnsInfo() {
		StringBuffer answer = new StringBuffer("DNS:\n");
		answer.append(UtilTool.retract(this.nodeCheck.get("DNS"), "  "));
		answer.append("\n");
		return answer.toString();
	}

	@Override
	public String getIptableInfo() {
		StringBuffer answer = new StringBuffer("防火墙信息:\n");
		answer.append(UtilTool.retract(this.nodeCheck.get("iptables"), "  "));
		answer.append("\n");
		return answer.toString();
	}
	
	@Override
	public String getNetworkInfo() {
		StringBuffer answer = new StringBuffer("网络信息：\n");
		answer.append(UtilTool.retract(this.nodeCheck.get("ip"), "  "));
		answer.append("\n");
		return answer.toString();
	}

	@Override
	public String getHostsInfo() {
		StringBuffer answer = new StringBuffer("hosts:\n");
		answer.append(UtilTool.retract(this.nodeCheck.get("hosts"), "  "));
		answer.append("\n");
		return answer.toString();
	}

	@Override
	public String getMemoryInfo() {
		List<String[]> maps = new ArrayList<String[]>();
		String memory_result = this.nodeCheck.get("memory");
		String[] lines = memory_result.split("\n");
		for(String line : lines) {
			String[] items = line.trim().split("\\s+");
			maps.add(items);
		}
		StringBuffer answer = new StringBuffer("节点内存检测:\n");
		try {
			answer.append(PrintToTableUtil.printToTable(maps, 20));
		}catch(Exception e) {
			logger.error("build table of memory info error, error message is " + e.getMessage());
		}
		answer.append("\n");
		return answer.toString();
	}

	@Override
	public String getMountInfo() {
		List<String[]> maps = new ArrayList<String[]>();
		String mount_result = this.nodeCheck.get("mount");
		String[] lines = mount_result.split("\n");
		for(String line : lines) {
			if(line.startsWith("#") || line.trim().equals("")) continue;
			maps.add(line.trim().split(" \\s*"));
		}
		StringBuffer answer = new StringBuffer("磁盘挂载检测:\n");
		try {
			answer.append(PrintToTableUtil.printToTable(maps, 30));
		}catch(Exception e) {
			logger.error("build table of mount info error, error message is " + e.getMessage());
		}
		answer.append("\n");
		return answer.toString();
	}

	@Override
	public String getPortInfo() {
		List<String[]> maps = new ArrayList<String[]>();
		for(Iterator<String> keys = this.portChecks.keySet().iterator(); keys.hasNext();) {
			String key = keys.next();
			String result = this.portChecks.get(key);
			maps.add(new String[]{key, result});
		}
		StringBuffer answer = new StringBuffer("端口检测:\n");
		try {
			answer.append(PrintToTableUtil.printToTable(maps, 60));
		}catch(Exception e) {
			logger.error("build table of port check error, error message is " + e.getMessage());
		}
		answer.append("\n");
		return answer.toString();
	}

	@Override
	public String getServiceConfigInfo() {
		List<String[]> maps = new ArrayList<String[]>();
		StringBuffer answer = new StringBuffer("服务配置检测：\n");
		for(Iterator<String> servicenames = this.configBeans.keySet().iterator(); servicenames.hasNext(); ) {
			String servicename = servicenames.next();
			ConfigBean configBean = this.configBeans.get(servicename);
			Vector<String> configFiles = configBean.getConfigFiles();
			if(configFiles.size() == 0) continue;
			answer.append(servicename).append("\n");			
			Map<String, Map<String, String>> configValues = configBean.getConfigValues();
			for(String configFile : configFiles) {
				maps.clear();
				Map<String, String> configValue = configValues.get(configFile);
				for(Iterator<String> keys = configValue.keySet().iterator(); keys.hasNext(); ) {
					String key = keys.next();
					String value = configValue.get(key);
					String[] valueLines = value.split(",");
					int number = valueLines.length;
					for(int i = 0; i < number; i++) {
						if(i == 0) maps.add(new String[]{key, valueLines[i]});
						else maps.add(new String[]{null, valueLines[i]});
					}
				}
				if(maps.size() > 0) {
					answer.append("  ").append(configFile).append(":\n");
					try {
						answer.append(PrintToTableUtil.printToTable(maps, 50));
					}catch(Exception e) {
						logger.error("build table of config file " + configFile + " is error, error message is " + e.getMessage());
					}
					answer.append("\n\n");
				}
			}
		}
		return answer.toString();
	}

	@Override
	public String getMetricInfo() {
		/* 用于存储结果 */
		StringBuffer result = new StringBuffer("指标检测：\n");
		/* 存储用于生成表格的数据信息 */
		List<String[]> maps = new ArrayList<String[]>();
		for(Iterator<String> metricNames = metrics.keySet().iterator(); metricNames.hasNext(); ) {
			String metricName = metricNames.next();
			MetricBean metric = metrics.get(metricName);
			/* 获取单位 */
			String unit = metric.getUnit();
			/* 将检测结果形成表格 */
			List<String> values = metric.getValues();
			for(String value : values) {
				String[] items = value.split(":");
				/* 获取并处理时间戳，将其转换为一般格式 */
				String timestamp = items[0];
				try {
					Date date = new Date(Long.valueOf(timestamp));
					items[0] = Constant.dateFormat.format(date);
				}catch(Exception e) {
					logger.error("change date type error, error message is " + e.getMessage());
				}
				/* 将单位加到值末尾 */
				items[1] += " " + unit;
				maps.add(items);
			}
			if(maps.size() != 0) {
				String name = metric.getMetricName();
				result.append("  ").append(name).append(":\n");
				try {
					result.append(PrintToTableUtil.printToTable(maps, 50)).append("\n");
				}catch(Exception e) {
					logger.error("change value to Table error, error message is " + e.getMessage());
				}
				maps.clear();
			}
			
		}
		return result.toString();
	}
}
