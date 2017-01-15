package io.transwarp.servlet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.transwarp.bean.NodeBean;
import io.transwarp.conn.ShellUtil;
import io.transwarp.util.Constant;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class NodeCheckRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(NodeCheckRunnable.class);
	private NodeBean node = null;
	private String nodeUser = null;
	
	public NodeCheckRunnable(NodeBean node, String nodeUser) {
		this.node = node;
		this.nodeUser = nodeUser;
	}
	
	@Override
	public void run() {
		/* 节点基础检测 */
		nodeBaseCheck();
		/* 端口检测 */
		portCheck();
		/* 检测完成，令计数器加1 */
		logger.info("check info of node " + node.getHostName() + " is completed");
		Information.successTask.incrementAndGet();
	}
	
	public void nodeBaseCheck() {
		/* 存放查询结果，参数依次为 检测项，结果 */
		Map<String, String> answer = new ConcurrentHashMap<String, String>();
		/* 获取相关配置 */
		List<Element> configs = null;
		try {
			configs = Constant.prop_nodeCheck.getAll();
		} catch(Exception e) {
			logger.error("get config of node check error, error message is " + e.getMessage());
		}
		if(configs == null) return;
		
		/* 遍历配置，根据配置进行查询 */
		for(Element config : configs) {
			/* 获取检查项 */
			Element properties = config.element("properties");
			if(properties == null) return;
			@SuppressWarnings("unchecked")
			List<Element> items = properties.elements();
			/* 遍历检查项进行检查 */
			for(Element item : items) {
				String command = item.elementText("command");
				String checkItem = item.elementText("parameter");
				String result = null;
				try {
					result = ShellUtil.executeDist(command, nodeUser, node.getIpAddress());
				}catch(Exception e) {
					logger.error("execute check shell is error, check item is " + checkItem + ", error message is " + e.getMessage());
				}
				if(result == null) {
					logger.error("cmd result is null, command is " + command);
					continue;
				}
				/* 将结果去掉首尾空格后存入 */
				answer.put(checkItem, result.trim());
			}
		}
		/* 将检测结果存入Information */
		Information.nodeChecks.put(node.getHostName(), answer);
	}
	
	public void portCheck() {
		/* 存放查询结果，参数依次为 检测项，检测结果 */
		Map<String, String> answer = new ConcurrentHashMap<String, String>();
		/* 获取配置 */
		List<Element> configs = null;
		try {
			configs = Constant.prop_portCheck.getAll();
		}catch(Exception e) {
			logger.error("get config of port check error, error message is " + e.getMessage());			
		}
		if(configs == null) return;
		/* 遍历配置进行检测 */
		for(Element config : configs) {
			String roleType = config.elementText("roleType");
			String port = config.elementText("port");
			String command = config.elementText("command");
			String result = null;
			try {
				result = ShellUtil.executeDist(command, nodeUser, node.getIpAddress());
			}catch(Exception e) {
				logger.error("execute check shell is error, check port is " + port + ", error message is " + e.getMessage());
			}
			if(result == null) continue;
			String topic = "服务" + roleType + "的 " + port + " 端口";
			answer.put(topic, result.trim());
		}
		/* 将结果存入Information */
		Information.portChecks.put(node.getHostName(), answer);
	}
}
