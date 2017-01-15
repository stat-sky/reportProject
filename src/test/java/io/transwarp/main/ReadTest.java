package io.transwarp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ReadTest {

	public static void main(String[] args) {
		String path = "/tmp/Service.json";
		Map<String, String> configPath = new HashMap<String, String>();
		StringBuffer serviceInfo = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			String len = null;
			while(true) {
				len = reader.readLine();
				if(len == null) break;
				serviceInfo.append(len);
			}
			JSONArray array = JSONArray.fromObject(serviceInfo.toString());
			int num = array.size();
			for(int i = 0; i < num; i++) {
				JSONObject json = array.getJSONObject(i);
				String serviceName = json.getString("name");
				String activeStatus = json.getString("activeStatus");
				if(activeStatus.equals("DELETED")) continue;
				String sid = json.getString("sid");
				configPath.put(serviceName, sid);
			}
			System.out.println("ok");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
