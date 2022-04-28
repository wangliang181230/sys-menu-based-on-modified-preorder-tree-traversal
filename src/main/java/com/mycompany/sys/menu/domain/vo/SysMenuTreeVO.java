package com.mycompany.sys.menu.domain.vo;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mycompany.sys.menu.domain.entity.SysMenuDO;

/**
 * 节点树形
 */
public class SysMenuTreeVO extends SysMenuDO {

	/**
	 * 子节点列表
	 */
	@TableField(exist = false)
	private List<SysMenuTreeVO> childList;

	@TableField(exist = false)
	private Boolean r;


	public List<SysMenuTreeVO> getChildList() {
		return childList;
	}

	public void setChildList(List<SysMenuTreeVO> childList) {
		this.childList = childList;
	}

	public Boolean getR() {
		return r;
	}

	public void setR(Boolean r) {
		this.r = r;
	}
}
