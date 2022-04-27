package com.mycompany.sys.menu.business.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mycompany.sys.menu.domain.entity.SysMenuDO;
import com.mycompany.sys.menu.domain.param.SysMenuQueryParam;
import com.mycompany.sys.menu.domain.vo.SysMenuTreeVO;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface ISysMenuService extends IService<SysMenuDO> {

	/**
	 * 查询根列表数据
	 *
	 * @param param 查询参数
	 * @return 返回列表数据
	 */
	List<SysMenuTreeVO> findRootList(@ModelAttribute SysMenuQueryParam param);

	/**
	 * 查询列表数据
	 *
	 * @param param 查询参数
	 * @return 返回列表数据
	 */
	List<SysMenuTreeVO> findList(@ModelAttribute SysMenuQueryParam param);

	/**
	 * 查询树型数据
	 *
	 * @param param 查询参数
	 * @return 返回树型数据
	 */
	List<SysMenuTreeVO> findTree(@ModelAttribute SysMenuQueryParam param);

	/**
	 * 新增一个菜单
	 *
	 * @param entity 菜单数据
	 * @return 菜单ID
	 */
	Long insertMenu(SysMenuDO entity);

	/**
	 * 删除一个菜单
	 *
	 * @param kid 菜单ID
	 */
	void deleteMenuAndChilds(Long kid);

	/**
	 * 移动菜单
	 *
	 * @param kid             菜单ID
	 * @param targetParentKid 目标父菜单ID
	 */
	void moveMenu(Long kid, Long targetParentKid);
}
