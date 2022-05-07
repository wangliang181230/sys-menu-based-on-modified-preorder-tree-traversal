package com.mycompany.sys.menu.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import icu.easyj.core.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 全局异常拦截器
 */
@ControllerAdvice(annotations = RestController.class) // 只拦截RestController
@Order(-1)
@ConditionalOnProperty(value = "mycompany.web.global-exception-handler.enable", matchIfMissing = true)
public class WebApiGlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(WebApiGlobalExceptionHandler.class);

	@ExceptionHandler(Throwable.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500错误
	@ResponseBody
	public Map<String, Object> handlerThrowable(Throwable t) {
		if (log.isErrorEnabled()) {
			log.error(t.getMessage(), t); // 记录error日志
		}

		Map<String, Object> result = new HashMap<>(2);
		result.put("message", t.getMessage());
		result.put("timestamp", DateUtils.toMilliseconds(new Date()));

		return result;
	}

}