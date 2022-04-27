package com.mycompany.sys.menu.business.service.impl;

import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.mycompany.sys.menu.business.mapper.SysMenuMapper;
import com.mycompany.sys.menu.business.service.ISysMenuService;
import com.mycompany.sys.menu.domain.entity.SysMenuDO;
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
	public Long insertMenu(SysMenuDO entity) {
		if (entity.isRoot()) { // 直接新增根节点
			if (entity.getKid() == null) {
				entity.setKid(IdWorker.getId()); // ID先生成好，因为要复制给pid和rootId
			}
			entity.setPid(entity.getKid()); // 复制kid到pid
			entity.setRootId(entity.getKid()); // 复制kid到rootId
			entity.setValueLeft(1);
			entity.setValueRight(2);
			entity.setLevel(1);
			if (!super.save(entity)) {
				throw new RuntimeException("新增菜单失败");
			}
		} else { // 新增叶节点
			SysMenuDO parentEntity = super.getById(entity.getPid());
			if (parentEntity == null) {
				throw new RuntimeException("父节点不存在，id: " + entity.getPid());
			}

			baseMapper.updateLeftByParentRight(parentEntity.getValueRight(), parentEntity.getRootId());
			baseMapper.updateRightByParentRight(parentEntity.getValueRight(), parentEntity.getRootId());

			entity.setRootId(parentEntity.getRootId());
			if (!SqlHelper.retBool(baseMapper.insertChild(entity, parentEntity))) {
				throw new RuntimeException("新增叶节点失败");
			}
		}

		return entity.getKid();
	}

	@Override
	@Transactional
	public void deleteMenuAndChilds(Long kid) {
		SysMenuDO entity = baseMapper.selectByIdForUpdate(kid);
		if (entity == null) {
			return; // 已经删除，直接返回成功
		}

		this.deleteMenuAndChilds(entity);
	}

	private void deleteMenuAndChilds(SysMenuDO entity) {
		baseMapper.deleteByParentLeftAndRight(entity.getValueLeft(), entity.getValueRight(), entity.getRootId());
		baseMapper.updateGreaterThanParentLeft(entity.getValueLeft(), entity.getValueRight(), entity.getRootId());
		baseMapper.updateGreaterThanParentRight(entity.getValueLeft(), entity.getValueRight(), entity.getRootId());
	}

	@Override
	@Transactional
	public void moveMenu(Long kid, Long targetParentKid) {
		// 获取菜单数据
		SysMenuDO entity = baseMapper.selectByIdForUpdate(kid);
		if (entity == null) {
			throw new RuntimeException("菜单数据不存在，id：" + kid);
		}
		if (Objects.equals(entity.getPid(), targetParentKid)) {
			return; // 父ID已经是目标父ID了，支持幂等
		}

		SysMenuDO targetParent;
		if (!Objects.equals(kid, targetParentKid)) {
			// 获取目标父菜单数据
			targetParent = super.getById(targetParentKid);
			if (targetParent == null) {
				throw new RuntimeException("目标父菜单数据不存在，id：" + targetParentKid);
			}
		} else {
			targetParent = entity;
		}

		// 删除菜单前，先把所有子菜单全部获取出来
		List<SysMenuTreeVO> childs = this.findChilds(entity);
		if (entity != targetParent) { // 不移动到根节点时，校验移动目标是否为子节点
			if (childs.stream().anyMatch(menu -> Objects.equals(menu.getKid(), targetParentKid))) {
				throw new RuntimeException("无法移动到自己的子节点");
			}
		}

		// 先删除当前菜单及其所有子菜单
		this.deleteMenuAndChilds(entity);

		// 设置新的父菜单ID和根节点，再新增菜单
		entity.setPid(targetParentKid);
		if (!Objects.equals(entity.getRootId(), targetParent.getRootId())) {
			entity.setRootId(targetParent.getRootId());
			for (SysMenuTreeVO child : childs) {
				child.setRootId(entity.getRootId());
			}
		}
		this.insertMenu(entity);

		// 重新新增所有子菜单
		childs = MenuUtils.listToTree(childs); // 转为树型
		this.saveChilds(childs);
	}

	private void saveChilds(List<SysMenuTreeVO> childs) {
		if (childs == null) {
			return;
		}
		for (SysMenuTreeVO menu : childs) {
			this.insertMenu(menu);
			this.saveChilds(menu.getChilds()); // 递归
		}
	}

	private List<SysMenuTreeVO> findChilds(SysMenuDO entity) {
		SysMenuQueryParam param = new SysMenuQueryParam();
		param.setRootId(entity.getRootId());
		param.setParentLeft(entity.getValueLeft());
		param.setParentRight(entity.getValueRight());

		return baseMapper.findVOList(param);
	}
}
