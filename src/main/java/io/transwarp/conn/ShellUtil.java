package io.transwarp.conn;

import io.transwarp.util.Constant;
import io.transwarp.util.UtilTool;

import java.io.InputStream;

import org.apache.log4j.Logger;


public class ShellUtil {
	
	private static Logger logger = Logger.getLogger(ShellUtil.class);
	
	public static String executeDist(String cmd, String nodeUser, String ip, long waitTime) throws Exception {
		StringBuffer command = new StringBuffer(Constant.distCmd);
		command.append(nodeUser).append("@").append(ip).append(" ").append(cmd);
		logger.debug("dist command is " + command.toString());
		return executeLocal(command.toString(), waitTime);
	}
	
	public static String executeDist(String cmd, String nodeUser, String ip) throws Exception {
		return executeDist(cmd, nodeUser, ip, 0);
	}
	
	public static String executeLocal(String cmd, long waitTime) throws Exception {
		Process process = Runtime.getRuntime().exec(cmd);
		if(waitTime != 0) {
			try {
				Thread.sleep(waitTime);
			}catch(Exception e) {}
		}
		InputStream input = process.getInputStream();
		String result = UtilTool.readInputStream(input);
		input.close();
		return result;
	}
	
	/**
	 * 将path1下的文件和文件夹拷贝到path2路径下
	 * @param path1
	 * @param path2
	 * @throws Exception
	 */	
	public static void scpDir(String path1, String path2) throws Exception {
		StringBuffer command = new StringBuffer(Constant.distScp);
		command.append("-r ").append(path1).append(" ").append(path2);
		logger.debug("scp command is " + command.toString());
		executeLocal(command.toString(), 0);
	}
	
	/**
	 * 将path1下的文件拷贝到path2路径下(不拷贝文件夹)
	 * @param path1
	 * @param path2
	 * @throws Exception
	 */	
	public static void scpFile(String path1, String path2) throws Exception {
		StringBuffer command = new StringBuffer(Constant.distScp);
		command.append(path1).append(" ").append(path2);
		logger.debug("scp command is " + command.toString());
		executeLocal(command.toString(), 0);	
	}
}
