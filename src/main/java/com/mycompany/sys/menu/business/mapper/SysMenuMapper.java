package com.mycompany.sys.menu.business.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mycompany.sys.menu.domain.entity.SysMenuDO;
import com.mycompany.sys.menu.domain.param.SysMenuQueryParam;
import com.mycompany.sys.menu.domain.vo.SysMenuTreeVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenuDO> {

	@Select("SELECT * FROM sys_menu WHERE pid is null OR pid = 0 OR pid = kid")
	List<SysMenuTreeVO> findRootList();

	List<SysMenuTreeVO> findVOList(SysMenuQueryParam param);

	List<SysMenuDO> findList(SysMenuQueryParam param);

	@Select("SELECT * from sys_menu WHERE kid = #{kid} FOR UPDATE")
	SysMenuDO selectByIdForUpdate(Long kid);


	//region 新增子节点所需的SQL

	@Update("UPDATE sys_menu SET value_left =  value_left + 2  WHERE value_left >= #{parentRight} AND root_id = #{rootId}")
	int updateLeftByParentRight(Integer parentRight, Long rootId);

	@Update("UPDATE sys_menu SET value_right = value_right + 2 WHERE value_right >= #{parentRight} AND root_id = #{rootId}")
	int updateRightByParentRight(Integer parentRight, Long rootId);

	default int insertChild(SysMenuDO childMenu, SysMenuDO parent) {
		childMenu.setPid(parent.getKid());
		childMenu.setRootId(parent.getRootId());
		childMenu.setValueLeft(parent.getValueRight());
		childMenu.setValueRight(parent.getValueRight() + 1);
		childMenu.setLevel(parent.getLevel() + 1);
		return insert(childMenu);
	}

	//endregion


	//region 删除节点所需的SQL

	@Delete("DELETE FROM sys_menu WHERE value_left >= #{parentLeft} AND value_right <= #{parentRight} AND root_id = #{rootId}")
	int deleteByParentLeftAndRight(Integer parentLeft, Integer parentRight, Long rootId);

	@Update("UPDATE sys_menu SET value_left  = value_left  - (#{parentRight} - #{parentLeft} + 1) WHERE value_left  > #{parentLeft} AND root_id = #{rootId}")
	int updateGreaterThanParentLeft(Integer parentLeft, Integer parentRight, Long rootId);

	@Update("UPDATE sys_menu SET value_right = value_right - (#{parentRight} - #{parentLeft} + 1) WHERE value_right > #{parentRight} AND root_id = #{rootId}")
	int updateGreaterThanParentRight(Integer parentLeft, Integer parentRight, Long rootId);

	//endregion
}
