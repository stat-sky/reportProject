package io.transwarp.servlet;

import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import io.transwarp.bean.NodeBean;
import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.template.RestAPITemplate;

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

/*	@Override
	public void getConfigInfo() {
		 获取配置 
		Element restapiConfig = null;
		try {
			restapiConfig = Constant.prop_restapi.getElement("purpose", Constant.DOWNLOAD_CONFIG);
		} catch (Exception e) {
			logger.error("get config of getting node info error, error message is " + e.getMessage());
		}
		 根据配置中的要求获取服务配置 
		String[] configTopics = null;
		try {
			Element element = Constant.prop_config.getElement("topic", "services");
			configTopics = element.elementText("property").split(";");
		} catch (Exception e) {
			logger.error("get config of service config error, error message is " + e.getMessage());
		}
		 遍历所有服务，选择配置中指定的服务下载并分析服务配置信息，并将结果存入Information的静态变量 
		for(Iterator<String> keys = Information.services.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			ServiceBean service = Information.services.get(key);
			try {
				for(String configTopic : configTopics) {
					if(service.getType().equals(configTopic)) {
						logger.info("begin get config of service : " + service.getServiceName());
						 为指定的服务，开始下载服务配置并解析 
						 构建url 
						Map<String, Object> urlParam = new HashMap<String, Object>();
						urlParam.put("serviceId", service.getServiceId());
						urlParam.put("fileName", "config");
						String url = null;
						try {
							url = UtilTool.buildURL(restapiConfig.elementText("url"), urlParam);
						} catch (Exception e) {
							logger.error("build url of download service config, error message is " + e.getMessage());
						}
						 获取结果，并将其解析为bean存入Information的静态变量 
						Map<String, byte[]> configFiles = this.method.getConfig(url, service.getType());
						this.analysisConfigFile(configTopic, configFiles, service.getServiceName());
					}
				}				
			}catch(Exception e) {
				logger.error("get information of config is error, error message is " + e.getMessage());
			}
			
		}
	}
	
	 分析下载的配置，并将结果存入Information的静态变量 
	private void analysisConfigFile(String configTopic, Map<String, byte[]> configFiles, String serviceName) throws Exception{
		for(Iterator<String> keys = configFiles.keySet().iterator(); keys.hasNext(); ) {
			String key = keys.next();
			 过滤不需要的文件 
			if(!key.endsWith(".sh") && !key.endsWith(".xml") && !key.endsWith("-env")) {
				continue;
			}
			String fileName = UtilTool.getFileName(key);
			 获取对应的配置文件 
			Element config = null;
			try {
				config = Constant.prop_config.getElement("topic", fileName);
			} catch (Exception e) {
				logger.error("get config of service config error, error message is " + e.getMessage());
			}
			if(config == null) {
				 若读取的配置为空，则说明该配置也是不需要的 
				continue;
			}
			 获取该配置所在的节点的hostname 
			String[] dirs = key.split("/");
			if(dirs.length < 2) {
				logger.error("config file name is error");
			}
			String hostname = dirs[1];
			Map<String, String> configValue = new HashMap<String, String>();
			 解析服务配置信息 
			if(key.endsWith(".sh") || key.endsWith("-env")) {
				logger.debug("config file is : " + key);
				 解析.sh或-env的配置文件 
				 获取需要截取的配置项 
				String parameter = config.elementText("property");
				if(parameter == null) {
					continue;
				}
				String[] itemKeys = parameter.split(";");
				 将配置文件内容按行切分 
				String[] lines = new String(configFiles.get(key)).split("\n");
				for(String line : lines) {
					if(line.startsWith("#") || line.equals("")) {
						 过滤空白行和注释行 
						continue;
					}
					 确定赋值语句的变量位置 
					int beginIndex = line.indexOf(" ", 1);
					int endIndex = line.indexOf("=", 1);
					if(endIndex == -1) {
						 过滤非赋值语句 
						continue;
					}
					if(beginIndex > endIndex || beginIndex == -1) {
						beginIndex = 0;
					}
					 获取赋值语句的变量名和变量值,并去除头尾空白 
					String paramName = line.substring(beginIndex, endIndex).trim();
					for(String itemKey : itemKeys) {
						if(paramName.indexOf(itemKey) != -1) {
							String paramValue = line.substring(endIndex + 1).trim();
							 去掉变量值中可能存在的引号 
							paramValue = paramValue.replaceAll("\"", "");
							 将变量值存入，若已经存在该变量，则对其拼接 
							String oldValue = configValue.get(paramName);
							if(oldValue != null) {
								paramValue = oldValue + "," + paramValue;
							}
							configValue.put(paramName, paramValue);							
						}
					}

				}
			}else if(key.endsWith(".xml")) {
				logger.info("config file is : " + key);
				 解析xml配置文件 
				 根据程序配置文件的要求过滤选取其中的配置 
				String parameter = config.elementText("property");
				if(parameter == null) {
					continue;
				}
				String[] items = parameter.split(";");
				Configuration configuration = HBaseConfiguration.create();
				byte[] fileValue = configFiles.get(key);
				InputStream inputStream = new ByteArrayInputStream(fileValue);
				configuration.addResource(inputStream);
				for(String item : items) {
					String value = configuration.get(item);
					if(value == null) continue;
					configValue.put(item, value);
				}
			}
			Map<String, ConfigBean> serviceConfigs = Information.configs.get(hostname);
			if(serviceConfigs == null) {
				serviceConfigs = new ConcurrentHashMap<String, ConfigBean>();
			}
			ConfigBean configBean = serviceConfigs.get(serviceName);
			if(configBean == null) {
				configBean = new ConfigBean(serviceName);
			}
			configBean.addConfigFile(fileName, configValue);
			serviceConfigs.put(serviceName, configBean);
			Information.configs.put(hostname, serviceConfigs);
		}
	}*/

}
