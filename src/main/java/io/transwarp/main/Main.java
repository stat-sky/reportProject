package io.transwarp.main;

import java.util.Date;

import io.transwarp.report.Report;
import io.transwarp.util.Constant;

public class Main {

	
	public static void main(String[] args) {
		try {
			//获取当期日期
			Date date = new Date();
			String dateTime = Constant.dateFormat.format(date);
			int endIndex = dateTime.indexOf(" ");
			if(endIndex != -1) {
				dateTime = dateTime.substring(0, endIndex);
			}
			long start = System.currentTimeMillis();
			Report report = new Report();
			report.getReport(Constant.prop_env.getProperty("goalPath") + "REPORT-" + dateTime + ".txt");
//			report.getRoleMap();
			long end = System.currentTimeMillis();
			System.out.println((end - start) * 1.0 / 1000 + " s");
//			report.outputConfig();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
