package io.transwarp.main;

import org.apache.log4j.PropertyConfigurator;

import io.transwarp.conn.HttpMethodTool;

public class MetricTes {

	public static void main(String[] args) {
		PropertyConfigurator.configure("config/log4j.properties");
		try {
			long endTime = System.currentTimeMillis();
			long start = endTime - 24 * 60 * 60 * 1000;
			String url = "http://172.16.1.109/ganglia/graph.php?r=hour&me=cluster_1&m=load_one&s=by+name&mc=2&g=mem_report&json=1";
			System.out.println(url);
			HttpMethodTool method = HttpMethodTool.getMethod("http://172.16.2.63:8180", "xhy", "123456");
			String result = method.execute(url, "get", null);
			System.out.println(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
