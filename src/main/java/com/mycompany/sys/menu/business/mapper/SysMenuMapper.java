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

	List<SysMenuTreeVO> findVOList(SysMenuQueryParam param);

	List<SysMenuDO> findList(SysMenuQueryParam param);

	@Select("SELECT * from sys_menu WHERE kid = #{kid} FOR UPDATE")
	SysMenuDO selectByIdForUpdate(Long kid);

	@Select("SELECT count(*) FROM sys_menu WHERE pid = #{pid}")
	int countByPid(Long pid);


	//region 新增子节点所需的SQL

	@Update("UPDATE sys_menu SET value_left =  value_left + 2  WHERE value_left >= #{parentRight} ")
	int updateLeftByParentRight(Integer parentRight);

	@Update("UPDATE sys_menu SET value_right = value_right + 2 WHERE value_right >= #{parentRight}")
	int updateRightByParentRight(Integer parentRight);

	default int insertChild(SysMenuDO childMenu, SysMenuDO parent) {
		childMenu.setPid(parent.getKid());
		childMenu.setValueLeft(parent.getValueRight());
		childMenu.setValueRight(parent.getValueRight() + 1);
		childMenu.setLevel(parent.getLevel() + 1);
		if (parent.getPid() != null) {
			childMenu.setGrandfatherId(parent.getPid());
		} else {
			childMenu.setGrandfatherId(0L);
		}
		return insert(childMenu);
	}

	//endregion


	//region 删除节点所需的SQL

	@Delete("DELETE FROM sys_menu WHERE value_left >= #{parentLeft} AND value_right <= #{parentRight}")
	int deleteByParentLeftAndRight(Integer parentLeft, Integer parentRight);

	@Update("UPDATE sys_menu SET value_left  = value_left  - (#{parentRight} - #{parentLeft} + 1) WHERE value_left  > #{parentLeft}")
	int updateGreaterThanParentLeft(Integer parentLeft, Integer parentRight);

	@Update("UPDATE sys_menu SET value_right = value_right - (#{parentRight} - #{parentLeft} + 1) WHERE value_right > #{parentRight}")
	int updateGreaterThanParentRight(Integer parentLeft, Integer parentRight);

	//endregion
}
