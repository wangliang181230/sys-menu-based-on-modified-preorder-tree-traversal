package com.mycompany.sys.menu.domain.param;

public class SysMenuMoveParam {

	/**
	 * 需要移动的菜单ID
	 */
	private Long kid;

	/**
	 * 移到到的目标父ID
	 */
	private Long targetPid;


	public Long getKid() {
		return kid;
	}

	public void setKid(Long kid) {
		this.kid = kid;
	}

	public Long getTargetPid() {
		return targetPid;
	}

	public void setTargetPid(Long targetPid) {
		this.targetPid = targetPid;
	}
}
