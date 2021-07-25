package com.lizhengpeng.learn.mongodblearn.boot;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 用户相关信息
 * @author idealist
 */
@Setter
@Getter
@Document(collection = "userInfo")
public class UserInfo {

    @Id
    private String id; //数据库文档使用的主键
    private String name; //用户姓名
    /**
     * 字段进行别名的设置
     */
    @Field("user_sex")
    private String sex; //用户的性别
    private Integer age; //用户的年龄信息

}
