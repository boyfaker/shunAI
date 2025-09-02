package com.zsy.shunai.scoring;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zsy.shunai.manager.AiManager;
import com.zsy.shunai.model.dto.question.QuestionContentDTO;
import com.zsy.shunai.model.dto.useranswer.QuestionAnswerDTO;
import com.zsy.shunai.model.entity.App;
import com.zsy.shunai.model.entity.Question;
import com.zsy.shunai.model.entity.UserAnswer;
import com.zsy.shunai.model.vo.QuestionVO;
import com.zsy.shunai.service.QuestionService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 自定义测评类应用评分策略
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
public class AiTestScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedissonClient redissonClient;

    // 分布式锁的key
    private static final String AI_ANSWER_LOCK = "AI_ANSWER_LOCK";





    private static final String AI_TEST_SCORING_SYSTEM_MESSAGE = "你是一位严谨的判题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "题目和用户回答的列表：格式为 [{\"title\": \"题目\",\"answer\": \"用户回答\"}]\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来对用户进行评价：\n" +
            "1. 要求：需要给出一个明确的评价结果，包括评价名称（尽量简短）和评价描述（尽量详细，大于 200 字）\n" +
            "2. 严格按照下面的 json 格式输出评价名称和评价描述\n" +
            "```\n" +
            "{\"resultName\": \"评价名称\", \"resultDesc\": \"评价描述\"}\n" +
            "```\n" +
            "3. 返回格式必须为 JSON 对象";


    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < questionContentDTOList.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            questionAnswerDTO.setUserAnswer(choices.get(i));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return userMessage.toString();
    }



    private final Cache<String, String> answerCacheMap =
            Caffeine.newBuilder().initialCapacity(1024)
                    // 缓存5分钟移除
                    .expireAfterAccess(5L, TimeUnit.MINUTES)
                    .maximumSize(10000)
                    .build();

    /**
     * 构建缓存答案
     * @param appId
     * @param choicesStr
     * @return
     */

    private String buildCacheKey(Long appId, String choicesStr) {
        return DigestUtil.md5Hex(appId + ":" + choicesStr);
    }




    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {
        Long appId = app.getId();
        String jsonStr = JSONUtil.toJsonStr(choices);
        // 构建缓存key
        String cacheKey = buildCacheKey(appId, jsonStr);
        // 查询缓存中是否有这个key
        String answerJson = answerCacheMap.getIfPresent(cacheKey);
        // 命中缓存则直接返回结果
        if (StrUtil.isNotBlank(answerJson)) {
            // 把缓存中返回的字符串反序列化为UserAnswer对象
            UserAnswer userAnswer = JSONUtil.toBean(answerJson, UserAnswer.class);
            // 缓存中只存了ai回答，其他属性需要再设置一下
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(jsonStr);
            return userAnswer;
        }

        // 如果没有缓存就要调用AI，但是如果此时大量请求请求AI，出现缓存击穿
        // 所以这里加一个分布式锁，防止缓存击穿
        // 第一个调用AI的请求拿到锁，直到调用完毕写入缓存再放开锁
        // 定义锁
        // AI_ANSWER_LOCK + cacheKey为锁的Key，即唯一标识
        RLock lock = redissonClient.getLock(AI_ANSWER_LOCK + cacheKey);
        // 分布式锁一定要用finally块，保证代码即使有问题也会释放锁
        // 防止异常导致锁无法释放（死锁）
        try {
            // 竞争分布式锁，等待 3 秒，15 秒自动释放
            // res==true表示抢到锁，false表示没抢到
            // 往redis中写入锁的key-value
            boolean res = lock.tryLock(3, 15, TimeUnit.SECONDS);
            if (res){


                // 抢到锁的业务才能执行 AI 调用
                // 1. 根据 id 查询到题目
                Question question = questionService.getOne(
                        Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
                );
                QuestionVO questionVO = QuestionVO.objToVo(question);
                List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
                // 2. 调用 AI 获取结果
                // 封装 Prompt
                String userMessage = getAiTestScoringUserMessage(app, questionContent, choices);
                // AI 生成
                String result = aiManager.doRequest(AI_TEST_SCORING_SYSTEM_MESSAGE, userMessage);
                // 结果处理
                int start = result.indexOf("{");
                int end = result.lastIndexOf("}");
                String json = result.substring(start, end + 1);

                // 缓存结果
                answerCacheMap.put(cacheKey, json);

                // 3. 构造返回值，填充答案对象的属性
                UserAnswer userAnswer = JSONUtil.toBean(json, UserAnswer.class);
                userAnswer.setAppId(appId);
                userAnswer.setAppType(app.getAppType());
                userAnswer.setScoringStrategy(app.getScoringStrategy());
                userAnswer.setChoices(jsonStr);
                return userAnswer;
            } else {
                // 没抢到锁,等锁释放从缓存中拿 轮询
                // 轮询这里阻塞了线程，高并发情况下可以使用异步 Future + Redis Key 事件监听
                // 或者Redisson Condition 或信号量
                long waitStart = System.currentTimeMillis();
                long waitTimeout = 5000; // 最多等 5 秒
                while (System.currentTimeMillis() - waitStart < waitTimeout) {
                    Thread.sleep(50); // 每 50ms 检查一次
                    String cachedAfterWait = answerCacheMap.getIfPresent(cacheKey);
                    if (StrUtil.isNotBlank(cachedAfterWait)) {
                        UserAnswer userAnswer = JSONUtil.toBean(cachedAfterWait, UserAnswer.class);
                        userAnswer.setAppId(appId);
                        userAnswer.setAppType(app.getAppType());
                        userAnswer.setScoringStrategy(app.getScoringStrategy());
                        userAnswer.setChoices(jsonStr);
                        return userAnswer;
                    }
                }
                // 超时还没拿到缓存，返回默认对象或抛异常
                return new UserAnswer(); // 或 throw new RuntimeException("AI scoring failed");

            }

        } finally {
            if (lock != null && lock.isLocked()) {
                if(lock.isHeldByCurrentThread()) {// 只有自己能释放自己的锁
                    // 往redis中删除这个key-value
                    lock.unlock();
                }
            }
        }
    }
}

