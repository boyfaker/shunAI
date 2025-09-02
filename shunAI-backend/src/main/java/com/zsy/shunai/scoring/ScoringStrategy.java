package com.zsy.shunai.scoring;

import com.zsy.shunai.model.entity.App;
import com.zsy.shunai.model.entity.UserAnswer;

import java.util.List;

public interface ScoringStrategy {

    /**
     * 执行评分
     *
     * @param choice 用户选择的答案
     * @param app    评分应用
     * @return UserAnswer
     * @throws Exception 自定义异常
     */
    UserAnswer doScore(List<String> choice, App app) throws Exception;

}
