package com.mycompany.sys.menu.business.service.impl;

import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.mycompany.sys.menu.business.mapper.SysMenuMapper;
import com.mycompany.sys.menu.business.service.ISysMenuService;
import com.mycompany.sys.menu.domain.entity.SysMenuDO;
import com.mycompany.sys.menu.domain.param.SysMenuInsertParam;
import com.mycompany.sys.menu.domain.param.SysMenuQueryParam;
import com.mycompany.sys.menu.domain.utils.MenuUtils;
import com.mycompany.sys.menu.domain.vo.SysMenuTreeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuDO>
		implements ISysMenuService {

	@Override
	public List<SysMenuTreeVO> findRootList() {
		return baseMapper.findRootList();
	}

	@Override
	public List<SysMenuTreeVO> findList(SysMenuQueryParam param) {
		return baseMapper.findVOList(param);
	}

	@Override
	public List<SysMenuTreeVO> findTree(SysMenuQueryParam param) {
		List<SysMenuTreeVO> list = baseMapper.findVOList(param);
		return MenuUtils.listToTree(list); // 列表转为树
	}

	@Override
	@Transactional
	public Long insertMenu(SysMenuInsertParam param) {
		SysMenuDO entity = new SysMenuDO();
		entity.setName(param.getName());
		entity.setPid(param.getPid());

		if (entity.isRoot()) { // 直接新增根节点
			entity.setId(IdWorker.getId()); // ID先生成好，因为要复制给pid和rootId（TODO: 注意，ID生成方式根据自己的项目进行调整）
			entity.setPid(entity.getId()); // 复制id到pid
			entity.setRootId(entity.getId()); // 复制id到rootId
			// 新的节点，左右值及level都是固定值
			entity.setL(1);
			entity.setR(2);
			entity.setLevel(1);
			if (!super.save(entity)) {
				throw new RuntimeException("新增根节点失败");
			}
		} else { // 新增子节点
			SysMenuDO parentEntity = baseMapper.selectByIdForUpdate(entity.getPid());
			if (parentEntity == null) {
				throw new RuntimeException("父节点不存在，id: " + entity.getPid());
			}

			// 先更新受影响节点的左右值
			baseMapper.updateLeftAndRightByStart(parentEntity.getRootId(), parentEntity.getR(), 2);
			// 再新增节点
			if (!SqlHelper.retBool(baseMapper.insertChild(entity, parentEntity))) {
				throw new RuntimeException("新增子节点失败");
			}
		}

		return entity.getId();
	}

	@Override
	@Transactional
	public void deleteMenuAndChilds(Long id) {
		SysMenuDO entity = baseMapper.selectByIdForUpdate(id);
		if (entity == null) {
			return; // 已经删除，直接返回成功
		}

		baseMapper.deleteByStartAndEnd(entity.getRootId(), entity.getL(), entity.getR());

		// 如果不是根节点，则更新受影响节点的左右值
		if (!entity.isRoot()) {
			// 大于right的所有节点的左右值，减小删除节点的长度
			baseMapper.updateLeftAndRightByStart(entity.getRootId(), entity.getR() + 1, -1 * entity.getLength());
		}
	}

	@Override
	@Transactional
	public void moveMenu(Long id, Long targetPid) {
		// 获取节点数据
		SysMenuDO entity = baseMapper.selectByIdForUpdate(id);
		if (entity == null) {
			throw new RuntimeException("节点数据不存在，id：" + id);
		}
		if (Objects.equals(entity.getPid(), targetPid)) {
			return; // 父ID已经是目标父ID了，直接返回，支持幂等
		}

		if (targetPid != null && !Objects.equals(id, targetPid)) { // 移动节点到目标父节点下
			// 获取目标父节点数据
			SysMenuDO targetParent = baseMapper.selectByIdForUpdate(targetPid);
			if (targetParent == null) {
				throw new RuntimeException("移动到的目标父节点数据不存在，id：" + targetPid);
			}
			// 判断目标父节点是否为被移动节点的子节点
			if (entity.isMyChild(targetParent)) {
				throw new RuntimeException("无法移动到自己的子节点下");
			}

			if (!entity.getRootId().equals(targetParent.getRootId())) { // 不同根的情况下
				// 移动到目标父节点
				// 先更新受影响的目标父节点及其相关节点的左右值（不包含 被移动节点及其子节点）
				baseMapper.updateLeftAndRightByStart(targetParent.getRootId(), targetParent.getR(), entity.getLength()); // 加length
				// 再更新 被移动节点及其子节点 的左右值、rootId、level
				baseMapper.updateLeftAndRightAndRootIdAndLevelForMoves(entity.getRootId(), entity.getL(), entity.getR(), targetParent.getR(), targetParent.getRootId(), entity.getLevel(), targetParent.getLevel(), 0);
				// 如果被移动节点不是根节点，移出时，更新受影响节点的左右值
				if (!entity.isRoot()) {
					baseMapper.updateLeftAndRightByStart(entity.getRootId(), entity.getR(), -1 * entity.getLength()); // 减length
				}
			} else { // 相同根的情况下
				// 先隔离 ”被移动节点及其所有子节点“，防止左右值更新受影响
				// 目前所使用的方案是 将其抽离成一棵独立的树，以其id作为rootId，避免受到相关SQL的影响
				Long tempRootId = entity.getId(); // 以ID作为临时的rootId
				baseMapper.updateRootIdToTempRootId(entity.getRootId(), entity.getL(), entity.getR(), tempRootId);

				// 右移时，被移动节点左右值的计算需考虑自身的长度，否则为0
				int length = 0;

				// 先更新受影响节点的左右值（不包含 被移动节点及其子节点）
				int start, end, changeValue;
				if (entity.getL() > targetParent.getR()) { // 左移时
					start = targetParent.getR();
					end = entity.getL() - 1;
					changeValue = entity.getLength(); // 加length
				} else { // 右移时
					start = entity.getR() + 1;
					end = targetParent.getR() - 1;
					changeValue = -1 * entity.getLength(); // 减length

					// 右移时，被移动节点左右值的计算需考虑自身的长度
					length = entity.getLength();
				}
				baseMapper.updateLeftAndRightByStartAndEnd(entity.getRootId(), start, end, changeValue);

				// 再更新 被移动节点及其子节点 的左右值、rootId、level
				baseMapper.updateLeftAndRightAndRootIdAndLevelForMoves(tempRootId, entity.getL(), entity.getR(), targetParent.getR(), targetParent.getRootId(), entity.getLevel(), targetParent.getLevel(), length);
			}

			// 最后更新 ”被移动节点“ 的pid
			baseMapper.updatePid(entity.getId(), targetPid);
		} else { // 将当前节点与原父节点分离，成为独立的根节点（如果有子节点，也带上其子节点）
			if (entity.isRoot()) {
				return; // 已经是根节点，无需处理
			}

			// 获取目标父节点数据
			SysMenuDO targetParent = baseMapper.selectByIdForUpdate(entity.getPid());
			if (targetParent == null) {
				throw new RuntimeException("父节点数据不存在，id：" + entity.getPid());
			}

			// 移出节点及其子节点（如果有子节点的话），形成新的的一根树
			// 先更新 ”被移动节点及其子节点“ 的左右值、rootId、level
			baseMapper.updateLeftAndRightAndRootIdAndLevelForMoves2(entity.getRootId(), entity.getL(), entity.getR(), entity.getId(), entity.getLevel());
			// 再更新受影响的节点的左右值（不含 被移动节点及其子节点）
			baseMapper.updateLeftAndRightByStart(entity.getRootId(), entity.getR() + 1, -1 * entity.getLength()); // 减length
			// 最后更新 ”被移动节点“ 的pid
			baseMapper.updatePid(entity.getId(), entity.getId());
		}
	}
}
