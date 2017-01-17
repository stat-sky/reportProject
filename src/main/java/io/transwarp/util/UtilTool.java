package io.transwarp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class UtilTool {
	
	private static Logger logger = Logger.getLogger(UtilTool.class);

	/**
	 * 将字符串首字母改为大写字母
	 * @param oldString 需要进行修改的字符串
	 * @return 修改后的字符串
	 */
	public static String changeFirstCharToCapital(String oldString) {
		byte[] items = oldString.getBytes();
		int ch = items[0];
		if(ch > 'a' && ch < 'z') {
			ch = ch - 'a' + 'A';
			items[0] = (byte)ch;
		}
		return new String(items);
	}
	
	/**
	 * 根据带占位符的url和url参数来构建可用的url字符串
	 * @param original 带占位符的url
	 * @param urlParam url中使用的参数
	 * @return 可用的url
	 * @throws Exception 构建失败
	 */
	public static String buildURL(String original, Map<String, Object> urlParam) throws Exception {
		if(urlParam == null) urlParam = new HashMap<String, Object>();
		String url = null;
		if(original.indexOf("{") == -1) {
			logger.debug("this url has not parameter");
			url = original;
		}else if(original.indexOf("[") == -1) {
			logger.debug("this url has required parameter but not optional parameter");
			url = buildURLWithRequired(original, urlParam);
		}else {
			logger.debug("this url has optional parameter");
			url = buildURLWithOptional(original, urlParam);
		}
		return url;
	}
	//存在且仅存在必选参数的url构建
	private static String buildURLWithRequired(String original, Map<String, Object> urlParam) throws Exception{
		StringBuffer urlBuild = new StringBuffer();
		String[] urlSplits = original.split("\\{");
		int numberOfSplit = urlSplits.length;
		if(numberOfSplit < 1) {
			throw new RuntimeException("原始url切分错误");
		}
		urlBuild.append(urlSplits[0]);
		for(int i = 1; i < numberOfSplit; i++) {
			String[] items = urlSplits[i].split("\\}");
			Object value = urlParam.get(items[0]);
			if(value == null || value.equals("")) {
				throw new RuntimeException("there is not this param : " + items[0]);
			}
			urlBuild.append(value);
			if(items.length == 2) urlBuild.append(items[1]);  
			
		}
		return urlBuild.toString();
	}
	/* 存在可选参数的url构建 */
	private static String buildURLWithOptional(String original, Map<String, Object> urlParam) throws Exception {
		StringBuffer urlBuild = new StringBuffer();
		String[] urlSplitByOptionals = original.split("\\[");
		int numberOfSplit = urlSplitByOptionals.length;
		if(numberOfSplit < 1) {
			throw new RuntimeException("原始url切分错误");
		}		
		urlBuild.append(buildURL(urlSplitByOptionals[0], urlParam));
		boolean hasParam = (urlBuild.toString().indexOf("?") == -1) ? false : true;
		for(int i = 0; i < numberOfSplit; i++) {
			urlSplitByOptionals[i] = urlSplitByOptionals[i].substring(1, urlSplitByOptionals[i].length() - 1);
			logger.debug("urlSplitByOptional is : " + urlSplitByOptionals[i]);
			String[] items = urlSplitByOptionals[i].split("\\&");
			for(int j = 0; j < items.length; j++) {
				try {
					String urlSplit = buildURLWithRequired(items[j], urlParam);
					logger.debug("read : " + items[j] + "  " + urlSplit);
					if(hasParam) {
						urlBuild.append("&").append(urlSplit);
					}else {
						urlBuild.append("?").append(urlSplit);
						hasParam = true;
					}
				}catch(RuntimeException e) {}
			}
		}
		return urlBuild.toString();
	}
	
	/**
	 * 读取tar.gz格式的压缩文件，结果以map返回，其中key为文件名，value为文件内容的byte数组
	 * @param inputStream tar.gz的inputStream输入
	 * @param serviceType tar.gz文件所属服务类型
	 * @return map存储的压缩文件内容
	 * @throws Exception 解析失败
	 */
	public static Map<String, byte[]> readInputStreamOfTarGz(InputStream inputStream, String serviceType) throws Exception{
		Map<String, byte[]> answer = new HashMap<String, byte[]>();
		GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
		//中间以tar文件输出
		String dirPath = Constant.prop_env.getProperty("goalPath") + "other/";
		/* 判断输出目录是否存在，若不存在则创建 */
		File dirFile = new File(dirPath);
		if(!dirFile.exists()) {
			dirFile.mkdirs();
		}
		StringBuffer outputPath = new StringBuffer(dirPath);
		outputPath.append(serviceType).append("-").append("config.tar");
		File outputFile = new File(outputPath.toString());
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		IOUtils.copy(gzipInputStream, outputStream);
		gzipInputStream.close();
		outputStream.close();
		
		//读取输出的tar文件
		InputStream input = new FileInputStream(outputFile);
		TarArchiveInputStream tarInputStream = (TarArchiveInputStream)new ArchiveStreamFactory().createArchiveInputStream("tar", input);
		TarArchiveEntry entry = null;
		while((entry = (TarArchiveEntry)tarInputStream.getNextEntry()) != null) {
			if(!entry.isDirectory()) {
				byte[] buffer = new byte[(int)entry.getSize()];
				tarInputStream.read(buffer);
				answer.put(entry.getName(), buffer);
			}
		}
		input.close();
		return answer;
	}
	
	/**
	 * 将json字符串转换为map类型返回
	 * @param jsonString 要进行转换的json字符串
	 * @return 转换后返回的map类型参数
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> changeJsonToMap(String jsonString) throws Exception {
		Map<String, Object> answer = new HashMap<String, Object>();
		JSONObject jsonObject = JSONObject.fromObject(jsonString);
		
		for(Iterator<String> keys = jsonObject.keys(); keys.hasNext(); ) {
			String key = keys.next();
			Object value = jsonObject.get(key);
			if(value.getClass().equals(JSONObject.class)) {
				String json = value.toString();
				answer.put(key, changeJsonToMap(json));
			}else if(value.getClass().equals(JSONArray.class)) {
				List<Object> list = new ArrayList<Object>();
				JSONArray array = JSONArray.fromObject(value.toString());
				int length = array.size();
				for(int i = 0; i < length; i++) {
					Object item = array.get(i);
					if(item.getClass().equals(JSONObject.class)) {
						String json = item.toString();
						list.add(changeJsonToMap(json));
					}else {
						list.add(item);
					}
				}
				answer.put(key, list);
			}else {
				answer.put(key, value);
			}
		}
		return answer;
	}
	
	/**
	 * 将map类型参数转换成json字符串
	 * @param param 要进行转换的map类型参数
	 * @return 转换后的json字符串
	 */
	public static String changeMapToString(Map<String, Object> param) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.putAll(param);
		return jsonObject.toString();
	}
	
	/**
	 * 将inputStream转换为String
	 * @param input 需要进行转换的inputStream
	 * @result 转换的结果
	 */
	public static String readInputStream(InputStream input) throws Exception {
		byte[] buffer = new byte[1024];
		StringBuffer answer = new StringBuffer();
		int len = -1;
		while(true) {
			len = input.read(buffer);
			if(len == -1) break;
			String str = new String(buffer, 0, len);
			answer.append(str);
		}
		return answer.toString();
	}

	public static String getFileName(String path) {
		if(path == null) {
			logger.error("path is null");
			return null;
		}
		String[] items = path.split("/");
		return items[items.length - 1];
	}
	
	public static String getCmdOfSecurity(String command, String security) {
		if(security.equals("simple") || security.equals("ldap")) {
			return "sudo -u hdfs " + command;
		}else {
			String fileName = UtilTool.getFileName(Constant.hdfsKey);
			return "kinit -kt " + Constant.scriptPath + fileName + " hdfs;" + command;
		}
	}
	
	/**
	 * 使用给定的字符将原始字符串填充至指定长度
	 * @param oldString 需要进行填充的原始字符串
	 * @param goalLength 需要填充至的长度
	 * @param ch 用来填充的字符
	 * @return 填充后的字符串
	 */
	public static String paddingString(String oldString, int goalLength, char ch) {
		int length = goalLength - oldString.getBytes().length;
		if(length <= 0) return oldString;
		StringBuffer newString = new StringBuffer(oldString);
		for(int i = 0; i < length; i++) {
			newString.append(ch);
		}
		return newString.toString();
	}
	
	/**
	 * 在oldString字符串的每行前添加value字符串
	 * @param oldString 要进行处理的字符串
	 * @param value 添加在每行开头的字符串
	 * @return 处理结果
	 */
	public static String retract(String oldString, String value) {
		StringBuffer result = new StringBuffer();
		String[] lines = oldString.split("\n");
		for(String line : lines) {
			result.append(value).append(line).append("\n");
		}
		return result.toString();
	}
	
	/**
	 * 将指定的文件夹压缩成tar.gz格式的压缩文件，输出路径为与要压缩文件夹所在的目录下
	 * @param filePath 要进行压缩的文件夹路径
	 * @throws Exception
	 */
	public static void compressTarGz(String filePath) throws Exception {
		/* 获取压缩文件的路径和文件名称的分割点 */
		filePath = filePath.substring(0, filePath.length() - 1);
		int dirIndex = filePath.lastIndexOf("/");
		
		/* 将文件夹下的所有文件打包为一个tar包 */
		FileOutputStream tarOutput = new FileOutputStream(filePath + ".tar");
		TarArchiveOutputStream tarArchive = new TarArchiveOutputStream(tarOutput);
		File sourceFile = new File(filePath);
		Queue<File> queue = new LinkedList<File>();
		queue.offer(sourceFile);
		while(!queue.isEmpty()) {
			File file = queue.poll();
			if(file.isDirectory()) {
				File[] children = file.listFiles();
				for(File child : children) {
					queue.add(child);
				}
			}else {
				TarArchiveEntry entry = new TarArchiveEntry(file);
				/* 设置tar包中文件的路径 */
				String path = file.getAbsolutePath().substring(dirIndex);
				entry.setName(path);
				tarArchive.putArchiveEntry(entry);
				IOUtils.copy(new FileInputStream(file), tarArchive);
				tarArchive.closeArchiveEntry();
			}
		}
		if(tarArchive != null) {
			tarArchive.flush();
			tarArchive.close();
		}
		logger.info("compress tar success");
		
		/* 将tar包压缩为tar.gz */
		FileInputStream input = new FileInputStream(filePath + ".tar");
		FileOutputStream goalFile = new FileOutputStream(filePath + ".tar.gz");
		GZIPOutputStream gzipOutput = new GZIPOutputStream(goalFile);
		IOUtils.copy(input, gzipOutput);
		input.close();
		gzipOutput.flush();
		gzipOutput.close();
		logger.info("compress tar.gz success");
		
		/* 删除tar包 */
		File tarFile = new File(filePath + ".tar");
		tarFile.delete();
	}
	
	/**
	 * 删除指定文件夹或文件
	 * @param path 要删除的文件或文件夹路径
	 */
	public static void deleteFile(String path) {
		Queue<String> queue = new LinkedList<String>();
		queue.offer(path);
		while(!queue.isEmpty()) {
			String filePath = queue.poll();
			File file = new File(filePath);
			if(file.isFile()) {
				if(!file.delete()) logger.info("delete file error, file path is : " + filePath);
			}else if(file.isDirectory()) {
				File[] files = file.listFiles();
				if(files.length == 0) {
					boolean del = file.delete();
					if(!del) logger.info("delete file error, file path is : " + filePath);
					continue;
				}
				for(File temp : files) {
					queue.offer(temp.getAbsolutePath());
				}
				queue.offer(file.getAbsolutePath());
			}
		}
	}
}
