package com.zsy.shunai.model.vo;

import cn.hutool.json.JSONUtil;
import com.zsy.shunai.model.dto.question.QuestionContentDTO;
import com.zsy.shunai.model.entity.Question;
import com.zsy.shunai.model.vo.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 问题视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@Data
public class QuestionVO implements Serializable {

    /**
     * id
     */
    private Long id;


    /**
     * 题目内容（json格式）
     */
    private List<QuestionContentDTO> questionContent;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 创建用户 id
     */
    private Long userId;


    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        //将QuestionVO中与Question同名的字段自动复制，然后单独处理特殊字段的转换
        BeanUtils.copyProperties(questionVO, question);
        //处理JSON格式的题目内容
        question.setQuestionContent(JSONUtil.toJsonStr(questionVO.getQuestionContent()));
        return question;
    }

    /**
     * 对象转封装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        //处理JSON格式的题目内容
        String questionContent1 = question.getQuestionContent();
        if (questionContent1 != null) {
            questionVO.setQuestionContent(JSONUtil.toList(question.getQuestionContent(), QuestionContentDTO.class));

        }
        return questionVO;
    }
}
