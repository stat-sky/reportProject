package io.transwarp.main;

import io.transwarp.util.UtilTool;

import java.io.InputStream;

public class Test {

	public static void main(String[] args) {
		String cmd = "scp -i /etc/transwarp/transwarp-id_rsa root@192.168.111.6:/etc/inceptorsql2/conf/* /home/xhy/temp";
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			InputStream input = process.getInputStream();
			String result = UtilTool.readInputStream(input);
			System.out.print(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
