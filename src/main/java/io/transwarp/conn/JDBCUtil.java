package io.transwarp.conn;

import io.transwarp.util.Constant;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.Logger;

public class JDBCUtil {

	private static Logger logger = Logger.getLogger(JDBCUtil.class);
	
	public static Connection getConnection(String url) {
		return getConnection(url, null, null);
	}
	
	public static Connection getConnection(String url, String jdbcUser, String jdbcPwd) {
		logger.info("url is " + url);
		Connection conn = null;
		String className = Constant.prop_env.getProperty("className");
		/* 加载className */
		try {
			Class.forName(className);
		} catch (Exception e) {
			logger.error("load classname error, className is " + className + ", error message is " + e.getMessage());
		}
		/* 获取sql连接 */
		try {
			if(jdbcUser == null) {
				conn = DriverManager.getConnection(url);
			}else {
				conn = DriverManager.getConnection(url, jdbcUser, jdbcPwd);
			}
		} catch (Exception e) {
			logger.error("get sql connection error, error message is " + e.getMessage());
		}
		return conn;
	}
	
/*	public static void main(String[] args) {
		PropertyConfigurator.configure("config/log4j.properties");
		String url = "jdbc:mysql://172.16.2.64:3306/metastore_inceptorsql1";
		Connection conn = JDBCUtil.getConnection(url, "xhy", "123");
		String sql = "SELECT A.database_name, A.owner, A.table_name, A.table_location, A.table_type, A.table_format, B.PARAM_VALUE as transactional FROM (SELECT TBLS.TBL_ID AS id, DBS.NAME AS database_name, TBLS.OWNER AS owner, TBLS.TBL_NAME AS table_name, SDS.LOCATION AS table_location, TBLS.TBL_TYPE AS table_type, SDS.INPUT_FORMAT AS table_format FROM DBS, SDS, TBLS WHERE DBS.DB_ID = TBLS.DB_ID AND SDS.SD_ID = TBLS.SD_ID) A LEFT JOIN (SELECT TBL_ID, PARAM_VALUE FROM TABLE_PARAMS WHERE PARAM_KEY = \"transactional\") B ON A.id = B.TBL_ID;";
		try {
			PreparedStatement pstat = conn.prepareStatement(sql);
			ResultSet rs = pstat.executeQuery();
			while(rs.next()) {
				String tableName = rs.getString("table_name");
				System.out.println(tableName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}*/
}
