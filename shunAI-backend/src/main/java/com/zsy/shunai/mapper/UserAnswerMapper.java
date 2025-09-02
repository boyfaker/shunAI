package com.zsy.shunai.mapper;

import com.zsy.shunai.model.dto.statistic.AppAnswerCountDTO;
import com.zsy.shunai.model.dto.statistic.AppAnswerResultCountDTO;
import com.zsy.shunai.model.entity.UserAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author ZhuanZ1
* @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
* @createDate 2025-06-23 14:38:25
* @Entity generator.domain.UserAnswer
*/
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    @Select("select appId, count(userId) as answerCount from user_answer " +
            "group by appId order by answerCount desc")
    List<AppAnswerCountDTO> doAppAnswerCount();

    @Select("select resultName, count(resultName) as resultCount from user_answer " +
            "where appId = #{appId} group by resultName order by resultCount desc")
    List<AppAnswerResultCountDTO> doAppAnswerResultCount(Long appId);
}




