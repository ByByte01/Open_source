### 数据脱敏
#### 组件集成支持
  采用Starter方式，可以集成到业务组件及其他技术组件，并且对于其他组件是透明的。
#### 主要功能：
1：支持属性脱敏
2：支持文本敏感数据脱敏
3：内置银行卡，手机号，IP，邮箱，身份证号脱敏组件
#### 可扩展性支持
当需要对应其他类型的数据进行脱敏时，只需要实现对应的接口即可
#### xml报文脱敏示例
```java 
/*
		 * XML报文脱敏
		 */
		String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
				"<!DOCTYPE note SYSTEM \"book.dtd\">\n" +
				"<book id=\"1\">\n" +
				"    <name>xml报文脱敏测试</name>\n" +
				"    <author>Cay S. Horstmann</author>\n" +
				"    <isbn lang=\"CN\">127.0.0.1</isbn>\n" +
				"    <isbn lang=\"CN\">188.0.200.1</isbn>\n" +
				"    <tags>\n" +
				"        <tag></tag>\n" +
				"        <tag>Network</tag>\n" +
				"    </tags>\n" +
				"    <pubDate/>\n" +
				"</book>";
		log.info("脱敏后：");
		log.info(sensitiveDataConverter
				.execute(new StringBuilder().append(xmlStr)).toString());
```
脱敏后的如下图
![image](https://github.com/cfjgithub/Open_source/assets/29352111/4ea05c07-132c-4fce-96f1-2c87b5eb4431)
## 关注关注我的公众号：
![WechatIMG167](https://github.com/cfjgithub/Open_source/assets/29352111/99671a37-8cb0-4b3d-ac45-e1db1d397638)
