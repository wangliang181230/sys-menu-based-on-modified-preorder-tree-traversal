package com.mycompany.sys.menu.domain.vo;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mycompany.sys.menu.domain.entity.SysMenuDO;

/**
 * 菜单树形
 */
public class SysMenuTreeVO extends SysMenuDO {

	/**
	 * 子菜单列表
	 */
	@TableField(exist = false)
	private List<SysMenuTreeVO> childs;

	private Boolean isRoot;


	public List<SysMenuTreeVO> getChilds() {
		return childs;
	}

	public void setChilds(List<SysMenuTreeVO> childs) {
		this.childs = childs;
	}

	public Boolean getRoot() {
		return isRoot;
	}

	public void setRoot(Boolean root) {
		isRoot = root;
	}
}
