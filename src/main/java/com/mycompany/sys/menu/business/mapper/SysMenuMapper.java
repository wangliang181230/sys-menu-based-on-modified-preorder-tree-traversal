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

	/**
	 * 悲观锁
	 *
	 * @param id 节点ID
	 * @return 节点数据
	 */
	@Select("SELECT * FROM sys_menu WHERE id = #{id} FOR UPDATE")
	SysMenuDO selectByIdForUpdate(Long id);

	/**
	 * 获取根节点列表
	 *
	 * @return 返回根节点列表
	 */
	@Select("SELECT * FROM sys_menu WHERE pid IS NULL OR pid = 0 OR pid = id")
	List<SysMenuTreeVO> findRootList();

	/**
	 * 查询 VO 列表
	 *
	 * @param param 查询参数
	 * @return 返回节点列表
	 */
	List<SysMenuTreeVO> findVOList(SysMenuQueryParam param);

	/**
	 * 查询 DO 列表
	 *
	 * @param param 查询参数
	 * @return 返回节点列表
	 */
	List<SysMenuDO> findList(SysMenuQueryParam param);

	/**
	 * 获取一个节点的父节点列表（按左值排序）
	 *
	 * @param rootId 当前节点所在的根节点ID
	 * @param left   当前节点的左值
	 * @param right  当前节点的右值
	 * @return 返回父节点列表（按左值排序）
	 */
	@Select("SELECT * FROM sys_menu" +
			" WHERE t.root_id = #{rootId}" +
			"   AND l < #{left}" +
			"   AND r > #{right}" +
			" ORDER BY l")
	List<SysMenuDO> findAllParentList(Long rootId, Integer left, Integer right);


	//region 受影响的节点的左右值更新SQL

	/**
	 * 更新左右值大于start的所有节点的左右值
	 *
	 * @param rootId      根节点ID
	 * @param start       起始值（>= start）
	 * @param changeValue 变更值（可为正数可为负数）
	 * @return 返回受影响行数
	 */
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

	/**
	 * 更新左右值在start和end范围内的所有节点的左右值
	 *
	 * @param rootId      根节点ID
	 * @param start       起始值（>= start）
	 * @param end         截止值（<= end）
	 * @param changeValue 变更值（可为正数可为负数）
	 * @return 返回受影响行数
	 */
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

	/**
	 * 新增子节点
	 *
	 * @param child  子节点
	 * @param parent 父节点
	 * @return 新增成功返回1，失败返回 <= 0
	 */
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

	/**
	 * 更新被移动节点的父ID
	 *
	 * @param id     节点ID
	 * @param newPid 新的父ID
	 * @return 更新成功返回1，失败返回 <= 0
	 */
	@Update("UPDATE sys_menu SET pid = #{newPid} WHERE id = #{id}")
	int updatePid(Long id, Long newPid);

	//region ”移动节点到目标父节点下“ 所需的SQL

	/**
	 * 隔离 ”被移动节点及其所有子节点“，防止左右值更新受影响
	 *
	 * @param rootId     根节点ID
	 * @param left       左值
	 * @param right      右值
	 * @param tempRootId 临时根节点ID
	 * @return 返回受影响行数
	 */
	@Update("UPDATE sys_menu SET root_id = #{tempRootId}" +
			" WHERE root_id = #{rootId} AND l >= #{left} AND r <= #{right}")
	int updateRootIdToTempRootId(Long rootId, Integer left, Integer right, Long tempRootId);

	/**
	 * 更新被移动节点及其子节点的左右值、rootId、level
	 *
	 * @param rootId       根节点ID
	 * @param left         左值
	 * @param right        右值
	 * @param parentRight  父右值
	 * @param targetRootId 目标rootId
	 * @param level        被移动节点的层级
	 * @param targetLevel  目标层级
	 * @param length       被移动节点的长度
	 * @return 返回受影响行数
	 */
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

	/**
	 * 更新被移动节点及其子节点的左右值、rootId、level，使其变为独立的一棵树。
	 *
	 * @param rootId       根ID
	 * @param left         左值
	 * @param right        右值
	 * @param targetRootId 目标rootId
	 * @param level        被移动节点的层级
	 * @return 返回受影响行数
	 */
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
