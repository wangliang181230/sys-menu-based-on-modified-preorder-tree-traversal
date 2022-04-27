package com.mycompany.sys.menu.domain.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mycompany.sys.menu.domain.vo.SysMenuTreeVO;

public abstract class MenuUtils {

	/**
	 * 列表转树（兼容根节点为整颗树中的某个节点，而不是根节点）
	 *
	 * @param list 菜单列表
	 * @return tree 菜单树
	 */
	public static List<SysMenuTreeVO> listToTree(List<SysMenuTreeVO> list) {
		List<SysMenuTreeVO> rootMenu = new ArrayList<>(list);

		for (SysMenuTreeVO nav : rootMenu) {
			List<SysMenuTreeVO> childList = getChildList(nav.getKid(), list);
			nav.setChilds(childList);
		}

		// 去掉不是根节点的
		rootMenu.removeIf(menu -> Boolean.FALSE.equals(menu.getRoot()));

		return rootMenu;
	}

	/**
	 * 获取子菜单列表
	 *
	 * @param id   父菜单ID
	 * @param list 菜单列表
	 * @return 子菜单列表
	 */
	private static List<SysMenuTreeVO> getChildList(Long id, List<SysMenuTreeVO> list) {
		List<SysMenuTreeVO> childList = new ArrayList<>();
		for (SysMenuTreeVO nav : list) {
			if (Objects.equals(nav.getPid(), id) && !Objects.equals(nav.getKid(), id)) {
				childList.add(nav);
				nav.setRoot(false);
			}
		}
		for (SysMenuTreeVO nav : childList) {
			nav.setChilds(getChildList(nav.getKid(), list));
		}
		return childList;
	}

}
