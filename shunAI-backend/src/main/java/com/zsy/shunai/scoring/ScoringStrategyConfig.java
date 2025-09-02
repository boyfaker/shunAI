package com.zsy.shunai.scoring;


import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//只能加在类上
@Target(ElementType.TYPE)
//运行时保留，可以通过反射读取注解信息
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ScoringStrategyConfig {

    /**
     * 应用类型
     *
     * @return 0-得分类 1-测评类
     */
    int appType();

    /**
     * 得分策略
     *
     * @return 0-CUSTOM自定义 1-AI
     */
    int scoringStrategy();
}
