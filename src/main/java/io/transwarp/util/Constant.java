package io.transwarp.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class Constant {
	
	/* 常量部分设置 */
	/** 用于去掉数据表存放路径中不需要的部分 */
	public static String locationSub = "hdfs://nameservice1";
	/** 日期格式设置 */
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	/** 浮点数小数位设置 */
	public static DecimalFormat decimalFormat = new DecimalFormat("#0.00");
	/** 编码格式 */
	public static final String ENCODING = "UTF-8";
	/** 环境变量配置文件路径 */
	public static final String ENV_PATH = "config/env.properties";
	/** 日志检测脚本在本地的路径 */
	public static final String[] LOGCHECKPAHT = new String[]{"script/logCheck.jar", "config/logCheck.xml"};
	/** 生成hdfs.keytab的shell命令 */
	public static final String BUILD_KEYTAB = "kadmin.local -q \"xst -norandkey -k hdfs.keytab hdfs\"";
	/** 数据字典查询语句 */
	public static final String QUERY_TABLE_INCEPTOR = "select database_name, table_name, table_type, transactional, table_format, table_location, owner_name from tables_v;";
	/** mysql数据库查询元数据 */
	public static final String QUERY_TABLE_MYSQL = "SELECT A.database_name, A.owner, A.table_name, A.table_location, A.table_type, A.table_format, B.PARAM_VALUE as transactional FROM (SELECT TBLS.TBL_ID AS id, DBS.NAME AS database_name, TBLS.OWNER AS owner, TBLS.TBL_NAME AS table_name, SDS.LOCATION AS table_location, TBLS.TBL_TYPE AS table_type, SDS.INPUT_FORMAT AS table_format FROM DBS, SDS, TBLS WHERE DBS.DB_ID = TBLS.DB_ID AND SDS.SD_ID = TBLS.SD_ID) A LEFT JOIN (SELECT TBL_ID, PARAM_VALUE FROM TABLE_PARAMS WHERE PARAM_KEY = \"transactional\") B ON A.id = B.TBL_ID;";
	/** 远程执行shell */
	public static String distCmd;
	/** 远程复制文件与文件夹 */
	public static String distScp;
	/** hdfs用户的keytab密钥路径 */
	public static String hdfsKey;
	/** 检测脚本在各个节点的存放路径 */
	public static String scriptPath;
	
	
	/* rest api查询项的中文名 */
	public static final String USER_LOGIN = "用户登录";
	public static final String USER_LOGOUT = "用户登出";
	public static final String FIND_SERVICE = "查询服务";
	public static final String FIND_MORE_SERVICE = "查询多个服务";
	public static final String FIND_MORE_SERVICE_ROLE = "查询多个服务角色";
	public static final String FIND_NODE = "查询节点";
	public static final String FIND_MORE_NODE = "查询多个节点";
	public static final String DOWNLOAD_CONFIG = "下载服务配置";
	public static final String DOWNLOAD_KEYTAB = "下载用户keytab";	
	public static final String NODE_METRIC = "节点指标查询";
	
	/* 配置文件变量 */
	/** 环境变量配置 */
	public static Properties prop_env = new Properties();
	/** rest api读取配置 */
	public static ConfigRead prop_restapi = null;
	/** 日志检测配置 */
	public static ConfigRead prop_logCheck = null;
	/** 进程检测配置 */
	public static ConfigRead prop_process = null;
	/** 端口检测配置 */
	public static ConfigRead prop_portCheck = null;
	/** 节点检测配置 */
	public static ConfigRead prop_nodeCheck = null;
	/** 服务配置检测配置 */
	public static ConfigRead prop_config = null;
	/** 集群检测配置 */
	public static ConfigRead prop_cluster = null;
	/** 节点负载指标配置 */
	public static ConfigRead prop_metric = null;
}
