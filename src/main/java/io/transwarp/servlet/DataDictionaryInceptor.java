package io.transwarp.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import io.transwarp.bean.TableBean;
import io.transwarp.conn.JDBCUtil;
import io.transwarp.template.DataDictionaryTemplate;
import io.transwarp.util.Constant;

public class DataDictionaryInceptor extends DataDictionaryTemplate {

	private static Logger logger = Logger.getLogger(DataDictionaryInceptor.class);
	
	public DataDictionaryInceptor(String security, String hdfsConfPath, String namenodeIP) {
		super(security, hdfsConfPath, namenodeIP);
	}
	
	@Override
	public void getTableInfo() {
		/* 构建URL */
		String jdbcIP = Constant.prop_env.getProperty("jdbcIP");
		String port = Constant.prop_env.getProperty("port");
		StringBuffer url = new StringBuffer("jdbc:hive2://");
		url.append(jdbcIP).append(":").append(port).append("/system");
		/* 根据安全模式获取jdbc连接 */
		Connection conn = null;
		try {
			if(security.equals("simple")) {
				conn = JDBCUtil.getConnection(url.toString());
			}else if(security.equals("kerberos")) {
				/* 添加kerberos认证信息 */
				String principal = Constant.prop_env.getProperty("principal");
				String kuser = Constant.prop_env.getProperty("kuser");
				String keytab = Constant.prop_env.getProperty("keytab");
				String krb5conf = Constant.prop_env.getProperty("krb5conf");
				url.append(";").append("principal=").append(principal).append(";")
					.append("authentication=kerberos;").append("kuser=").append(kuser).append(";")
					.append("keytab=").append(keytab).append(";").append("krb5conf=").append(krb5conf);
				/* 获取连接 */
				conn = JDBCUtil.getConnection(url.toString());
			}else {
				String jdbcUser = Constant.prop_env.getProperty("jdbcUser");
				String jdbcPwd = Constant.prop_env.getProperty("jdbcPwd");
				conn = JDBCUtil.getConnection(url.toString(), jdbcUser, jdbcPwd);
			}
		}catch(Exception e) {
			logger.error("get connection error, error message is " + e.getMessage());
		}

		
		/* 查询表信息 */
		if(conn == null) {
			logger.error("get connection faild");
			return;
		}
		try {
			PreparedStatement pstat = conn.prepareStatement(Constant.QUERY_TABLE_INCEPTOR);
			ResultSet rs = pstat.executeQuery();
			while(rs.next()) {
				TableBean table = new TableBean();
				table.setDatabase_name(rs.getString("database_name"));
				table.setOwner_name(rs.getString("owner_name"));
				table.setTable_format(rs.getString("table_format"));
				table.setTable_location(rs.getString("table_location"));
				table.setTable_name(rs.getString("table_name"));
				table.setTable_type(rs.getString("table_type"));
				table.setTransactional(rs.getString("transactional"));
				Information.tables.add(table);
			}
		} catch (SQLException e) {
			logger.error("query table info error, error message is " + e.getMessage());
		} finally {
			if(conn != null) {
				try {
					conn.close();
				}catch(Exception e) {
					logger.error("close connection error");
				}
			}
		}
	}

}
