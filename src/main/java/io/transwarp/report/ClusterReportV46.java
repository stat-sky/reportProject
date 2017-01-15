package io.transwarp.report;

import io.transwarp.bean.NodeBean;
import io.transwarp.bean.ServiceBean;
import io.transwarp.bean.TableBean;
import io.transwarp.template.ClusterReportTemplate;
import io.transwarp.util.Constant;
import io.transwarp.util.PrintToTableUtil;
import io.transwarp.util.UtilTool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Element;

public class ClusterReportV46 extends ClusterReportTemplate{

	private static Logger logger = Logger.getLogger(ClusterReportV46.class);

	private Map<String, String> hdfsChecks;
	private Map<String, Map<String, String>> processChecks;
	private Vector<TableBean> tables;
	
	public ClusterReportV46(String tdh_version, Map<String, NodeBean> nodes, Map<String, ServiceBean> services, 
			Map<String, Map<String, String>> processChecks, Map<String, String> hdfsChecks, Vector<TableBean> tables) {
		super(tdh_version, nodes, services);
		this.hdfsChecks = hdfsChecks;
		this.processChecks = processChecks;
		this.tables = tables;
	}
	

	@Override
	public String getHDFSInfo() {
		/* 存放结果 */
		StringBuffer answer = new StringBuffer("HDFS数据检测：\n");
		/* 用于生成表格 */
		List<String[]> maps = new ArrayList<String[]>();
		List<Element> configs = Constant.prop_cluster.getAll();
		for(Element config : configs) {
			String name = config.elementText("name");
			answer.append("  ").append(name).append(":\n");
			String result = this.hdfsChecks.get(name);
			String[] lines = result.split("\n");
			for(String line : lines) {
				if(line.startsWith("/") || line.startsWith(".")) continue;
				line = line.trim();
				if(line.equals("")) {
					if(maps.size() > 0) {
						try {
							answer.append(PrintToTableUtil.printToTable(maps, 50));
						}catch(Exception e) {
							logger.error("build table is error, error message is " + e.getMessage());
						}
						answer.append("\n");
						maps.clear();
					}
				}else {
					int index = line.indexOf(":");
					if(index != -1) {
						String key = line.substring(0, index);
						String value = line.substring(index + 1);
						maps.add(new String[]{key, value.trim()});
					}
				}
			}
			if(maps.size() > 0) {
				try {
					answer.append(PrintToTableUtil.printToTable(maps, 50));
				}catch(Exception e) {
					logger.error("build table error, error message is " + e.getMessage());
				}
				answer.append("\n");
			}
			maps.clear();
		}
		return answer.toString();
	}
	
	@Override
	public String getTableInfo() {
		List<String[]> maps = new ArrayList<String[]>();
		maps.add(new String[]{"数据库名", "所有者", "表名", "表类型", "文件夹：最大/最小/总数/平均(b)", "文件：最大/最小/总数/平均(b)"});
		for(TableBean table : tables) {
			String[] line = new String[6];
			line[0] = table.getDatabase_name();
			line[1] = table.getOwner_name();
			line[2] = table.getTable_name();
			line[3] = table.checkTableType();
			if(table.getCountDir() == 0) {
				line[4] = "无";
			}else {
				line[4] = table.getMaxDir() + "/" + table.getMinDir() + "/" + table.getCountDir() + "/" + table.getAvgDir();
			}
			if(table.getCountFile() == 0) {
				line[5] = "无";
			}else {
				line[5] = table.getMaxFile() + "/" + table.getMinFile() + "/" + table.getCountFile() + "/" + table.getAvgFile();
			}
			maps.add(line);
		}
		StringBuffer answer = new StringBuffer("数据表检测:\n");
		answer.append("  ").append("tables count : ").append(tables.size()).append("\n");
		try {
			answer.append(PrintToTableUtil.printToTable(maps, 30));
		}catch(Exception e) {
			logger.error("build table of table info error, error message is " + e.getMessage());
		}
		answer.append("\n\n");
		return answer.toString();
	}
	
	@Override
	public String getProcessInfo() {
		StringBuffer answer = new StringBuffer("进程检测:\n");
		for(Iterator<String> keys = this.processChecks.keySet().iterator(); keys.hasNext(); ) {
			/* 获取在information中存放结果的key,为"服务名：服务角色" */
			String key = keys.next();
			Map<String, String> processCheck = this.processChecks.get(key);
			/* 获取服务角色 */
			String[] topics = key.split(":");
			String serviceRoleType = topics[1];
			/* 获取该服务角色的配置 */
			Element config = Constant.prop_process.getElement("serviceRoleType", serviceRoleType);
			if(config == null) {
				logger.error("this config of process is not found");
				continue;
			}
			/* 获取需要解析的配置项 */
			Element properties = config.element("properties");
			@SuppressWarnings("unchecked")
			List<Element> props = properties.elements();
			for(Element prop : props) {
				/* 获取配置项名称，为jinfo、jmap等 */
				String name = prop.elementText("name");
				/* 获取检测结果 */
				String result = processCheck.get(name);
				/* 标题 */
				answer.append("  服务:").append(topics[0]).append(",角色:").append(topics[1]).append(",检测项:").append(name).append(":\n");
				/* 获取过滤项 */
				String splitKey = prop.elementText("key");
				if(splitKey == null || splitKey.equals("")) {
					/* 若过滤项为空，则将全部按表格输出 */
					String[] lines = result.split("\n");
					if(lines.length == 1) {
						answer.append("    ").append(result).append("\n\n");
						continue;
					}
					List<String[]> maps = new ArrayList<String[]>();
					for(String line : lines) {
						line = line.trim();
						if(line.equals("")) continue;
						String[] lineItems = line.split("\\s+");
						if(lineItems.length < 2) continue;
						maps.add(lineItems);
					}
					if(maps.size() > 0) {
						int width = 160 / maps.get(0).length;
						try {
							answer.append(PrintToTableUtil.printToTable(maps, width)).append("\n\n");
						} catch (Exception e) {
							logger.error("build table error, error message is " + e.getMessage());
						}
					}
					
				}else {
					/* 否则，对结果进行分割 */
					String buffer = this.analysis(result, splitKey);
					answer.append(UtilTool.retract(buffer, "    ")).append("\n\n");
				}
			}
		}
		return answer.toString();
	}

	public String analysis(String result, String key) {
		StringBuffer answer = new StringBuffer();
		String[] lines = result.split("\n");
		String[] items = key.split(";");
		for(String line : lines) {
			/* 取一行开头作为行关键字用于查询 */
			int equalsIndex = line.indexOf("=");
			if(equalsIndex == -1) {
				equalsIndex = line.length();
			}
			String lineKey = line.substring(0, equalsIndex);
			for(String item : items) {
				int index = item.indexOf("(");
				if(index == -1) {
					/* 没有要选取的子项，则直接取关键字对该行进行判断 */
					if(lineKey.indexOf(item) != -1) {
						answer.append(line).append("\n");
					}
				}else {
					/* 有需要选取的子项，所以先对关键字解析 */
					String itemKey = item.substring(0, index);
					if(lineKey.indexOf(itemKey) != -1) {
						answer.append(itemKey).append(":");
						/* 去掉括号并按照逗号分割 */
						String[] props = item.substring(index, item.length() - 1).split(",");
						String[] lineItems = line.split(" ");
						/* 遍历所有子项，判断是否需要 */
						for(String lineItem : lineItems) {
							for(String prop : props) {
								if(lineItem.indexOf(prop) != -1) {
									answer.append(lineItem).append(" ");
								}
							}
						}
					}
				}
				
			}
		}
		return answer.toString();
	}
}
