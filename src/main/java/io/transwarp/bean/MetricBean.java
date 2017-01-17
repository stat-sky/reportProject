package io.transwarp.bean;

import java.util.ArrayList;
import java.util.List;

public class MetricBean {

	private String metricName;
	private String unit;
	private List<String> values;
	
	public MetricBean() {
		super();
		values = new ArrayList<String>();
	}

	public String getMetricName() {
		return metricName;
	}

	public void setMetricName(Object metricName) {
		if(metricName == null) {
			metricName = "";
			return;
		}
		this.metricName = metricName.toString();
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(Object unit) {
		if(unit == null) {
			unit = "";
			return;
		}
		String temp = unit.toString();
		if(temp.equalsIgnoreCase("PERCENT")) {
			this.unit = "%";
		}else if(temp.equalsIgnoreCase("MB_PER_SECOND")) {
			this.unit = "MB/s";
		}else {
			this.unit = temp;
		}
	}

	public List<String> getValues() {
		return values;
	}

	public void addValue(String value) {
		this.values.add(value);
	}
	
	
}
