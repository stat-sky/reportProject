package io.transwarp.servlet;

import io.transwarp.bean.ConfigBean;
import io.transwarp.bean.NodeBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.bean.TableBean;

import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Information {

	/** 
	 * 存放rest api获取的节点信息
	 * 参数依次为 hostname，node java bean
	 */
	protected static Map<String, NodeBean> nodes = new ConcurrentHashMap<String, NodeBean>();
	/** 
	 * 存放rest api获取的服务信息
	 * 参数依次为 serviceName, service java bean
	 */
	protected static Map<String, ServiceBean> services = new ConcurrentHashMap<String, ServiceBean>();
	/** 
	 * 存放rest api获取的服务配置信息
	 * 三个参数依次为 hostname、serviceName, config javabean
	 */
	protected static Map<String, Map<String, ConfigBean>> configs = new ConcurrentHashMap<String, Map<String, ConfigBean>>();
	
	/**
	 * 存放节点检测结果数据
	 * 参数依次为 hostname, 检测项, 检测结果
	 */
	protected static Map<String, Map<String, String>> nodeChecks = new ConcurrentHashMap<String, Map<String, String>>();
	/**
	 * 存放端口检测结果
	 * 参数依次为 hostname, 端口号，连接数
	 */
	protected static Map<String, Map<String, String>> portChecks = new ConcurrentHashMap<String, Map<String, String>>();
	/**
	 * 存放进程检测结果
	 * 参数依次为 服务+服务角色, 检测项， 检测结果
	 */
	protected static Map<String, Map<String, String>> processChecks = new ConcurrentHashMap<String, Map<String, String>>();
	/**
	 * 存放集群检测结果
	 * 参数依次为 检测项，检测结果
	 */
	protected static Map<String, String> hdfsChecks = new ConcurrentHashMap<String, String>();

	/** 存放数据表信息 */
	protected static Vector<TableBean> tables = new Vector<TableBean>();
	/** 集群版本号 */
	protected static String tdh_version;
	
	/** 记录运行任务总数 */
	protected static int totalTask = 0;
	/** 记录完成任务数 */
	protected static AtomicInteger successTask = new AtomicInteger(0);
	/** 线程池 */
	protected static ExecutorService threadPool = null;
}
