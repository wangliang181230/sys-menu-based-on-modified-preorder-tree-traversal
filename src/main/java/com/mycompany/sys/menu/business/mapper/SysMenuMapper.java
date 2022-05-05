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

	@Select("SELECT * FROM sys_menu WHERE pid IS NULL OR pid = 0 OR pid = id")
	List<SysMenuTreeVO> findRootList();

	List<SysMenuTreeVO> findVOList(SysMenuQueryParam param);

	List<SysMenuDO> findList(SysMenuQueryParam param);

	@Select("SELECT * FROM sys_menu WHERE id = #{id} FOR UPDATE")
	SysMenuDO selectByIdForUpdate(Long id);


	//region 新增子节点所需的SQL

	@Update("UPDATE sys_menu SET l = l + 2 * #{childSize}" +
			" WHERE l >= #{parentRight} AND root_id = #{rootId}")
	int updateLeftByParentRight(Integer parentRight, Long rootId, Integer childSize);

	@Update("UPDATE sys_menu SET r = r + 2 * #{childSize}" +
			" WHERE r >= #{parentRight} AND root_id = #{rootId}")
	int updateRightByParentRight(Integer parentRight, Long rootId, Integer childSize);

	default int insertChild(SysMenuDO childMenu, SysMenuDO parent) {
		childMenu.setPid(parent.getId());
		childMenu.setRootId(parent.getRootId());
		childMenu.setL(parent.getR());
		childMenu.setR(parent.getR() + 1);
		childMenu.setLevel(parent.getLevel() + 1);
		return this.insert(childMenu);
	}

	//endregion


	//region 删除节点所需的SQL

	@Delete("DELETE FROM sys_menu WHERE l >= #{parentLeft} AND r <= #{parentRight} AND root_id = #{rootId}")
	int deleteByParentLeftAndRight(Integer parentLeft, Integer parentRight, Long rootId);

	@Update("UPDATE sys_menu SET l = l - (#{right} - #{left} + 1)" +
			" WHERE l > #{right} AND root_id = #{rootId}")
	int updateLeftGreaterThanParentLeft(Integer left, Integer right, Long rootId);

	@Update("UPDATE sys_menu SET r = r - (#{right} - #{left} + 1)" +
			" WHERE r > #{right} AND root_id = #{rootId}")
	int updateRightGreaterThanParentRight(Integer left, Integer right, Long rootId);

	//endregion


	//region 移动节点所需的SQL

	//region 节点移动到其他节点下所需的SQL

	@Update("UPDATE sys_menu SET l = l + #{length}" +
			" WHERE root_id = #{rootId} AND l >= #{targetParentRight} AND l < #{left}"
	)
	int updateLeftForMoveLeft(Long rootId, Integer targetParentRight, Integer left, Integer length);


	@Update("UPDATE sys_menu SET r = r + #{length}" +
			" WHERE root_id = #{rootId} AND r >= #{targetParentRight} AND r < #{left}"
	)
	int updateRightForMoveLeft(Long rootId, Integer targetParentRight, Integer left, Integer length);


	@Update("UPDATE sys_menu SET l = l - #{length}" +
			" WHERE root_id = #{rootId} AND l > #{right} AND l < #{targetParentRight}"
	)
	int updateLeftForMoveRight(Long rootId, Integer targetParentRight, Integer right, Integer length);

	@Update("UPDATE sys_menu SET r = r - #{length}" +
			" WHERE root_id = #{rootId} AND r > #{right} AND r < #{targetParentRight}"
	)
	int updateRightForMoveRight(Long rootId, Integer targetParentRight, Integer right, Integer length);


	@Update("UPDATE sys_menu" +
			"   SET l = l + (#{parentRight} - #{left} - #{length})," +
			"       r = r + (#{parentRight} - #{left} - #{length})," +
			"       level = level + (#{targetLevel} - #{level} + 1)," +
			"       root_id = #{targetRootId}" +
			" WHERE root_id = #{rootId} AND l >= #{left} AND r <= #{right}" // 被移动节点
	)
	int updateLeftAndRightForMoves(Integer parentRight, Long rootId, Integer left, Integer right, Long targetRootId, Integer level, Integer targetLevel, Integer length);

	@Update("UPDATE sys_menu SET pid = #{pid} WHERE id = #{id}")
	int updatePid(Long id, Long pid);

	@Update("UPDATE sys_menu SET root_id = #{newRootId}" +
			" WHERE root_id = #{rootId} AND l >= #{left} AND r <= #{right}")
	int updateRootId(Long rootId, Integer left, Integer right, Long newRootId);

	//endregion

	//region 节点独立成一个根节点所需的SQL

	@Update("UPDATE sys_menu" +
			"   SET l = l - #{left} + 1," +
			"       r = r - #{left} + 1," +
			"       level = level - #{level} + 1," +
			"       root_id = #{id}" +
			" WHERE root_id = #{rootId} AND l >= #{left} AND r <= #{right}" // 被移动节点
	)
	int updateLeftAndRightForMoves2(Long id, Long rootId, Integer left, Integer right, Integer level);

	//endregion

	//endregion
}
