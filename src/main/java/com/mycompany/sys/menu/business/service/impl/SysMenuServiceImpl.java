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
			entity.setId(IdWorker.getId()); // ID先生成好，因为要复制给pid和rootId（TODO 注意：ID生成方式根据自己的项目进行调整）
			entity.setPid(entity.getId()); // 复制id到pid
			entity.setRootId(entity.getId()); // 复制id到rootId
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

			baseMapper.updateLeftByParentRight(parentEntity.getR(), parentEntity.getRootId(), 1);
			baseMapper.updateRightByParentRight(parentEntity.getR(), parentEntity.getRootId(), 1);

			entity.setRootId(parentEntity.getRootId());
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

		baseMapper.deleteByParentLeftAndRight(entity.getL(), entity.getR(), entity.getRootId());
		baseMapper.updateLeftGreaterThanParentLeft(entity.getL(), entity.getR(), entity.getRootId());
		baseMapper.updateRightGreaterThanParentRight(entity.getL(), entity.getR(), entity.getRootId());
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
			return; // 父ID已经是目标父ID了，支持幂等
		}

		if (!Objects.equals(id, targetPid)) { // 转移到目标父节点下
			// 获取目标父节点数据
			SysMenuDO targetParent = baseMapper.selectByIdForUpdate(targetPid);
			if (targetParent == null) {
				throw new RuntimeException("移动到的目标父节点数据不存在，id：" + targetPid);
			}

			// 判断是否为当前节点的子节点
			if (entity.isMyChild(targetParent)) {
				throw new RuntimeException("无法移动到自己的子节点下");
			}

			// 移动到目标父节点
			// 先更新受影响的节点的左右值
			int moveSize = entity.getLength() / 2; // 被移动的节点数量
			baseMapper.updateLeftByParentRight(targetParent.getR(), targetParent.getRootId(), moveSize);
			baseMapper.updateRightByParentRight(targetParent.getR(), targetParent.getRootId(), moveSize);
			// 再更新移动节点的左右值及rootId
			baseMapper.updateLeftAndRightForMoves(targetParent.getR(), entity.getRootId(), entity.getL(), entity.getR(), targetParent.getRootId(), entity.getLevel(), targetParent.getLevel());
			baseMapper.updatePid(entity.getId(), targetParent.getId());
			// 不是根节点，移出时，更新受影响节点的左右值
			if (!entity.isRoot()) {
				baseMapper.updateLeftGreaterThanParentLeft(entity.getL(), entity.getR(), entity.getRootId());
				baseMapper.updateRightGreaterThanParentRight(entity.getL(), entity.getR(), entity.getRootId());
			}
		} else { // 将当前节点与原父节点分离，转换为独立的根节点（带上其子节点）
			if (entity.isRoot()) {
				return; // 已经是根节点，无需处理
			}

			// 获取目标父节点数据
			SysMenuDO targetParent = baseMapper.selectByIdForUpdate(entity.getPid());
			if (targetParent == null) {
				throw new RuntimeException("父节点数据不存在，id：" + targetPid);
			}

			// 移出父节点
			// 先更新移动的节点的左右值
			baseMapper.updateLeftAndRightForMoves2(entity.getId(), entity.getRootId(), entity.getL(), entity.getR(), entity.getLevel());
			baseMapper.updatePid(entity.getId(), entity.getId());
			// 再更新受影响的节点的左右值
			baseMapper.updateLeftGreaterThanParentLeft(entity.getL(), entity.getR(), entity.getRootId());
			baseMapper.updateRightGreaterThanParentRight(entity.getL(), entity.getR(), entity.getRootId());
		}
	}
}
