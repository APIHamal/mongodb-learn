package com.lizhengpeng.learn.mongodblearn.boot;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 增加mongodb配置对象
 * @author idealist
 */
@Configuration
public class MongoConfiguration {

    @Bean
    public MongoClient mongoClient(MongoProperties mongoProperties){
        /**
         * mongodb构造器对象
         */
        MongoClientSettings.Builder builder = MongoClientSettings.builder();
        /**
         * 添加mongodb服务器地址
         * 不需要全部进行配置(搭建副本集情况下客户端会自动找出所有的副本集对象)
         * 因此通常只需要部分服务器的地址信息即可
         */
        List<ServerAddress> serverAddressList = new ArrayList<>();
        String[] hostArray = mongoProperties.getHost().split(",");
        for(String host : hostArray){
            serverAddressList.add(new ServerAddress(host,mongoProperties.getPort()));
        }
        /**
         * 配置mongodb服务器地址对象
         */
        builder.applyToClusterSettings(settingBuilder -> settingBuilder.hosts(serverAddressList));
        /**
         * 设置mongodb的认证和鉴权信息
         * 1、参数代表当前认证用户的用户名
         * 2、代表认证的数据库对象
         * 3、代表当前认证用户的密码
         */
        MongoCredential credential = MongoCredential.createCredential(mongoProperties.getUsername(),mongoProperties.getDatabase(),mongoProperties.getPassword());
        builder.credential(credential);
        /**
         * 数据库连接池相关参数的设置
         * 注意:mongodb中连接池的设计与传统的数据库驱动(dbcp、c3p0等有区别)
         * mongodb连接池中缓存的连接对于客户端开发(开发人员来说)并不可见
         * API进行增删改查等操作时会自动的从连接池中获取一个连接然后进行
         * 相关操作并且在相关操作结束后自动归还给mongodb的连接池对象
         *
         */
        builder.applyToConnectionPoolSettings(poolBuilder->{
            /**
             * 连接池中保存的最小连接数
             */
            poolBuilder.minSize(1);
            /**
             * 连接池中的最大连接数
             */
            poolBuilder.maxSize(1);
            /**
             * 连接的最大空闲时间
             */
            poolBuilder.maxConnectionIdleTime(60, TimeUnit.SECONDS);
            /**
             * 连接的最大使用生命周期
             */
            poolBuilder.maxConnectionLifeTime(10,TimeUnit.MINUTES);
        });
        /**
         * 设置数据库连接的相关超时时间
         */
        builder.applyToSocketSettings(socketBuilder->{
            /**
             * TCP建立连接的超时时间
             */
            socketBuilder.connectTimeout(5,TimeUnit.SECONDS);
            /**
             * socket读取的超时时间
             */
            socketBuilder.readTimeout(10,TimeUnit.SECONDS);
        });
        /**
         * 设置当前客户端的读偏好
         * 有线读取跟随者节点数据
         */
        builder.readPreference(ReadPreference.secondary());
        /**
         * 构建出MongoClient对象
         */
        MongoClient mongoClient = MongoClients.create(builder.build());
        return mongoClient;
    }

}
