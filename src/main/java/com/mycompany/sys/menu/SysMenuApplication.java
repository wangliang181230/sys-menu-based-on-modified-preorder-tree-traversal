package com.mycompany.sys.menu;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.ApiOperation;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
public class SysMenuApplication {

	public static void main(String[] args) {
		SpringApplication.run(SysMenuApplication.class, args);
	}


	@GetMapping("/")
	@ApiOperation(value = "首页", hidden = true)
	public void index(HttpServletResponse response) throws IOException {
		response.sendRedirect("/swagger-ui.html");
	}

}
