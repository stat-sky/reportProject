package io.transwarp.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import io.transwarp.bean.ConfigBean;
import io.transwarp.bean.MetricBean;
import io.transwarp.bean.NodeBean;
import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.conn.ShellUtil;
import io.transwarp.servlet.ConfigCheckRunnable;
import io.transwarp.servlet.DataDictionaryInceptor;
import io.transwarp.servlet.DataDictionaryMysql;
import io.transwarp.servlet.HdfsCheckRunnable;
import io.transwarp.servlet.Information;
import io.transwarp.servlet.LogCheckRunnable;
import io.transwarp.servlet.NodeCheckRunnable;
import io.transwarp.servlet.ProcessCheckRunnable;
import io.transwarp.servlet.RestAPIV45;
import io.transwarp.servlet.RestAPIV46;
import io.transwarp.template.DataDictionaryTemplate;
import io.transwarp.template.RestAPITemplate;
import io.transwarp.util.ConfigRead;
import io.transwarp.util.Constant;
import io.transwarp.util.UtilTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Element;

public class Report extends Information{

	private static Logger logger = Logger.getLogger(Report.class);
	
	private String version;
	private String os;
	
	public Report() {
		init();
	}
	
	public void getReport(String path) throws Exception {
		/* 进行集群检测，获取检测结果 */
		long start = System.currentTimeMillis();
		this.check();
		long end = System.currentTimeMillis();
		System.out.println("check cost time is " + (end - start) * 1.0 / 1000);
		
		/* 集群整体检测 */
		ClusterReportV46 clusterReport = new ClusterReportV46(Information.tdh_version, Information.nodes, 
				Information.services, Information.processChecks, Information.hdfsChecks, Information.tables);
		clusterReport.getReport(path);
		/* 按节点输出节点检测、端口检测、服务配置检测的结果 */
		String outputNodePath = Constant.prop_env.getProperty("goalPath") + "nodeCheck/";
		File outputNode = new File(outputNodePath);
		if(!outputNode.exists()) outputNode.mkdirs();
		for(Iterator<String> hostnames = Information.nodes.keySet().iterator(); hostnames.hasNext(); ) {
			String hostname = hostnames.next();
			try {
				NodeBean node = Information.nodes.get(hostname);
				Map<String, String> nodeCheck = Information.nodeChecks.get(hostname);
				Map<String, ConfigBean> configBean = Information.configs.get(hostname);
				Map<String, String> portCheck = Information.portChecks.get(hostname);
				Map<String, MetricBean> metrics = Information.nodeMetrics.get(hostname);
				NodeReportOfCentOS nodeReport = new NodeReportOfCentOS(node, nodeCheck, configBean, portCheck, metrics);
				nodeReport.getReport(outputNodePath);				
			}catch(Exception e) {
				logger.error("write node check error");
			}

		}
	}
	
	/** 加载配置 */
	public void init() {
		/* 加载log4j配置 */
		PropertyConfigurator.configure("config/log4j.properties");
		logger.info("begin load config");
		try {
			/* 加载环境配置 */
			Constant.prop_env.load(new FileInputStream("config/env.properties"));
			
			/* 加载rest api相关配置 */
			version = Constant.prop_env.getProperty("tdh_version");
			Information.tdh_version = version;
			Constant.prop_restapi = new ConfigRead("config/restapi/restapiURL.xml");
			Constant.prop_config = new ConfigRead("config/restapi/serviceConfig.xml");
			Constant.prop_metric = new ConfigRead("config/restapi/loadMetric.xml");
			
			/* 加载节点检测相关配置 */
			os = Constant.prop_env.getProperty("os");
			if(os == null) os = "";
			if(os.equalsIgnoreCase("CentOS")) {
				Constant.prop_nodeCheck = new ConfigRead("config/shell/CentOS/nodeCheck.xml");
			}else if(os.equalsIgnoreCase("SuSE")) {
				Constant.prop_nodeCheck = new ConfigRead("config/shell/Suse/nodeCheck.xml");
			}else if(os.equalsIgnoreCase("redHat")) {
				Constant.prop_nodeCheck = new ConfigRead("config/shell/CentOS/nodeCheck.xml");
			}else {
				Constant.prop_nodeCheck = new ConfigRead("ocnfig/shell/CentOS/nodeCheck.xml");				
			}
			Constant.prop_portCheck = new ConfigRead("config/shell/portCheck.xml");
			Constant.prop_process = new ConfigRead("config/shell/processCheck.xml");
			Constant.prop_cluster = new ConfigRead("config/shell/clusterCheck.xml");
			
			/* 加载日志检测配置 */
			Constant.prop_logCheck = new ConfigRead("config/logCheck.xml");
			
			/* 生成远程执行的命令 */
			String rootKey = Constant.prop_env.getProperty("rootKey");
			Constant.distCmd = "ssh -i " + rootKey + " ";
			Constant.distScp = "scp -i " + rootKey + " ";
			/* hdfs用户密钥路径 */
			Constant.hdfsKey = Constant.prop_env.getProperty("hdfsKey");
			/* 日志检测脚本存放路径 */
			Constant.scriptPath = Constant.prop_env.getProperty("scriptPath");
			
			/* 建立执行的线程池 */
			Information.threadPool = Executors.newFixedThreadPool(Integer.parseInt(Constant.prop_env.getProperty("threadNum")));;
		} catch (Exception e) {
			logger.error("load properties error, error message is " + e.getMessage());
		}
		logger.info("load config success");
	}
	
