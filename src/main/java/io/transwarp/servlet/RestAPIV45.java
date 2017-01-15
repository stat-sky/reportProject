package io.transwarp.servlet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.transwarp.bean.RoleBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.util.Constant;
import io.transwarp.util.UtilTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class RestAPIV45 extends RestAPIV46{
	
	private static Logger logger = Logger.getLogger(RestAPIV45.class);

	public RestAPIV45(String url, String username, String password) {
		super(url, username, password);
	}
	
	@Override
	public void getRoleInfo() {
		logger.info("begin ge service role info");
		/* 获取配置 */
		Element config = null;
		String urlOfConfig = null;
		try {
			config = Constant.prop_restapi.getElement("purpose", Constant.FIND_MORE_SERVICE_ROLE);
			urlOfConfig = config.elementText("url");
		}catch(Exception e) {
			logger.error("get config of getting role info error, error message is " + e.getMessage());
		}
		/* 按照服务ID查询服务角色 */
		for(Iterator<String> servicenames = Information.services.keySet().iterator(); servicenames.hasNext(); ) {
			String servicename = servicenames.next();
			ServiceBean service = Information.services.get(servicename);
			/* 构建url */
			Map<String, Object> urlParam = new HashMap<String, Object>();
			urlParam.put("serviceId", service.getServiceId());
			String url = null;
			try {
				url = UtilTool.buildURL(urlOfConfig, urlParam);
			}catch(Exception e) {
				logger.error("build url of get role info error, error message is " + e.getMessage());
			}
			/* 获取结果 */
			String result = null;
			try {
				result = method.execute(url, config.elementText("http-method"), null);
			}catch(Exception e) {
				logger.error("get info of service role error, error message is " + e.getMessage());
			}
			/* 分析结果，将角色信息填入服务信息中 */
			try {
				JSONArray array = JSONArray.fromObject(result);
				int num = array.size();
				for(int i = 0; i < num; i++) {
					JSONObject json = array.getJSONObject(i);
					RoleBean role = new RoleBean(json);
					service.addRole(role);
				}
			}catch(Exception e) {
				logger.error("analysis service role error, error message is " + e.getMessage());
			}
		}
		
	}
	
}
