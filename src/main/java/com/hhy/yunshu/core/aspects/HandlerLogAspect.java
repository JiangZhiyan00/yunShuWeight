package com.hhy.yunshu.core.aspects;

import com.xxl.job.core.context.XxlJobHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HandlerLogAspect {

    Logger logger = LoggerFactory.getLogger(HandlerLogAspect.class);

    /**
     * 执行器handler的运行前后日志切面
     */
    @Around(value = "@annotation(com.xxl.job.core.handler.annotation.XxlJob)")
    public void aroundHandle(ProceedingJoinPoint pjp) throws Throwable {
        logger.info("init......");
        XxlJobHelper.log("---------------------------------程序开始---------------------------------");
        pjp.proceed();
        XxlJobHelper.log("---------------------------------程序结束---------------------------------");
        logger.info("destroy......");
    }
}
