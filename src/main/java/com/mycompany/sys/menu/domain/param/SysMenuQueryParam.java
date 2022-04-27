package com.mycompany.sys.menu.domain.param;

/**
 * 菜单查询参数
 */
public class SysMenuQueryParam {

	private Long pid;

	private Integer parentLeft;

	private Integer parentRight;


	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public Integer getParentLeft() {
		return parentLeft;
	}

	public void setParentLeft(Integer parentLeft) {
		this.parentLeft = parentLeft;
	}

	public Integer getParentRight() {
		return parentRight;
	}

	public void setParentRight(Integer parentRight) {
		this.parentRight = parentRight;
	}
}
