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
//			e.printStackTrace();
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
		String url = "jdbc:hive2://172.16.1.111:10000/system;principal=hive/tw-node111@TDH;authentication=kerberos;kuser=hive;keytab=/home/xhy/temp/hive.keytab;krb5conf=/etc/krb5.conf";
		Connection conn = JDBCUtil.getConnection(url);
		String sql = "select table_name from tables_v";
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
