package io.transwarp.main;

import io.transwarp.util.UtilTool;

import java.io.File;
import java.io.InputStream;


public class TestScpConfig {

	public static void main(String[] args) {
		String serviceName = "HDFS1";
		
		
		String[] ips = new String[]{"172.16.2.93", "172.16.2.94", "172.16.2.95"};
		for(String ip : ips) {
			System.out.println(ip + ":");
			StringBuffer configPath = new StringBuffer("root@172.16.2.93:/etc/");
			configPath.append(serviceName.toLowerCase()).append("/conf/*");
			System.out.println(configPath.toString());
			
			String goalPath = "/home/xhy/temp/" + serviceName.toLowerCase() + "/configFile/" + ip;
			File file = new File(goalPath);
			if(!file.exists()) file.mkdirs();
			
			String scp = "scp -i /tmp/transwarp-id_rsa " + configPath.toString() + " " + goalPath;
			System.out.println(scp);
			try {
				Process process = Runtime.getRuntime().exec(scp);
				InputStream input = process.getInputStream();
				String result = UtilTool.readInputStream(input);
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
	/*		try {
				Thread.sleep(10000);
			}catch(Exception e) {}*/
			
//			file = new File(goalPath);
			deepSearchFile(file);
			System.out.println("\n\n");
		}

	}
	
	public static void deepSearchFile(File file) {
		if(file.isDirectory()) {
			File[] children = file.listFiles();
			for(File child : children) {
				deepSearchFile(child);
			}
		}else {
			String path = file.getAbsolutePath();
			if(path.endsWith(".xml") || path.endsWith(".sh") || path.endsWith("env")) {
				System.out.println(file.getAbsolutePath());
			}
		}
	}
}
