package io.transwarp.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import io.transwarp.bean.TableBean;
import io.transwarp.conn.JDBCUtil;
import io.transwarp.template.DataDictionaryTemplate;
import io.transwarp.util.Constant;

public class DataDictionaryMysql extends DataDictionaryTemplate {

	private static Logger logger = Logger.getLogger(DataDictionaryMysql.class);
	
	public DataDictionaryMysql(String security, String hdfsConfPath ,String namenodeIP) {
		super(security, hdfsConfPath, namenodeIP);
	}
	
	@Override
	public void getTableInfo() {
		/* 构建url */
		String jdbcIP = Constant.prop_env.getProperty("jdbcIP");
		String port = Constant.prop_env.getProperty("port");
		String database = Constant.prop_env.getProperty("database");
		if(database == null) database = "metastore_inceptorsl1";
		StringBuffer url = new StringBuffer("jdbc:mysql://");
		url.append(jdbcIP).append(":").append(port).append("/").append(database);
		/* 获取连接 */
		String jdbcUser = Constant.prop_env.getProperty("jdbcUser");
		String jdbcPwd = Constant.prop_env.getProperty("jdbcPwd");
		Connection conn = null;
		try {
			conn = JDBCUtil.getConnection(url.toString(), jdbcUser, jdbcPwd);
		}catch(Exception e) {
			logger.error("get connection error, error message is " + e.getMessage());
		}
		if(conn == null) {
			logger.error("get connection faild");
			return;
		}
		
		/*查询结果*/
		try {
			PreparedStatement pstat = conn.prepareStatement(Constant.QUERY_TABLE_MYSQL);
			ResultSet rs = pstat.executeQuery();
			while(rs.next()) {
				TableBean table = new TableBean();
				/* 获取表名称信息 */
				String tableName = rs.getString("table_name");
				table.setTable_name(tableName);
				/* 获取表空间路径 */
				String location = rs.getString("table_location");
				if(location == null || location.equals("null")) {
					logger.info("table : " + tableName + " has no path");
					continue;
				}
				table.setTable_location(location);
				/* 获取所在数据库信息 */
				String database_name = rs.getString("database_name"); 
				table.setDatabase_name(database_name);
				/* 获取所有者信息 */
				String owner = rs.getString("owner");
				table.setOwner_name(owner);
				/* 获取表类型 */
				String type = rs.getString("table_type");
				table.setTable_type(type);
				/* 获取表存储结构 */
				String format = rs.getString("table_format");
				String[] items = format.split("\\.");
				int index = items.length - 1;
				if(index < 0) table.setTable_format(format);
				else table.setTable_format(items[index]);
				/* 获取是否为事物表信息 */
				String transactional = rs.getString("transactional");
				if(transactional == null || transactional.equals("null")) {
					table.setTransactional("false");
				}else {
					table.setTransactional("true");
				}
				
				Information.tables.add(table);
			}
		} catch(Exception e) {
			logger.error("get table info error, error message is " + e.getMessage());
		} finally {
			if(conn != null) {
				try {
					conn.close();
				}catch(Exception e) {
					logger.error("close connection error, error message is " + e.getMessage());
				}
			}
		}
	}

}
