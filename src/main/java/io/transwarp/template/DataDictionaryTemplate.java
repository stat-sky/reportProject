package io.transwarp.template;

import java.util.List;

import io.transwarp.servlet.Information;
import io.transwarp.servlet.TableCheckRunnable;

public abstract class DataDictionaryTemplate extends Information {
	
	protected String security;
	protected List<String> ips;
	protected String nodeUser;
	
	public DataDictionaryTemplate(String security, List<String> ips, String nodeUser) {
		this.security = security;
		this.ips = ips;
		this.nodeUser = nodeUser;
	}
	
	public void beginTableCheckRunnabl() {
		getTableInfo();
		putTableCheckRunnable();
	}
	
	public void putTableCheckRunnable() {
		int num = ips.size();
		int tableNum = Information.tables.size();
		/* 记录表空间个数 */
		Information.totalTask += tableNum;
		/* 对每个表空间建立一个线程查询 */
		for(int i = 0; i < tableNum; i++) {
			Information.threadPool.execute(new TableCheckRunnable(Information.tables.get(i), security, ips.get(i%num), nodeUser));
		}
	}
	
	public abstract void getTableInfo();
}
