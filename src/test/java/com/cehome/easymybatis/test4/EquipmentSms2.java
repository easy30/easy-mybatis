package com.cehome.easymybatis.test4;


/**
 *
 * @author apple
 * @since 1.0.0
 */

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "equipment_sms2")

public class EquipmentSms2 implements Serializable {

	/**主键**/ 
	@Id
	private Integer id;

	/**类型 1-通过 2-人工驳回 3-系统驳回**/ 
	private Integer type;

	/**设备id**/ 
	private Integer equipmentId;

	/**短信网关状态 0-失败 1-成功**/ 
	private Integer status;

	/**区**/ 
	private String zone;

	/**战队**/ 
	private String corps;

	/**手机号**/ 
	private String mobile;

	/**接收职员id**/ 
	private Integer employeeId;

	/**接收职员姓名**/ 
	private String employeeName;

	/**职位**/ 
	private Integer roleId;

	/**内容**/ 
	private String content;

	/**发送次数**/ 
	private Integer sendCount;

	/**创建时间**/ 
	private Date createTime;

	/**修改（发送）时间**/ 
	private Date modifyTime;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getEquipmentId() {
		return this.equipmentId;
	}

	public void setEquipmentId(Integer equipmentId) {
		this.equipmentId = equipmentId;
	}

	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getZone() {
		return this.zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getCorps() {
		return this.corps;
	}

	public void setCorps(String corps) {
		this.corps = corps;
	}

	public String getMobile() {
		return this.mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public Integer getEmployeeId() {
		return this.employeeId;
	}

	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}

	public String getEmployeeName() {
		return this.employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public Integer getRoleId() {
		return this.roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getSendCount() {
		return this.sendCount;
	}

	public void setSendCount(Integer sendCount) {
		this.sendCount = sendCount;
	}

	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return this.modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

}