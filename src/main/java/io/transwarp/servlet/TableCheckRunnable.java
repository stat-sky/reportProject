package io.transwarp.servlet;

import java.util.LinkedList;
import java.util.Queue;

import io.transwarp.bean.TableBean;
import io.transwarp.conn.ShellUtil;
import io.transwarp.util.UtilTool;

import org.apache.log4j.Logger;

public class TableCheckRunnable implements Runnable {

	private static Logger logger = Logger.getLogger(TableCheckRunnable.class);
	
	private TableBean table;
	private String security;
	private String ipAddress;
	private String nodeUser;
	
	public TableCheckRunnable(TableBean table, String security, String ipAddress, String nodeUser) {
		this.table = table;
		this.security = security;
		this.ipAddress = ipAddress;
		this.nodeUser = nodeUser;
	}
	
	@Override
	public void run() {
		/* 获取表空间的路径 */
		String dataPath = table.getTable_location();
		/* 使用队列对表空间进行逐层搜索，直至找到数据文件 */
		Queue<String> queue = new LinkedList<String>();
		queue.offer(dataPath);
		while(!queue.isEmpty()) {
			String path = queue.poll();
			/* 记录文件夹大小 */
			long sizeDir = 0;
			/* 构建查询命令 */
			String command = "hdfs dfs -ls " + path;
			command = UtilTool.getCmdOfSecurity(command, security);
			/* 执行命令获取结果 */
			String result = null;
			try {
				result = ShellUtil.executeDist(command, nodeUser, ipAddress);
			}catch(Exception e) {
				logger.error("check table location error, error path is " + path + ", error message is " + e.getMessage());
			}
			if(result == null) continue;
			/* 分析处理查询结果，若为文件夹则加入队列，若为文件则记录大小 */
			String[] lines = result.split("\n");
			for(String line : lines) {
				/* 将每行数据按中间空白切分 */
				String[] items = line.replaceAll("\\s+", ",").split(",");
				/* 若该行不为文件或文件夹信息，则跳过 */
				if(items.length < 8 || !items[0].matches("[-dwxr]+")) continue; 
				/* 根据第一列中信息判断是否为文件夹，第八列为路径 */
				if(items[0].indexOf("d") != -1) {
					queue.offer(items[7]);
				}else {
					/* 为文件，记录大小，并将大小加到所在文件夹的大小 */
					long sizeFile = Long.valueOf(items[4]);
					sizeDir += sizeFile;
					table.addFile(sizeFile);
				}
			}
			/* 以文件夹大小是否为0判断该文件夹是否为最底层的文件夹，且需要排除初始路径 */
			if(sizeDir > 0 && !path.equals(dataPath)) {
				table.addDir(sizeDir);
			}
		}
		/* 检测完成，计数器加1 */
		Information.successTask.incrementAndGet();
	}
}
