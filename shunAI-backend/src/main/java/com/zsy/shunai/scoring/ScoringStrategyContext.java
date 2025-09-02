package com.zsy.shunai.scoring;

import com.zsy.shunai.common.ErrorCode;
import com.zsy.shunai.exception.BusinessException;
import com.zsy.shunai.model.entity.App;
import com.zsy.shunai.model.entity.UserAnswer;
import com.zsy.shunai.model.enums.AppTypeEnum;
import com.zsy.shunai.model.enums.ScoringStrategyEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

import static com.zsy.shunai.model.enums.ScoringStrategyEnum.AI;
import static com.zsy.shunai.model.enums.ScoringStrategyEnum.CUSTOM;

@Service
@Deprecated
public class ScoringStrategyContext {

    @Resource
    private CustomScoreScoringStrategy customScoreScoringStrategy;

    @Resource
    private CustomTestScoringStrategy customTestScoringStrategy;

    /**
     * 评分
     *
     * @param choiceList
     * @param app
     * @return
     * @throws Exception
     */
    public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
        AppTypeEnum appTypeEnum = AppTypeEnum.getEnumByValue(app.getAppType());
        ScoringStrategyEnum scoringStrategyEnum = ScoringStrategyEnum.getEnumByValue(app.getScoringStrategy());
        if (appTypeEnum == null || scoringStrategyEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
        }
        // 根据不同的应用类别和评分策略，选择对应的策略执行
        switch (appTypeEnum) {
            case SCORE:
                switch (scoringStrategyEnum) {
                    case CUSTOM:
                        return customScoreScoringStrategy.doScore(choiceList, app);
                    case AI:
                        break;
                }
                break;
            case TEST:
                switch (scoringStrategyEnum) {
                    case CUSTOM:
                        return customTestScoringStrategy.doScore(choiceList, app);
                    case AI:
                        break;
                }
                break;
        }
        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
    }
}