	public void check() {
		/* 获取rest api登录信息 */
		String url = "http://" + Constant.prop_env.getProperty("managerIP") + ":8180";
		String username = Constant.prop_env.getProperty("username");
		String password = Constant.prop_env.getProperty("password");
		/* 节点登录用户 */
		String nodeUser = Constant.prop_env.getProperty("nodeUser");
		/* 本地信息存放路径 */
		String goalPath = Constant.prop_env.getProperty("goalPath") + "serviceConfigs/";
		/* 获取集群安全 */
		String security = Constant.prop_env.getProperty("security");
		/* jdbc连接信息 */
		String choose = Constant.prop_env.getProperty("choose");
		if(choose == null) choose = "";

		/* 根据版本号调用rest api获取相关信息 */
		logger.info("begin get info from rest api");
		RestAPITemplate restapi = null;
		if(version.startsWith("4.6")) {
			restapi = new RestAPIV46(url, username, password);
		}else if(version.startsWith("4.5")) {
			restapi = new RestAPIV45(url, username, password);
		}else if(version.startsWith("4.3")) {
			restapi = new RestAPIV45(url, username, password);
		}else {
			restapi = new RestAPIV46(url, username, password);
		}
		restapi.run();
		logger.info("get info from rest api success");
		/* 获取配置文件夹名称和服务名称的映射 */
		/* 存放映射关系 */
		Map<String, String> configMap = new HashMap<String, String>();
		/* 读取json获取相关信息 */
		String configMapJsonPath = Constant.prop_env.getProperty("configMap");
		if(configMapJsonPath != null) {
			try {
				StringBuffer mapJson = new StringBuffer();
				try {
					@SuppressWarnings("resource")
					BufferedReader reader = new BufferedReader(new FileReader(configMapJsonPath));
					String line = null;
					while(true) {
						line = reader.readLine();
						if(line == null) break;
						mapJson.append(line);
					}
				}catch(Exception e) {
					logger.error("read json file error, error message is " + e.getMessage());
				}
				JSONArray array = JSONArray.fromObject(mapJson.toString());
				int num = array.size();
				for(int i = 0; i < num; i++) {
					JSONObject serviceConfig = array.getJSONObject(i);
					Object activeStatus = serviceConfig.get("activeStatus");
					if(activeStatus == null || activeStatus.toString().equalsIgnoreCase("DELETED")) continue;
					Object sid = serviceConfig.get("sid");
					Object name = serviceConfig.get("name");
					if(sid == null || name == null) continue;
					configMap.put(name.toString(), sid.toString());
				}
			}catch(Exception e) {
				logger.error("read Service.json error, error message is " + e.getMessage());
			}
		}
		int configMapSize = configMap.size();
		
		/* 存放包含 kadmin 服务角色的节点ip，用于查询hdfs信息 */
		List<String> ips = new ArrayList<String>();
		
		/* 根据rest api获取的节点信息对所有节点检测 */
		for(Iterator<String> hostnames = Information.nodes.keySet().iterator(); hostnames.hasNext(); ) {
			String hostname = hostnames.next();
			NodeBean node = Information.nodes.get(hostname);
			String nodeStatus = node.getStatus();
			if(nodeStatus.equals("Disassociated")) continue;
			ips.add(node.getIpAddress());
			/* 建立线程进行日志检测 */
			Information.threadPool.execute(new LogCheckRunnable(node, nodeUser, Constant.scriptPath, configMap));
			/* 建立线程进行节点检测 */
			Information.threadPool.execute(new NodeCheckRunnable(node, nodeUser));
			/* 建立线程进行服务配置检测 */
			if(configMapSize > 0) {
				Information.threadPool.execute(new ConfigCheckRunnable(configMap, node, nodeUser, goalPath));
				Information.totalTask += 1;
			}else {
				logger.error("read Service.json faild");
			}
			/* 执行线程数加2 */
			Information.totalTask += 2;			
		}
		
		/* 根据服务信息中的角色列表进行进程检测 */
		/* 获取进程检测要求配置 */
		List<Element> processConfigs = Constant.prop_process.getAll();
		/* 遍历服务，进行进程检测 */
		for(Iterator<String> serviceNames = Information.services.keySet().iterator(); serviceNames.hasNext(); ) {
			String serviceName = serviceNames.next();
			ServiceBean service = Information.services.get(serviceName);
			List<RoleBean> roles = service.getRoles();
			for(RoleBean role : roles) {
				for(Element processConfig : processConfigs) {
					String serviceRole = processConfig.elementText("serviceRoleType");
					if(role.getRoleType().equals(serviceRole)) {
						if(role.getHealth().equals("DOWN")) continue;
						String ip = role.getNode().getIpAddress();
						String topic = serviceName + ":" + serviceRole;
						Information.threadPool.execute(new ProcessCheckRunnable(ip, nodeUser, processConfig.element("properties"), topic));
						Information.totalTask += 1;
					}
				}
			}
		}
		
		/* 若开启kerberos则需要在进行hdfs检测和表空间检测的节点生成keytab */
		/* 若开启kerberos认证，则在执行节点上生成keytab */
		try {
			if(security.equals("kerberos") || security.equals("all")) {
				for(String ip : ips) {
					StringBuffer path2 = new StringBuffer();
					path2.append(nodeUser).append("@").append(ip).append(":")
						.append(Constant.scriptPath);
					ShellUtil.scpFile(Constant.hdfsKey, path2.toString());
				}
			}
		}catch(Exception e) {
			logger.error("build keytab error, error message is " + e.getMessage());
		}

		/* 建立线程进行hdfs检测 */
		try {
			Information.threadPool.execute(new HdfsCheckRunnable(security, ips.get(0), nodeUser));
			Information.totalTask += 1;			
		}catch(Exception e) {
			logger.error("execute hdfs check error, error message is " + e.getMessage());
		}

		/* 数据表空间检测 */
		DataDictionaryTemplate dataCheck = null;
		if(choose.equals("inceptor")) {
			dataCheck = new DataDictionaryInceptor(security, ips, nodeUser);
		}else if(choose.equals("mysql")) {
			dataCheck = new DataDictionaryMysql(security, ips, nodeUser);
		}else {
			dataCheck = new DataDictionaryMysql(security, ips, nodeUser);
		}
		dataCheck.beginTableCheckRunnabl();
		/* 判断多线程执行是否结束 */
		int completeNum = 0;
		int count = 0;
		while(true) {
			int value = Information.successTask.intValue();
			if(value == Information.totalTask) {
				Information.threadPool.shutdown();
				break;
			}
			try {
				/* 判断若达到10次检测都没有线程完成，则当作线程阻塞强行关闭线程池 */
				if(completeNum == value) {
					count++;
				}else {
					count = 0;
					completeNum = value;
				}
				if(count >= 10) {
					logger.error("there are 50 sec is no sueess");
					Information.threadPool.shutdown();
					break;
				}
				logger.info("now successTask is : " + Information.successTask.intValue() + ", total is " + Information.totalTask + " sleep 5 sec");
				Thread.sleep(5000);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		if(security.equals("all") || security.equals("kerberos")) {
			/* 根据配置决定是否删除发送的hdfs的keytab */
			String deleteFile = Constant.prop_env.getProperty("deleteFile");
			if(deleteFile.equals("true")) {
				for(Iterator<String> hostnames = Information.nodes.keySet().iterator(); hostnames.hasNext(); ) {
					String hostname = hostnames.next();
					NodeBean node = Information.nodes.get(hostname);
					try {
						ShellUtil.executeDist("rm -rf " + Constant.scriptPath + "hdfs.keytab", nodeUser, node.getIpAddress());
					}catch(Exception e) {
						logger.error("delete file error, " + e.getMessage());
					}
				}
			}
		}

		try {
			UtilTool.compressTarGz(goalPath);
			UtilTool.deleteFile(goalPath);
		} catch (Exception e) {
			logger.error("compress folder error, error messge is " + e.getMessage());
		}
	}
	

}
