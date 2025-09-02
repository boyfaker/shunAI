package com.zsy.shunai.model.vo;

import cn.hutool.json.JSONUtil;
import com.zsy.shunai.model.entity.UserAnswer;
import com.zsy.shunai.model.vo.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户答案视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class UserAnswerVO implements Serializable {

    /**
     * id
     */
    private Long id;
    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 应用类型（0-得分类，1-角色测评类）
     */
    private Integer appType;

    /**
     * 评分策略（0-自定义，1-AI）
     */
    private Integer scoringStrategy;

    /**
     * 用户答案（JSON 数组）
     */
    private List<String> choices;

    /**
     * 评分结果 id
     */
    private Long resultId;

    /**
     * 结果名称，如物流师
     */
    private String resultName;

    /**
     * 结果描述
     */
    private String resultDesc;

    /**
     * 结果图标
     */
    private String resultPicture;

    /**
     * 得分
     */
    private Integer resultScore;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param useranswerVO
     * @return
     */
    public static UserAnswer voToObj(UserAnswerVO useranswerVO) {
        if (useranswerVO == null) {
            return null;
        }
        UserAnswer useranswer = new UserAnswer();
        BeanUtils.copyProperties(useranswerVO, useranswer);
        useranswer.setChoices(JSONUtil.toJsonStr(useranswerVO.getChoices()));
        return useranswer;
    }

    /**
     * 对象转封装类
     *
     * @param useranswer
     * @return
     */
    public static UserAnswerVO objToVo(UserAnswer useranswer) {
        if (useranswer == null) {
            return null;
        }
        UserAnswerVO useranswerVO = new UserAnswerVO();
        BeanUtils.copyProperties(useranswer, useranswerVO);
        useranswerVO.setChoices(JSONUtil.toList(useranswer.getChoices(), String.class));
        return useranswerVO;
    }
}
