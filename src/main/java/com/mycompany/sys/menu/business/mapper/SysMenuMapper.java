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


	//region 受影响的节点的左右值更新SQL

	@Update("UPDATE sys_menu" +
			"   SET l = (" +
			"         CASE WHEN l >= #{start}" +
			"           THEN l + #{changeValue}" +
			"           ELSE l" +
			"         END" +
			"       )," +
			"       r = (" +
			"         CASE WHEN r >= #{start}" +
			"           THEN r + #{changeValue}" +
			"           ELSE r" +
			"         END" +
			"       )" +
			" WHERE root_id = #{rootId}" +
			"   AND (" +
			"          l >= #{start}" +
			"       OR r >= #{start}" +
			"   )")
	int updateLeftAndRightByStart(Long rootId, Integer start, Integer changeValue);

	@Update("UPDATE sys_menu" +
			"   SET l = (" +
			"         CASE WHEN l BETWEEN #{start} AND #{end}" +
			"           THEN l + #{changeValue}" +
			"           ELSE l" +
			"         END" +
			"       )," +
			"       r = (" +
			"         CASE WHEN r BETWEEN #{start} AND #{end}" +
			"           THEN r + #{changeValue}" +
			"           ELSE r" +
			"         END" +
			"       )" +
			" WHERE root_id = #{rootId}" +
			"   AND (" +
			"       l BETWEEN #{start} AND #{end}" +
			"    OR r BETWEEN #{start} AND #{end}" +
			"   )")
	int updateLeftAndRightByStartAndEnd(Long rootId, Integer start, Integer end, Integer changeValue);

	//endregion


	//region ”新增子节点“ 所需的SQL

	default int insertChild(SysMenuDO child, SysMenuDO parent) {
		child.setPid(parent.getId());
		child.setRootId(parent.getRootId());
		child.setL(parent.getR());
		child.setR(parent.getR() + 1);
		child.setLevel(parent.getLevel() + 1);
		return this.insert(child);
	}

	//endregion


	//region ”删除节点“ 所需的SQL

	/**
	 * 删除左右值在某个范围内的所有节点
	 *
	 * @param rootId 根节点ID
	 * @param start  起始左右值
	 * @param end    截止左右值
	 * @return deletedRows 返回删除的行数
	 */
	@Delete("DELETE FROM sys_menu WHERE root_id = #{rootId} AND l >= #{start} AND r <= #{end}")
	int deleteByStartAndEnd(Long rootId, Integer start, Integer end);

	//endregion


	//region 移动节点所需的SQL

	@Update("UPDATE sys_menu SET pid = #{pid} WHERE id = #{id}")
	int updatePid(Long id, Long pid);

	//region ”移动节点到目标父节点下“ 所需的SQL

	@Update("UPDATE sys_menu SET root_id = #{tempRootId}" +
			" WHERE root_id = #{rootId} AND l >= #{left} AND r <= #{right}")
	int updateRootIdToTempRootId(Long rootId, Integer left, Integer right, Long tempRootId);

	@Update("UPDATE sys_menu" +
			"   SET l = l + (#{parentRight} - #{left} - #{length})," +
			"       r = r + (#{parentRight} - #{left} - #{length})," +
			"       root_id = #{targetRootId}," +
			"       level = level + (#{targetLevel} - #{level} + 1)" +
			" WHERE root_id = #{rootId} AND l >= #{left} AND r <= #{right}" // 被移动节点
	)
	int updateLeftAndRightAndRootIdAndLevelForMoves(Long rootId, Integer left, Integer right, Integer parentRight, Long targetRootId, Integer level, Integer targetLevel, Integer length);

	//endregion


	//region ”将当前节点与原父节点分离，成为独立的根节点“ 所需的SQL

	@Update("UPDATE sys_menu" +
			"   SET l = l - #{left} + 1," +
			"       r = r - #{left} + 1," +
			"       root_id = #{targetRootId}," +
			"       level = level - #{level} + 1" +
			" WHERE root_id = #{rootId} AND l >= #{left} AND r <= #{right}" // 被移动节点
	)
	int updateLeftAndRightAndRootIdAndLevelForMoves2(Long rootId, Integer left, Integer right, Long targetRootId, Integer level);

	//endregion

	//endregion
}
