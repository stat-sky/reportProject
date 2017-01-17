package io.transwarp.servlet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import io.transwarp.bean.MetricBean;
import io.transwarp.bean.NodeBean;
import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.template.RestAPITemplate;
import io.transwarp.util.Constant;
import io.transwarp.util.UtilTool;

public class RestAPIV46 extends RestAPITemplate{
	
	private static Logger logger = Logger.getLogger(RestAPIV46.class);

	public RestAPIV46(String url, String username, String password) {
		super(url, username, password);
		
	}

	@Override
	public void disposeVersion(String json) {
		JSONObject result = null;
		try {
			result = JSONObject.fromObject(json);
		}catch(Exception e) {
			logger.error("get version error, error message is " + e.getMessage());
			return;
		}
		String version = result.getString("version");
		if(version != null) {
			Information.tdh_version = version;
		}
	}

	@Override
	public void disposeNodeInfo(String json) {
		JSONArray array = null;
		try {
			array = JSONArray.fromObject(json);
		} catch(Exception e) {
			logger.error("get node info error, error message is " + e.getMessage());
		}
		int nodeNum = array.size();
		for(int i = 0; i < nodeNum; i++) {
			JSONObject jsonNode = array.getJSONObject(i);
			/* 获取结果写入java bean */
			try {
				NodeBean node = new NodeBean(jsonNode);
				Information.nodes.put(node.getHostName(), node);			
			}catch(Exception e) {
				logger.error("build node java bean error, error message is " + e.getMessage());
			}

		}
		
	}

	@Override
	public void disposeServices(String json) {
		JSONArray array = null;
		try {
			array = JSONArray.fromObject(json);
		} catch(Exception e) {
			logger.error("get service info error, error message is " + e.getMessage());
		}
		int serviceNum = array.size();
		for(int i = 0; i < serviceNum; i++) {
			JSONObject jsonService = array.getJSONObject(i);
			/* 将结果写入java bean */
			try {
				ServiceBean service = new ServiceBean(jsonService);
				Information.services.put(service.getServiceName(), service);
			}catch(Exception e) {
				logger.error("build service java bean error, error message is " + e.getMessage());
			}
		}
		
	}

	@Override
	public void disposeRole(String json) {
		JSONArray array = null;
		try {
			array = JSONArray.fromObject(json);
		} catch(Exception e) {
			logger.error("get service role error, error message is " + e.getMessage());
		}
		int roleNum = array.size();
		for(int i = 0; i < roleNum; i++) {
			JSONObject jsonRole = array.getJSONObject(i);
			/* 将结果写入java bean */
			try {
				RoleBean role = new RoleBean(jsonRole);
				ServiceBean service = role.getService();
				if(service != null) {
					ServiceBean saveService = Information.services.get(service.getServiceName());
					if(saveService != null) {
						saveService.addRole(role);
					}else {
						service.addRole(role);
						Information.services.put(service.getServiceName(), service);
					}
				}				
			}catch(Exception e) {
				logger.error("build service role error, error message is " + e.getMessage());
			}

		}
		
	}

	@Override
	public void getMetricsInfo() {
		/* 获取rest api配置 */
		Element metricConfig = Constant.prop_restapi.getElement("purpose", Constant.NODE_METRIC);
		String originalUrl = metricConfig.elementText("url");
		String httpMethod = metricConfig.elementText("http-method");
		/* 获取指标的时间区间，为当前时间往前记24小时 */
		long end = System.currentTimeMillis();
		long start = end - 24 * 60 * 60 * 1000;
		/* 构建url的参数 */
		Map<String, Object> urlParam = new HashMap<String, Object>();
		urlParam.put("startTimeStamp", start);
		urlParam.put("endTimeStamp", end);
		List<Element> configs = Constant.prop_metric.getAll();
		/* 按照节点循环获取指标 */
		for(Iterator<String> hostnames = Information.nodes.keySet().iterator(); hostnames.hasNext(); ) {
			String hostname = hostnames.next();
			NodeBean node = Information.nodes.get(hostname);
			String id = node.getNodeId();
			urlParam.put("nodeId", id);
			/* 存放该节点的指标信息，参数依次为指标名称，指标内容 */
			Map<String, MetricBean> metrics = new HashMap<String, MetricBean>();
			/* 循环遍历所有指标 */
			for(Element config : configs) {
				String metricName = config.elementText("metric");
				String name = config.elementText("name");
				urlParam.put("metricsName", metricName);
				/* 构建url */
				String url = null;
				try {
					url = UtilTool.buildURL(originalUrl, urlParam);
				}catch(Exception e) {
					logger.error("build url of get metric error, nodeId is " + id + ", metric is " + metricName + ", error message is " + e.getMessage());
				}
				/* 执行http方法获取结果并解析 */
				try {
					String result = this.method.execute(url, httpMethod, null);
					if(result != null) {
						MetricBean metric = analysisMetric(result, name);
						metrics.put(metricName, metric);
					}
				}catch(Exception e) {
					logger.error("execute of getting metric of node error, nodeId is " + id + ", metric is " + metricName + ", error message is " + e.getMessage());
				}
			}
			Information.nodeMetrics.put(hostname, metrics);
		}
	}

	private MetricBean analysisMetric(String json, String name) {
		MetricBean metric = new MetricBean();
		JSONArray array = JSONArray.fromObject(json);
		int num = array.size();
		for(int i = 0; i < num; i++) {
			JSONObject obj = array.getJSONObject(i);
			String unit = obj.getString("unit");
			String timestamp = obj.getString("timestamp");
			String value = obj.getJSONObject("metricValue").getString("value");
			metric.setMetricName(name);
			metric.setUnit(unit);
			metric.addValue(timestamp + ":" + value);
		}
		return metric;
	}
}
