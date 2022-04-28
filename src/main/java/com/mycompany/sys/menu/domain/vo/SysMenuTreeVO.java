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
	private Boolean c;


	public List<SysMenuTreeVO> getChildList() {
		return childList;
	}

	public void setChildList(List<SysMenuTreeVO> childList) {
		this.childList = childList;
	}

	public Boolean getC() {
		return c;
	}

	public void setC(Boolean c) {
		this.c = c;
	}
}
