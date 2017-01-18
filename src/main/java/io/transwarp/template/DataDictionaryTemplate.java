package io.transwarp.template;

import io.transwarp.servlet.Information;
import io.transwarp.servlet.TableCheckRunnable;

public abstract class DataDictionaryTemplate extends Information {
	
	protected String security;
	protected String hdfsConfPath;
	protected String namenodeIP;
	
	public DataDictionaryTemplate(String security, String hdfsConfPath, String namenodeIP) {
		this.security = security;
		this.hdfsConfPath = hdfsConfPath;
		this.namenodeIP = namenodeIP;
	}
	
	public void beginTableCheckRunnabl() {
		getTableInfo();
		putTableCheckRunnable();
	}
	
	public void putTableCheckRunnable() {
		int tableNum = Information.tables.size();
		/* 记录表空间个数 */
		Information.totalTask += tableNum;
		/* 对每个表空间建立一个线程查询 */
		for(int i = 0; i < tableNum; i++) {
			Information.threadPool.execute(new TableCheckRunnable(Information.tables.get(i), security, hdfsConfPath, namenodeIP));
		}
	}
	
	public abstract void getTableInfo();
}
