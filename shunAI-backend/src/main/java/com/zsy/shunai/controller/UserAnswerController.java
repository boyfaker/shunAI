package com.zsy.shunai.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zsy.shunai.annotation.AuthCheck;
import com.zsy.shunai.common.BaseResponse;
import com.zsy.shunai.common.DeleteRequest;
import com.zsy.shunai.common.ErrorCode;
import com.zsy.shunai.common.ResultUtils;
import com.zsy.shunai.constant.UserConstant;
import com.zsy.shunai.exception.BusinessException;
import com.zsy.shunai.exception.ThrowUtils;
import com.zsy.shunai.model.dto.useranswer.UserAnswerAddRequest;
import com.zsy.shunai.model.dto.useranswer.UserAnswerEditRequest;
import com.zsy.shunai.model.dto.useranswer.UserAnswerQueryRequest;
import com.zsy.shunai.model.dto.useranswer.UserAnswerUpdateRequest;
import com.zsy.shunai.model.entity.App;
import com.zsy.shunai.model.entity.UserAnswer;
import com.zsy.shunai.model.entity.User;
import com.zsy.shunai.model.enums.ReviewStatusEnum;
import com.zsy.shunai.model.vo.UserAnswerVO;
import com.zsy.shunai.scoring.ScoringStrategyExecutor;
import com.zsy.shunai.service.AppService;
import com.zsy.shunai.service.UserAnswerService;
import com.zsy.shunai.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户答案接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/userAnswer")
@Slf4j
public class UserAnswerController {

    @Resource
    private UserAnswerService useranswerService;

    @Resource
    private UserService userService;

    @Resource
    private ScoringStrategyExecutor scoringStrategyExecutor;

    @Resource
    private AppService appService;



    // region 增删改查

