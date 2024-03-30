package com.fajianchen.log;


import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class SensitiveTest {

    private static Logger logger = LoggerFactory.getLogger(SensitiveTest.class);

    @Test
    public void shouldAnswerWithTrue() {

        com.alibaba.fastjson2.JSONObject jsonObject = new JSONObject();

        TestUser testUser = new TestUser();
        testUser.setIdCard("6222600250013173553"); //银行卡
        testUser.setNumberCard("340825198702264517"); //身份证
        testUser.setIP("127.0.0.1");

        TestUser testUser1 = new TestUser();
        testUser1.setIdCard("6222600250013173553"); //银行卡
        testUser1.setNumberCard("340825198702264517"); //身份证
        testUser1.setIP("127.0.0.1");
        List<TestUser> arrayList = Arrays.asList(testUser, testUser1);
        /**
         * 集合脱敏
         */
        logger.info(JSONObject.toJSONString(arrayList));
        /**
         * XML报文脱敏
         */
        String str="<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!DOCTYPE note SYSTEM \"book.dtd\">\n" +
                "<book id=\"1\">\n" +
                "    <name>xml报文脱敏测试</name>\n" +
                "    <author>Cay S. Horstmann</author>\n" +
                "    <isbn lang=\"CN\">127.0.0.1</isbn>\n" +
                "    <sfz sfz=\"CN\">6222600250013173553</sfz>\n" +
                "    <tags>\n" +
                "        <tag></tag>\n" +
                "        <tag>Network</tag>\n" +
                "    </tags>\n" +
                "    <pubDate/>\n" +
                "</book>";
        logger.info(str);
       // logger.info(JSONObject.toJSONString(arrayList));

    }
}
