package io.transwarp.bean;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NodeBean {

	private String nodeId;					//节点ID
	private String hostName;				//节点hostname
	private String ipAddress;				//节点IP
	private String clusterId;				//所属集群编号
	private String clusterName;				//所属集群名称
	private String sshConfigId;				//ssh配置编号
	private String rackId;					//所属机柜编号
	private String rackName;				//所属机柜名称
	private String isManaged;				//是否为manager节点
	private String expectedConfigVersion;	//最近一次配置修改的时间戳
	private String lastHeartbeat;			//最近一次心跳的时间戳
	private String numCores;				//core的数量
	private String totalPhysMemBytes;		//总的物理空间大小
	private String mounts;					//硬盘挂载点
	private String status;					//节点状态
	private String cpu;						//cpu信息
	private String disk;					//磁盘信息
	private String osType;					//操作系统
	private String serverKey;				//机器码
	private List<RoleBean> roles;			//包含的服务角色
	
	public NodeBean() {}
	
	public NodeBean(JSONObject json) {
		this.setNodeId(json.get("id"));
		this.setHostName(json.get("hostName"));
		this.setIpAddress(json.get("ipAddress"));
		this.setClusterId(json.get("clusterId"));
		this.setClusterName(json.get("clusterName"));
		this.setSshConfigId(json.get("sshConfigId"));
		this.setRackId(json.get("rackId"));
		this.setRackName(json.get("rackName"));	
		this.setIsManaged(json.get("isManaged"));
		this.setExpectedConfigVersion(json.get("expectedConfigVersion"));
		this.setLastHeartbeat(json.get("lastHeartbeat"));
		this.setNumCores(json.get("numCores"));
		this.setTotalPhysMemBytes(json.get("totalPhysMemBytes"));
		this.setMounts(json.get("mounts"));
		this.setStatus(json.get("status"));
		this.setCpu(json.get("cpu"));
		this.setDisk(json.get("disk"));
		this.setOsType(json.get("osType"));
		this.setServerKey(json.get("serverKey"));
		try {
			JSONArray roleArray = json.getJSONArray("roles");
			int num = roleArray.size();
			for(int i = 0; i < num; i++) {
				JSONObject roleJson = roleArray.getJSONObject(i); 
				RoleBean role = new RoleBean(roleJson);
				this.addRole(role);
			}
		} catch(Exception e) {}
	}
	
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(Object nodeId) {
		if(nodeId == null) return;
		this.nodeId = nodeId.toString();
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(Object hostName) {
		if(hostName == null) return;
		this.hostName = hostName.toString();
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(Object ipAddress) {
		if(ipAddress == null) return;
		this.ipAddress = ipAddress.toString();
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
	public String getSshConfigId() {
		return sshConfigId;
	}
	public void setSshConfigId(Object sshConfigId) {
		if(sshConfigId == null) return;
		this.sshConfigId = sshConfigId.toString();
	}
	public String getRackId() {
		return rackId;
	}
	public void setRackId(Object rackId) {
		if(rackId == null) return;
		this.rackId = rackId.toString();
	}
	public String getRackName() {
		return rackName;
	}
	public void setRackName(Object rackName) {
		if(rackName == null) return;
		this.rackName = rackName.toString();
	}
	public String getIsManaged() {
		return isManaged;
	}
	public void setIsManaged(Object isManaged) {
		if(isManaged == null) return;
		this.isManaged = isManaged.toString();
	}
	public String getExpectedConfigVersion() {
		return expectedConfigVersion;
	}
	public void setExpectedConfigVersion(Object expectedConfigVersion) {
		if(expectedConfigVersion == null) return;
		this.expectedConfigVersion = expectedConfigVersion.toString();
	}
	public String getLastHeartbeat() {
		return lastHeartbeat;
	}
	public void setLastHeartbeat(Object lastHeartbeat) {
		if(lastHeartbeat == null) return;
		this.lastHeartbeat = lastHeartbeat.toString();
	}
	public String getNumCores() {
		return numCores;
	}
	public void setNumCores(Object numCores) {
		if(numCores == null) return;
		this.numCores = numCores.toString();
	}
	public String getTotalPhysMemBytes() {
		return totalPhysMemBytes;
	}
	public void setTotalPhysMemBytes(Object totalPhysMemBytes) {
		if(totalPhysMemBytes == null) return;
		this.totalPhysMemBytes = totalPhysMemBytes.toString();
	}
	public String getMounts() {
		return mounts;
	}
	public void setMounts(Object mounts) {
		if(mounts == null) return;
		this.mounts = mounts.toString();
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(Object status) {
		if(status == null) return;
		this.status = status.toString();
	}
	public String getCpu() {
		return cpu;
	}
	public void setCpu(Object cpu) {
		if(cpu == null) return;
		this.cpu = cpu.toString();
	}
	public String getDisk() {
		return disk;
	}
	public void setDisk(Object disk) {
		if(disk == null) return;
		this.disk = disk.toString();
	}
	public String getOsType() {
		return osType;
	}
	public void setOsType(Object osType) {
		if(osType == null) return;
		this.osType = osType.toString();
	}
	public String getServerKey() {
		return serverKey;
	}
	public void setServerKey(Object serverKey) {
		if(serverKey == null) return;
		this.serverKey = serverKey.toString();
	}
	public List<RoleBean> getRoles() {
		return roles;
	}
	public void addRole(RoleBean role) {
		if(role == null) return;
		if(this.roles == null) {
			this.roles = new ArrayList<RoleBean>();
		}
		this.roles.add(role);
	}
}
