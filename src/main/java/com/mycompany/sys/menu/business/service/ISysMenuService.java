package com.mycompany.sys.menu.business.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mycompany.sys.menu.domain.entity.SysMenuDO;
import com.mycompany.sys.menu.domain.param.SysMenuInsertParam;
import com.mycompany.sys.menu.domain.param.SysMenuQueryParam;
import com.mycompany.sys.menu.domain.vo.SysMenuTreeVO;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface ISysMenuService extends IService<SysMenuDO> {

	/**
	 * 查询根节点列表数据
	 *
	 * @return 返回根节点列表数据
	 */
	List<SysMenuTreeVO> findRootList();

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
	 * 新增一个节点
	 *
	 * @param param 新增节点参数
	 * @return 节点ID
	 */
	Long insertMenu(SysMenuInsertParam param);

	/**
	 * 删除一个节点
	 *
	 * @param id 节点ID
	 */
	void deleteMenuAndChilds(Long id);

	/**
	 * 移动节点
	 *
	 * @param id        节点ID
	 * @param targetPid 目标父节点ID
	 */
	void moveMenu(Long id, Long targetPid);
}
