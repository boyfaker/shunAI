package com.zsy.shunai.scoring;

import com.zsy.shunai.common.ErrorCode;
import com.zsy.shunai.exception.BusinessException;
import com.zsy.shunai.model.entity.App;
import com.zsy.shunai.model.entity.UserAnswer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ScoringStrategyExecutor {
    //注入所有的策略
    //因为所有策略类都有注解@Component，所以这里注入的是所有策略类的实例
    @Resource
    private List<ScoringStrategy> scoringStrategyList;

    /**
     * 评分
     *
     * @param choiceList 答案列表
     * @param app        应用
     * @return UserAnswer 用户答案
     * @throws Exception 异常
     */
    public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
        //获取应用类型：0-得分类，1-测评类 和评分策略：0-自定义，1-AI
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        //检验
        if (appType == null || scoringStrategy == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用类型或评分策略为空");
        }

        //根据应用类型和评分策略获取对应的策略
        //通过反射获取不同策略对应的appType和scoringStrategy
        for (ScoringStrategy strategy : scoringStrategyList) {
            if (strategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
                ScoringStrategyConfig scoringStrategyConfig = strategy.getClass().getAnnotation(ScoringStrategyConfig.class);
                if (scoringStrategyConfig.appType() == appType && scoringStrategyConfig.scoringStrategy() == scoringStrategy)
                    return strategy.doScore(choiceList, app);
            }
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "没有找到对应的评分策略");
    }
}
