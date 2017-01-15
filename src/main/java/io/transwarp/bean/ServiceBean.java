package io.transwarp.bean;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

public class ServiceBean {

	private String serviceId; 				//服务编号
	private String serviceName; 			//服务名称
	private String dependencies; 			//服务依赖的服务编号
	private String status; 					//服务状态
	private String installed; 				//服务是否安装
	private String health;					//服务健康状态
	private String configStatus;			//配置状况
	private String enableKerberos;			//是否开启kerberos
	private String type;					//服务类型
	private String clusterId;				//集群编号
	private String clusterName;				//集群名称
	private List<RoleBean> roles;			//属于该服务的角色
	
	public ServiceBean() {
		roles = new ArrayList<RoleBean>();
	}
	
	public ServiceBean(JSONObject json) {
		this();
		this.setServiceId(json.get("id"));
		this.setServiceName(json.get("name"));
		this.setDependencies(json.get("dependencies"));
		this.setStatus(json.get("status"));
		this.setInstalled(json.get("installed"));
		this.setHealth(json.get("health"));
		this.setConfigStatus(json.get("configStatus"));
		this.setEnableKerberos(json.get("enableKerberos"));
		this.setType(json.get("type"));
		this.setClusterId(json.get("clusterId"));
		this.setClusterName(json.get("clusterName"));
	}
	
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(Object serviceId) {
		if(serviceId == null) return;
		this.serviceId = serviceId.toString();
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(Object serviceName) {
		if(serviceName == null) return;
		this.serviceName = serviceName.toString();
	}
	public String getDependencies() {
		return dependencies;
	}
	public void setDependencies(Object dependencies) {
		if(dependencies == null) return;
		this.dependencies = dependencies.toString();
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(Object status) {
		if(status == null) return;
		this.status = status.toString();
	}
	public String getInstalled() {
		return installed;
	}
	public void setInstalled(Object installed) {
		if(installed == null) return;
		this.installed = installed.toString();
	}
	public String getHealth() {
		return health;
	}
	public void setHealth(Object health) {
		if(health == null) return ;
		this.health = health.toString();
	}
	public String getConfigStatus() {
		return configStatus;
	}
	public void setConfigStatus(Object configStatus) {
		if(configStatus == null) return;
		this.configStatus = configStatus.toString();
	}
	public String getEnableKerberos() {
		return enableKerberos;
	}
	public void setEnableKerberos(Object enableKerberos) {
		if(enableKerberos == null) return;
		this.enableKerberos = enableKerberos.toString();
	}
	public String getType() {
		return type;
	}
	public void setType(Object type) {
		if(type == null) return;
		this.type = type.toString();
	}
	public String getClusterId() {
		return clusterId;
	}
	public void setClusterId(Object clusterId) {
		if(clusterId == null) return;
		this.clusterId = clusterId.toString();
	}
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(Object clusterName) {
		if(clusterName == null) return;
		this.clusterName = clusterName.toString();
	}
	public List<RoleBean> getRoles() {
		return roles;
	}
	public void addRole(RoleBean role) {
		this.roles.add(role);
	}
}
