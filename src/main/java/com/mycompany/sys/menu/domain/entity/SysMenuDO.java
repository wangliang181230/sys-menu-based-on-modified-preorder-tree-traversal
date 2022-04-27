package com.mycompany.sys.menu.domain.entity;

import java.util.Objects;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_menu")
public class SysMenuDO {

	@TableId(type = IdType.ASSIGN_ID)
	private Long kid; // 主键

	private Long pid; //  父级id

	private String name; //  菜单名称

	private Integer valueLeft; //  左值

	private Integer valueRight; //  右值

	private Integer level; // 层级

	private Long grandfatherId; //  爷爷级id


	public boolean isRoot() {
		return pid == null || Objects.equals(pid, 0L);
	}


	//region Getter and Setter

	public Long getKid() {
		return kid;
	}

	public void setKid(Long kid) {
		this.kid = kid;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getValueLeft() {
		return valueLeft;
	}

	public void setValueLeft(Integer valueLeft) {
		this.valueLeft = valueLeft;
	}

	public Integer getValueRight() {
		return valueRight;
	}

	public void setValueRight(Integer valueRight) {
		this.valueRight = valueRight;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Long getGrandfatherId() {
		return grandfatherId;
	}

	public void setGrandfatherId(Long grandfatherId) {
		this.grandfatherId = grandfatherId;
	}

	//endregion
}
