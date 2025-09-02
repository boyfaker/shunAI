package com.zsy.shunai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zsy.shunai.common.ErrorCode;
import com.zsy.shunai.constant.CommonConstant;
import com.zsy.shunai.exception.ThrowUtils;
import com.zsy.shunai.mapper.UserAnswerMapper;
import com.zsy.shunai.model.dto.useranswer.UserAnswerQueryRequest;
import com.zsy.shunai.model.entity.App;
import com.zsy.shunai.model.entity.UserAnswer;
import com.zsy.shunai.model.vo.UserAnswerVO;
import com.zsy.shunai.model.entity.User;
import com.zsy.shunai.model.vo.UserVO;
import com.zsy.shunai.service.UserAnswerService;
import com.zsy.shunai.service.UserService;
import com.zsy.shunai.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户答案服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Service
@Slf4j
public class UserAnswerServiceImpl extends ServiceImpl<UserAnswerMapper, UserAnswer> implements UserAnswerService {

    @Resource
    private UserService userService;

    @Resource
    private AppServiceImpl appService;

    /**
     * 校验数据
     *
     * @param useranswer
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validUserAnswer(UserAnswer useranswer, boolean add) {
        ThrowUtils.throwIf(useranswer == null, ErrorCode.PARAMS_ERROR);
        //  从对象中取值
        Long appId = useranswer.getAppId();
        Long id = useranswer.getId();


        // 创建数据时，参数不能为空
        if (add) {
            //  补充校验规则
            ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "appId 非法");
            ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "id 非法");
        }

        //修改时要确定app是存在的,从数据库中取出app的数据，校验是否存在
        if (appId != null && appId > 0) {
            App app = appService.getById(appId);
            ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR, "应用不存在");
        }
    }


    /**
     * 获取查询条件
     *
     * @param useranswerQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserAnswer> getQueryWrapper(UserAnswerQueryRequest useranswerQueryRequest) {
        QueryWrapper<UserAnswer> queryWrapper = new QueryWrapper<>();
        if (useranswerQueryRequest == null) {
            return queryWrapper;
        }
        Long id = useranswerQueryRequest.getId();
        Long notId = useranswerQueryRequest.getNotId();
        Long appId = useranswerQueryRequest.getAppId();
        Integer appType = useranswerQueryRequest.getAppType();
        Integer scoringStrategy = useranswerQueryRequest.getScoringStrategy();
        String choices = useranswerQueryRequest.getChoices();
        Long resultId = useranswerQueryRequest.getResultId();
        String resultName = useranswerQueryRequest.getResultName();
        String resultDesc = useranswerQueryRequest.getResultDesc();
        Integer resultScore = useranswerQueryRequest.getResultScore();
        Long userId = useranswerQueryRequest.getUserId();
        String searchText = useranswerQueryRequest.getSearchText();
        String sortField = useranswerQueryRequest.getSortField();
        String sortOrder = useranswerQueryRequest.getSortOrder();

        //  补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("resultName", searchText).or().like("resultDesc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(resultName), "resultName", resultName);
        queryWrapper.like(StringUtils.isNotBlank(resultDesc), "resultDesc", resultDesc);
        queryWrapper.like(StringUtils.isNotBlank(choices), "choices", choices);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultId), "resultId", resultId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(resultScore), "resultScore", resultScore);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取用户答案封装
     *
     * @param useranswer
     * @param request
     * @return
     */
    @Override
    public UserAnswerVO getUserAnswerVO(UserAnswer useranswer, HttpServletRequest request) {
        // 对象转封装类
        UserAnswerVO useranswerVO = UserAnswerVO.objToVo(useranswer);

        //  可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = useranswer.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        useranswerVO.setUser(userVO);
        // endregion

        return useranswerVO;
    }

    /**
     * 分页获取用户答案封装
     *
     * @param useranswerPage
     * @param request
     * @return
     */
    @Override
    public Page<UserAnswerVO> getUserAnswerVOPage(Page<UserAnswer> useranswerPage, HttpServletRequest request) {
        List<UserAnswer> useranswerList = useranswerPage.getRecords();
        Page<UserAnswerVO> useranswerVOPage = new Page<>(useranswerPage.getCurrent(), useranswerPage.getSize(), useranswerPage.getTotal());
        if (CollUtil.isEmpty(useranswerList)) {
            return useranswerVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserAnswerVO> useranswerVOList = useranswerList.stream().map(useranswer -> {
            return UserAnswerVO.objToVo(useranswer);
        }).collect(Collectors.toList());

        //  可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = useranswerList.stream().map(UserAnswer::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        useranswerVOList.forEach(useranswerVO -> {
            Long userId = useranswerVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            useranswerVO.setUser(userService.getUserVO(user));
        });
        // endregion

        useranswerVOPage.setRecords(useranswerVOList);
        return useranswerVOPage;
    }

}
