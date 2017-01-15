package io.transwarp.bean;

import net.sf.json.JSONObject;

public class RoleBean {

	private String roleId;			//角色编号
	private String name;			//角色名称
	private String roleType;		//角色类型
	private String status;			//角色状态
	private String health;			//健康状态
	private NodeBean node;			//所在节点
	private ServiceBean service;	//所属服务
	
	public RoleBean() {}
	
	public RoleBean(JSONObject json) {
		this.setRoleId(json.get("id"));
		this.setRoleType(json.get("roleType"));
		this.setName(json.get("name"));
		this.setStatus(json.get("status"));
		this.setHealth(json.get("health"));
		try {
			JSONObject nodeJson = json.getJSONObject("node");
			NodeBean node = new NodeBean(nodeJson);
			this.setNode(node);
		}catch(Exception e) {}
		try {
			JSONObject serviceJson = json.getJSONObject("service");
			ServiceBean service = new ServiceBean(serviceJson);
			this.setService(service);
		}catch(Exception e) {}
	}
	
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(Object roleId) {
		if(roleId == null) return;
		this.roleId = roleId.toString();
	}
	public String getName() {
		return name;
	}
	public void setName(Object name) {
		if(name == null) return;
		this.name = name.toString();
	}
	public String getRoleType() {
		return roleType;
	}
	public void setRoleType(Object roleType) {
		if(roleType == null) return;
		this.roleType = roleType.toString();
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(Object status) {
		if(status == null) return;
		this.status = status.toString();
	}
	public String getHealth() {
		return health;
	}
	public void setHealth(Object health) {
		if(health == null) return;
		this.health = health.toString();
	}
	public NodeBean getNode() {
		return node;
	}
	public void setNode(NodeBean node) {
		this.node = node;
	}
	public ServiceBean getService() {
		return service;
	}
	public void setService(ServiceBean service) {
		this.service = service;
	}
}
