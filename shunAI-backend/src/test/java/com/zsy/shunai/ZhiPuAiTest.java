//package com.zsy.shunai;
//
//import ai.z.openapi.ZhipuAiClient;
//import ai.z.openapi.service.model.ModelData;
//import cn.hutool.core.util.StrUtil;
//import com.zsy.shunai.manager.AiManager;
//import com.zsy.shunai.model.entity.App;
//import com.zsy.shunai.model.enums.AppTypeEnum;
//import com.zsy.shunai.service.AppService;
//import io.reactivex.Flowable;
//import io.reactivex.schedulers.Schedulers;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@SpringBootTest
//class ZhiPuAiTest {
//
//    @Resource
//    ZhipuAiClient zhipuAiClient;
//
//    @Resource
//    AiManager aiManager;
//
//    @Resource
//    AppService appService;
//
//    private static final String GENERATE_QUESTION_SYSTEM_MESSAGE = "你是一位严谨的出题专家，我会给你如下信息：\n" +
//            "```\n" +
//            "应用名称，\n" +
//            "【【【应用描述】】】，\n" +
//            "应用类别，\n" +
//            "要生成的题目数，\n" +
//            "每个题目的选项数\n" +
//            "```\n" +
//            "\n" +
//            "请你根据上述信息，按照以下步骤来出题：\n" +
//            "1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复\n" +
//            "2. 严格按照下面的 json 格式输出题目和选项\n" +
//            "```\n" +
//            "[{\"options\":[{\"value\":\"选项内容\",\"key\":\"A\"},{\"value\":\"\",\"key\":\"B\"}],\"title\":\"题目标题\"}]\n" +
//            "```\n" +
//            "title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容\n" +
//            "3. 检查题目是否包含序号，若包含序号则去除序号\n" +
//            "4. 返回的题目列表格式必须为 JSON 数组";
//
//    /**
//     * @param app
//     * @param questionNumber
//     * @param optionNumber
//     * @return
//     */
//    private String getGenerateQuestionUserMessage(App app, int questionNumber, int optionNumber) {
//
//        StringBuilder userMessage = new StringBuilder();
//        userMessage.append(app.getAppName()).append("\n")
//                .append(app.getAppDesc()).append("\n")
//                .append(AppTypeEnum.getEnumByValue(app.getAppType()).getText() + "类").append("\n")
//                .append(questionNumber).append("\n")
//                .append(optionNumber);
//        return userMessage.toString();
//    }
//
//
//    @Test
//    public void test() {
//
//        App app = appService.getById(1);
//        String userMessage = getGenerateQuestionUserMessage(app,10,2);
//        // 封装 Prompt
//        // 发送请求
//        // contentBuilder 用于拼接返回内容
//        //StringBuilder contentBuilder = new StringBuilder();
//        // flag用于括号匹配 { => flag++  } => flag--
//        //AtomicInteger flag = new AtomicInteger(0);
//        Flowable<ModelData> modelDataFlowable = aiManager.doStreamRequest(GENERATE_QUESTION_SYSTEM_MESSAGE, userMessage);
//        // 订阅并打印结果
//        modelDataFlowable
//                .blockingForEach(modelData -> {
//                    System.out.println("收到流式数据: " + modelData);
//                });
//
//        //        modelDataFlowable// 异步线程池执行
////                // RxJava的IO线程池
////                .observeOn(Schedulers.io())
////                .doOnNext(c -> System.out.println("原始数据: " + c))
////                .blockingSubscribe();
//                // 变换操作符 map()里面是变化函数
//                // 把Flowable<ModelData>转成String
////                .map(chunk -> chunk.getChoices().get(0).getDelta().getContent())
////                .filter(Objects::nonNull)
////                .doOnNext(s -> System.out.println("map之后: " + s))
////                // 替换掉空格
////                .map(message -> message.replaceAll("\\s", ""))
////                // 过滤掉空字符串
////                .filter(StrUtil::isNotBlank)
////                .doOnNext(s -> System.out.println("filter之后: " + s))
////                .flatMap(message -> {// 将字符串转换为 List<Character>
////                    List<Character> charList = new ArrayList<>();
////                    for (char c : message.toCharArray()) {
////                        charList.add(c);
////                    }
////                        return Flowable.fromIterable(charList);
////                    })
////                .doOnNext(s -> System.out.println("flatMap之后: " + s))
////                .doOnNext(c -> {
////                    {
////                        // 识别第一个 [ 表示开始 AI 传输 json 数据，打开 flag 开始拼接 json 数组
////                        if (c == '{') {
////                            flag.addAndGet(1);
////                        }
////                        if (flag.get() > 0) {
////                            contentBuilder.append(c);
////                        }
////                        if (c == '}') {
////                            flag.addAndGet(-1);
////                            if (flag.get() == 0) {
////                                // 累积单套题目满足 json 格式后，sse 推送至前端
////                                // sse 需要压缩成当行 json，sse 无法识别换行
////                                System.out.println(contentBuilder.toString());
////                                // 清空 StringBuilder
////                                contentBuilder.setLength(0);
////                            }
////                        }
////                    }
////                }).blockingSubscribe();
//    }
//}
