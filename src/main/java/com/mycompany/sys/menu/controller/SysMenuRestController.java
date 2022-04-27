package com.mycompany.sys.menu.controller;

import java.util.List;

import com.mycompany.sys.menu.business.service.ISysMenuService;
import com.mycompany.sys.menu.domain.entity.SysMenuDO;
import com.mycompany.sys.menu.domain.param.SysMenuMoveParam;
import com.mycompany.sys.menu.domain.param.SysMenuQueryParam;
import com.mycompany.sys.menu.domain.vo.SysMenuTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sys-menu")
public class SysMenuRestController {

	@Autowired
	private ISysMenuService sysMenuService;


	@GetMapping("/root-list")
	public List<SysMenuTreeVO> findRootList() {
		return sysMenuService.findRootList();
	}

	@GetMapping("/list")
	public List<SysMenuTreeVO> findList(@ModelAttribute SysMenuQueryParam param) {
		return sysMenuService.findList(param);
	}

	@GetMapping("/tree")
	public List<SysMenuTreeVO> findTree(@ModelAttribute SysMenuQueryParam param) {
		return sysMenuService.findTree(param);
	}

	@GetMapping("/get")
	public SysMenuDO get(@RequestParam Long kid) {
		return sysMenuService.getById(kid);
	}

	@PostMapping("/insert")
	public Long insert(@RequestBody SysMenuDO param) {
		return sysMenuService.insertMenu(param);
	}

	@PostMapping("/delete")
	public void delete(@RequestParam Long kid) {
		sysMenuService.deleteMenuAndChilds(kid);
	}

	@PostMapping("/move")
	public void move(@RequestBody SysMenuMoveParam param) {
		sysMenuService.moveMenu(param.getKid(), param.getTargetPid());
	}
}
