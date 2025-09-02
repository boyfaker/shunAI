package com.zsy.shunai.model.dto.question;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionContentDTO {
    private String title;
    private List<Option> options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private String result;  // 测评类答案属性
        private int score;      // 得分类题目分数
        private String value;   // 选项内容
        private String key;     // 选项标识（如 A/B/C）
    }
}