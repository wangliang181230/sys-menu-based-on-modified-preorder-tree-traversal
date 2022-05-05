package com.mycompany.sys.menu.domain.entity;

import java.util.Objects;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_menu")
public class SysMenuDO {

	@TableId(type = IdType.ASSIGN_ID)
	private Long id; // 主键

	private Long pid; //  父级ID

	private String name; //  节点名称

	private Integer l; //  左值

	private Integer r; //  右值

	private Integer level; // 层级

	private Long rootId; //  根节点ID


	/**
	 * 是否根节点
	 *
	 * @return true=是 | false=否
	 */
	public boolean isRoot() {
		return pid == null || Objects.equals(pid, 0L) || Objects.equals(pid, id);
	}

	/**
	 * 是否叶子节点
	 *
	 * @return true=是 | false=否
	 */
	public boolean isLeaf() {
		return l != null && r != null && Objects.equals(l, r - 1);
	}

	/**
	 * 获取当前节点左右值长度
	 *
	 * @return 返回左右值长度
	 */
	public int getLength() {
		if (l == null || r == null) {
			return 0;
		}
		return r - l + 1;
	}

	/**
	 * 获取子节点数量
	 *
	 * @return 返回子节点数量
	 */
	public int getChildSize() {
		if (l == null || r == null) {
			return 0;
		}
		return (r - l - 1) / 2;
	}

	/**
	 * 判断入参节点是否为当前节点的子节点
	 *
	 * @param otherMenu 节点数据
	 * @return true=是 | false=否
	 */
	public boolean isMyChild(SysMenuDO otherMenu) {
		try {
			return Objects.equals(rootId, otherMenu.rootId) // 相同的根节点下
					&& otherMenu.l > this.l && otherMenu.r < this.r; // 左右值在当前节点的左右值范围内
		} catch (NullPointerException e) {
			return false;
		}
	}


	//region Getter and Setter

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getL() {
		return l;
	}

	public void setL(Integer l) {
		this.l = l;
	}

	public Integer getR() {
		return r;
	}

	public void setR(Integer r) {
		this.r = r;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Long getRootId() {
		if (rootId == null) {
			return id;
		}
		return rootId;
	}

	public void setRootId(Long rootId) {
		this.rootId = rootId;
	}

	//endregion
}
