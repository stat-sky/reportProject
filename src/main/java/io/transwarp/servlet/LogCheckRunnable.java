package io.transwarp.servlet;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.transwarp.bean.NodeBean;
import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.conn.ShellUtil;
import io.transwarp.util.Constant;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class LogCheckRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(LogCheckRunnable.class);
	
	private NodeBean node;
	private String nodeUser;
	private String logCheckPath;
	private Map<String, String> serviceMap;
	
	public LogCheckRunnable(NodeBean node, String nodeUser, String logCheckPath, Map<String, String> serviceMap) {
		this.node = node;
		this.nodeUser = nodeUser;
		this.logCheckPath = logCheckPath;
		this.serviceMap = serviceMap;
	}
	
	@Override
	public void run() {
		/* 发送执行脚本 */
		sendJar();
		/* 停止 2 sec 等待脚本传输 */
		try {
			Thread.sleep(2000);
		}catch(Exception e) {}
		/* 获取时间戳 */
		long dateTime = System.currentTimeMillis();
		/* 指定生成文件夹 */
		String logDir = "result-" + dateTime + "/";
		/* 根据节点拥有的服务角色生成执行语句 */
		StringBuffer command = new StringBuffer("java -jar ");
		command.append(this.logCheckPath).append("logCheck.jar")
			.append(" ").append(node.getHostName())    //拼接节点hostname
			.append(" ").append(this.logCheckPath).append("logCheck.xml")  //拼接配置路径
			.append(" ").append(this.logCheckPath).append(logDir);  //拼接生成文件夹
		/* 遍历所有服务， */
		List<Element> configs = Constant.prop_logCheck.getAll();
		for(Iterator<String> servicenames = Information.services.keySet().iterator(); servicenames.hasNext();) {
			String servicename = servicenames.next();
			ServiceBean service = Information.services.get(servicename);
			List<RoleBean> roles = service.getRoles();
			for(RoleBean role : roles) {
				try {
					String nodeIP = role.getNode().getIpAddress();
					if(!nodeIP.equals(node.getIpAddress())) continue;
				}catch(Exception e) {
					logger.error("get nodeIP of role error, error message is " + e.getMessage());
					continue;
				}
				String roleType = role.getRoleType();
				for(Element config : configs) {
					String serviceRole = config.elementText("serviceRole");
					if(roleType.matches("\\S*" + serviceRole + "\\S*")) {
						command.append(" ").append(servicename).append(":").append(serviceRole).append(":").append(serviceMap.get(servicename));
						break;
					}
				}
			}
		}
		/* 拼接执行结果输出路径 */
		command.append(" > ").append(this.logCheckPath).append("logCheck.log 2>&1");
		/* 执行命令进行检测 */
		try {
//			logger.info(command.toString());
			ShellUtil.executeDist(command.toString(), nodeUser, node.getIpAddress());
		}catch(Exception e) {
			logger.error("execute shell of check log error, node ip is " + node.getIpAddress() + ", error message is " + e.getMessage());
		}
		/* 将执行结果拷贝回本地 */
		logger.info("begin scp result");
		String path_result = this.nodeUser + "@" + node.getIpAddress() + ":" + this.logCheckPath + logDir;
		String path_log = this.nodeUser + "@" + node.getIpAddress() + ":" + this.logCheckPath + "logCheck.log";
		String path_local = Constant.prop_env.getProperty("goalPath") + "logCheck/" + node.getHostName() + "/";
		/* 判断本地路径是否存在，若不存在则进行创建 */
		File dir = new File(path_local);
		if(!dir.exists()) dir.mkdirs();
		/* 执行命令，将结果拷贝回本地 */
		try {
			ShellUtil.scpDir(path_result, path_local);
			ShellUtil.scpFile(path_log, path_local);
		}catch(Exception e) {
			logger.error("scp result to local error, error node is " + node.getIpAddress() + ", error message is " + e.getMessage());
		}
		
		/* 检测完成，令计数器加1 */
		logger.info("log check of node " + node.getHostName() + " is completed");
		Information.successTask.incrementAndGet();
	}
	
	/* 将执行jar包发送到要进行日志检测的节点下 */
	private void sendJar() {
		int num = Constant.LOGCHECKPAHT.length;
		for(int i = 0; i < num; i++) {
			StringBuffer goalPath = new StringBuffer();
			goalPath.append(nodeUser).append("@").append(node.getIpAddress()).append(":").append(this.logCheckPath);
			try {
				ShellUtil.scpDir(Constant.LOGCHECKPAHT[i], goalPath.toString());
			}catch(Exception e) {
				logger.error("send script to node error, node ip is " + node.getIpAddress() + ", error message is " + e.getMessage());
			}
		}
	}
}