    /**
     * 创建用户答案
     *
     * @param useranswerAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserAnswer(@RequestBody UserAnswerAddRequest useranswerAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(useranswerAddRequest == null, ErrorCode.PARAMS_ERROR);
        //  在此处将实体类和 DTO 进行转换
        UserAnswer useranswer = new UserAnswer();
        BeanUtils.copyProperties(useranswerAddRequest, useranswer);

        //取出appId，找到app
        Long appId = useranswer.getAppId();
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 如果应用未审核，不能做题
        if (!ReviewStatusEnum.PASS.equals(ReviewStatusEnum.getEnumByValue(app.getReviewStatus()))) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "应用未审核，不能做题");
        }
        //UserAnswerAddRequest
        //private List<String> choices;

        //UserAnswer
        //private String choices;

        //  将 List<String> 转换为 String
        List<String> choices = useranswerAddRequest.getChoices();
        useranswer.setChoices(JSONUtil.toJsonStr(choices));

        // 数据校验
        useranswerService.validUserAnswer(useranswer, true);
        //  填充默认值
        User loginUser = userService.getLoginUser(request);
        useranswer.setUserId(loginUser.getId());
        //这里useranswer包含的属性只有：choices、appId、userId
        //计算得分后还要再加上很多结果字段
        //而doScore(List<String> choice, App app)要传入的参数就从上面的useranswer里拿

        // 写入数据库
        try {
            boolean result = useranswerService.save(useranswer);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        } catch (DuplicateKeyException e) {
            // ignore error
        }
        // 返回新写入的数据 id
        long newUserAnswerId = useranswer.getId();

        try {
            UserAnswer userAnswerWithResult = scoringStrategyExecutor.doScore(choices, app);
            userAnswerWithResult.setId(newUserAnswerId);
            userAnswerWithResult.setAppId(null);
            useranswerService.updateById(userAnswerWithResult);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "评分失败");
        }


        return ResultUtils.success(newUserAnswerId);
    }

    /**
     * 删除用户答案
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserAnswer(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserAnswer oldUserAnswer = useranswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserAnswer.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = useranswerService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户答案（仅管理员可用）
     *
     * @param useranswerUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserAnswer(@RequestBody UserAnswerUpdateRequest useranswerUpdateRequest) {
        if (useranswerUpdateRequest == null || useranswerUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //  在此处将实体类和 DTO 进行转换
        UserAnswer useranswer = new UserAnswer();
        BeanUtils.copyProperties(useranswerUpdateRequest, useranswer);

        List<String> choices = useranswerUpdateRequest.getChoices();
        useranswer.setChoices(JSONUtil.toJsonStr(choices));
        // 数据校验
        useranswerService.validUserAnswer(useranswer, false);
        // 判断是否存在
        long id = useranswerUpdateRequest.getId();
        UserAnswer oldUserAnswer = useranswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = useranswerService.updateById(useranswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户答案（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserAnswerVO> getUserAnswerVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        UserAnswer useranswer = useranswerService.getById(id);
        ThrowUtils.throwIf(useranswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(useranswerService.getUserAnswerVO(useranswer, request));
    }

    /**
     * 分页获取用户答案列表（仅管理员可用）
     *
     * @param useranswerQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserAnswer>> listUserAnswerByPage(@RequestBody UserAnswerQueryRequest useranswerQueryRequest) {
        long current = useranswerQueryRequest.getCurrent();
        long size = useranswerQueryRequest.getPageSize();
        // 查询数据库
        Page<UserAnswer> useranswerPage = useranswerService.page(new Page<>(current, size),
                useranswerService.getQueryWrapper(useranswerQueryRequest));
        return ResultUtils.success(useranswerPage);
    }

    /**
     * 分页获取用户答案列表（封装类）
     *
     * @param useranswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest useranswerQueryRequest,
                                                               HttpServletRequest request) {
        long current = useranswerQueryRequest.getCurrent();
        long size = useranswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> useranswerPage = useranswerService.page(new Page<>(current, size),
                useranswerService.getQueryWrapper(useranswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(useranswerService.getUserAnswerVOPage(useranswerPage, request));
    }

    /**
     * 分页获取当前登录用户创建的用户答案列表
     *
     * @param useranswerQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserAnswerVO>> listMyUserAnswerVOByPage(@RequestBody UserAnswerQueryRequest useranswerQueryRequest,
                                                                     HttpServletRequest request) {
        ThrowUtils.throwIf(useranswerQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        useranswerQueryRequest.setUserId(loginUser.getId());
        long current = useranswerQueryRequest.getCurrent();
        long size = useranswerQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserAnswer> useranswerPage = useranswerService.page(new Page<>(current, size),
                useranswerService.getQueryWrapper(useranswerQueryRequest));
        // 获取封装类
        return ResultUtils.success(useranswerService.getUserAnswerVOPage(useranswerPage, request));
    }

    /**
     * 编辑用户答案（给用户使用）
     *
     * @param useranswerEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserAnswer(@RequestBody UserAnswerEditRequest useranswerEditRequest, HttpServletRequest request) {
        if (useranswerEditRequest == null || useranswerEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //  在此处将实体类和 DTO 进行转换
        UserAnswer useranswer = new UserAnswer();
        BeanUtils.copyProperties(useranswerEditRequest, useranswer);

        List<String> choices = useranswerEditRequest.getChoices();
        useranswer.setChoices(JSONUtil.toJsonStr(choices));
        // 数据校验
        useranswerService.validUserAnswer(useranswer, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = useranswerEditRequest.getId();
        UserAnswer oldUserAnswer = useranswerService.getById(id);
        ThrowUtils.throwIf(oldUserAnswer == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUserAnswer.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = useranswerService.updateById(useranswer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion

    // 生成id 用于数据库唯一索引 -- 幂等设计
    @GetMapping("/generate/id")
    public BaseResponse<Long> generateUserAnswerId() {
        return ResultUtils.success(IdUtil.getSnowflakeNextId());
    }

}
