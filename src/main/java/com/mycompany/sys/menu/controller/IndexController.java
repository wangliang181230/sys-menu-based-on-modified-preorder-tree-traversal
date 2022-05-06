package com.mycompany.sys.menu.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	@GetMapping("/")
	@ApiOperation(value = "首页")
	public void index(HttpServletResponse response) throws IOException {
		response.sendRedirect("/test");
	}

	@GetMapping("/test")
	@ApiOperation(value = "测试")
	public String test() {
		return "test";
	}

	@GetMapping("/pyramid")
	@ApiOperation(value = "左右值树型数据倒金字塔图")
	public String pyramid() {
		return "pyramid";
	}

}
