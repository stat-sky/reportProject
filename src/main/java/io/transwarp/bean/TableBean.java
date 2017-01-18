package io.transwarp.bean;

import io.transwarp.util.Constant;

public class TableBean {

	private String table_name;		//表名
	private String database_name;	//所属数据库名
	private String table_type;		//内表(MANAGED_TABLE)还是外表(EXTERNAL_TABLE)
	private String transactional;	//是否为事物表
	private String table_format;	//表的存储文件格式
	private String table_location;	//表的物理存放位置
	private String owner_name;		//表的拥有者
	private long maxDir;			//最大文件夹大小
	private long minDir;			//最小文件夹大小
	private double sumDir;			//总文件夹大小
	private int countDir;			//最下层文件夹个数
	private long maxFile;			//最大文件大小
	private long minFile;			//最小文件大小
	private double sumFile;			//文件总大小
	private int countFile;			//文件总数
	
	public TableBean() {
		super();
		this.maxDir = 0;
		this.minDir = 0xffffff;
		this.sumDir = 0;
		this.countDir = 0;
		this.maxFile = 0;
		this.minFile = 0xffffff;
		this.sumFile = 0;
		this.countFile = 0;
	}

	public String getTable_name() {
		return table_name;
	}
	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}
	public String getDatabase_name() {
		return database_name;
	}
	public void setDatabase_name(String database_name) {
		this.database_name = database_name;
	}
	public String getTable_type() {
		return table_type;
	}
	public void setTable_type(String table_type) {
		this.table_type = table_type;
	}
	public String getTransactional() {
		return transactional;
	}
	public void setTransactional(String transactional) {
		this.transactional = transactional;
	}
	public String getTable_format() {
		return table_format;
	}
	public void setTable_format(String table_format) {
		this.table_format = table_format;
	}
	public String getTable_location() {
		if(table_location == null) return null;
		int subIndex = table_location.indexOf(Constant.locationSub);
		if(subIndex != -1) {
			return table_location.substring(subIndex + Constant.locationSub.length());
		}else {
			return table_location;
		}
	}
	public void setTable_location(String table_location) {
		this.table_location = table_location;
	}
	public String getOwner_name() {
		return owner_name;
	}
	public void setOwner_name(String owner_name) {
		this.owner_name = owner_name;
	}
	public long getMaxDir() {
		return maxDir;
	}
	public long getMinDir() {
		if(minDir == 0xffffff) return 0;
		return minDir;
	}
	public double getSumDir() {
		return sumDir;
	}
	public int getCountDir() {
		return countDir;
	}
	public void addDir(long sizeDir) {
		this.sumDir += sizeDir;
		this.maxDir = maxDir > sizeDir ? maxDir : sizeDir;
		this.minDir = minDir < sizeDir ? minDir : sizeDir;
		this.countDir += 1;
	}
	public String getAvgDir() {
		double answer = 0;
		if(this.countDir != 0) {
			answer = this.sumDir / this.countDir;
		}
		return Constant.decimalFormat.format(answer);
	}
	public long getMaxFile() {
		return maxFile;
	}
	public long getMinFile() {
		if(minFile == 0xffffff) return 0;
		return minFile;
	}
	public double getSumFile() {
		return sumFile;
	}
	public int getCountFile() {
		return countFile;
	}
	public void addFile(long sizeFile) {
		this.sumFile += sizeFile;
		this.maxFile = maxFile > sizeFile ? maxFile : sizeFile;
		this.minFile = minFile < sizeFile ? minFile : sizeFile;
		this.countFile += 1;
	}
	public String getAvgFile() {
		double answer = 0;
		if(this.countFile != 0) {
			answer = this.sumFile / this.countFile;
		}
		return Constant.decimalFormat.format(answer);
	}

	public String checkTableType() {
		String tableType;
		String formatType;
		String transaction;
		/* 记录是否为外表 */
		if(table_type.equals("EXTERNAL_TABLE")) {
			tableType = "外表";
		}else {
			tableType = "表";
		}
		/* 记录表类型 */
		if(table_format.startsWith("Text") || table_format.startsWith("text")) {
			formatType = "text";
		}else if(table_format.startsWith("Orc") || table_format.startsWith("orc")) {
			formatType = "orc";
		}else if(table_format.startsWith("Hyerbase") || table_format.startsWith("hyperbase")) {
			formatType = "hyperbase";
		}else if(table_format.indexOf("memory") != -1) {
			formatType = "holodesk";
		}else {
			String[] items = table_format.split("\\.");
			formatType = items[items.length - 1];
		}
		/* 记录是否为事物表 */
		if(transactional != null && transactional.equals("true")) {
			transaction = "事务";
		}else {
			transaction = "";
		}
		return formatType + transaction + tableType;
		
	}
}
