package com.mycompany.sys.menu.domain.param;

public class SysMenuMoveParam {

	/**
	 * 需要移动的节点ID
	 */
	private Long id;

	/**
	 * 移到到的目标父ID
	 */
	private Long targetPid;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTargetPid() {
		return targetPid;
	}

	public void setTargetPid(Long targetPid) {
		this.targetPid = targetPid;
	}
}
