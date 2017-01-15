package io.transwarp.template;

import java.util.HashMap;
import java.util.Map;

import io.transwarp.conn.HttpMethodTool;
import io.transwarp.util.Constant;
import io.transwarp.util.UtilTool;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public abstract class RestAPITemplate implements Runnable{

	private static Logger logger = Logger.getLogger(RestAPITemplate.class);
	protected HttpMethodTool method = null;
	
	public RestAPITemplate(String url, String username, String password) {
		try {
			method = HttpMethodTool.getMethod(url, username, password);
		}catch(Exception e) {
			logger.error("get http method faild, error message is " + e.getMessage());
		}
	}
	
	@Override
	public void run() {
		if(method == null) {
			logger.error("there is no get method");
			return;
		}
		/* 获取集群版本号 */
		this.getVersion();
		/* 获取节点信息 */
		this.getNodeInfo();
		/* 获取服务信息 */
		this.getServices();
		/* 获取服务角色信息 */
		this.getRoleInfo();
		/* 获取服务配置信息 */
//		this.getConfigInfo();
		/* 关闭method连接 */
		this.method.close();
	}
	
	/** 获取集群版本号 */
	public void getVersion() {
		String url = "/manager/version";
		try {
			String result = this.method.execute(url, "get", null);
			this.disposeVersion(result);
		}catch(Exception e) {
			logger.error("get version of cluster is error, error message is " + e.getMessage());
		}
	}
	
	/** 处理分析集群版本号 */
	public abstract void disposeVersion(String json);
	
	/** 获取节点信息 */
	public void getNodeInfo() {
		logger.info("begin get info of node");
		Element config = null;
		try {
			config = Constant.prop_restapi.getElement("purpose", Constant.FIND_MORE_NODE);
		} catch (Exception e) {
			logger.error("get config of getting node info error, error message is " + e.getMessage());
		}
		/* 构建url */
		Map<String, Object> urlParam = new HashMap<String, Object>();
		urlParam.put("viewType", "summary");
		String url = null;
		try {
			url = UtilTool.buildURL(config.elementText("url"), urlParam);
		} catch (Exception e) {
			logger.error("build url of get node info error, error message is " + e.getMessage());
		}
		/* 获取并处理结果 */
		try {
			String result = this.method.execute(url, config.elementText("http-method"), null);
			this.disposeNodeInfo(result);
		} catch (Exception e) {
			logger.error("get information of node error, error message is " + e.getMessage());
		}
	}
	
	/** 处理节点信息 */
	public abstract void disposeNodeInfo(String json);
	
	/** 获取服务信息 */
	public void getServices() {
		logger.info("begin get info of services");
		/* 获取配置 */
		Element config = null;
		try {
			config = Constant.prop_restapi.getElement("purpose", Constant.FIND_MORE_SERVICE);
		}catch(Exception e) {
			logger.error("get config of getting services info is error, error message is " + e.getMessage());
		}
		/* 构建url */
		Map<String, Object> urlParam = new HashMap<String, Object>();
		urlParam.put("viewType", "summary");
		String url = null;
		try {
			url = UtilTool.buildURL(config.elementText("url"), urlParam);
		} catch (Exception e) {
			logger.error("build url of get service info error, error message is " + e.getMessage());
		}
		/* 获取并处理结果 */
		try {
			String result = this.method.execute(url, config.elementText("http-method"), null);
			this.disposeServices(result);
		}catch(Exception e) {
			logger.error("get info of service error, error message is " + e.getMessage());
		}
	}
	
	/** 解析服务信息 */
	public abstract void disposeServices(String json);
	
	/** 获取服务角色信息并加入服务信息中 */
	public void getRoleInfo() {
		logger.info("begin get role info");
		/* 获取配置 */
		Element config = null;
		try {
			config = Constant.prop_restapi.getElement("purpose", Constant.FIND_MORE_SERVICE_ROLE);
		} catch (Exception e) {
			logger.error("get config of getting role info error, error message is " + e.getMessage());
		}
		/* 构建url */
		Map<String, Object> urlParam = new HashMap<String, Object>();
		String url = null;
		try {
			url = UtilTool.buildURL(config.elementText("url"), urlParam);
		} catch (Exception e) {
			logger.error("build url of get role info error, error message is " + e.getMessage());
		}
		/* 获取并处理结果 */
		try {
			String result = this.method.execute(url, config.elementText("http-method"), null);
			this.disposeRole(result);
		} catch(Exception e) {
			logger.error("get info of service role error, error message is " + e.getMessage());
		}
	}
	
	/** 分析服务角色信息 */
	public abstract void disposeRole(String json);
	
	/** 获取服务配置信息 */
//	public abstract void getConfigInfo();
}
