package com.zsy.shunai.controller;

import com.zsy.shunai.common.BaseResponse;
import com.zsy.shunai.common.ErrorCode;
import com.zsy.shunai.common.ResultUtils;
import com.zsy.shunai.exception.ThrowUtils;
import com.zsy.shunai.mapper.UserAnswerMapper;
import com.zsy.shunai.model.dto.statistic.AppAnswerCountDTO;
import com.zsy.shunai.model.dto.statistic.AppAnswerResultCountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/app/statistic")
@Slf4j
public class AppStatisticController {
    @Resource
    private UserAnswerMapper userAnswerMapper;

    @GetMapping("/answer_count")
    public BaseResponse<List<AppAnswerCountDTO>> getAppAnswerCount() {
        return ResultUtils.success(userAnswerMapper.doAppAnswerCount());
    }


    /**
     * 应用回答分布统计
     * @param appId
     * @return
     */
    @GetMapping("/answer_result_count")
    public BaseResponse<List<AppAnswerResultCountDTO>> getAppAnswerResultCount(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userAnswerMapper.doAppAnswerResultCount(appId));
    }


}
