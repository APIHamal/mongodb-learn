package com.lizhengpeng.learn.mongodblearn.original;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.connection.ClusterSettings;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Filter;

/**
 * mongodb客户端工厂
 * @author idealist
 */
public class MongoClientFactory {

    /**
     * mongodb客户端对象
     */
    private MongoClient mongoClient;

    /**
     * 初始化mongodb客户端对象
     */
    public void init(){
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
        serverAddressList.add(new ServerAddress("192.168.168.168",27017));
        serverAddressList.add(new ServerAddress("192.168.168.169",27017));
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
        MongoCredential credential = MongoCredential.createCredential("lizhengpeng","app_db","123456".toCharArray());
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
        mongoClient = MongoClients.create(builder.build());
    }

    /**
     * 列出当前的数据库对象以及集合对象
     */
    public void listInfo(){
        /**
         * 列出当前mongodb中所有的数据库对象
         */
        MongoIterable<String> databaseNames = mongoClient.listDatabaseNames();
        for(String database : databaseNames){
            System.out.println("数据库对象----->"+database);
            /**
             * 列出当前数据库对象的所有集合对象
             */
            MongoIterable<String> collections = mongoClient.getDatabase(database).listCollectionNames();
            for(String collectionName : collections){
                /**
                 * 列出当前集合索引以及数据统计相关信息
                 */
                ListIndexesIterable<Document> documents = mongoClient.getDatabase(database).getCollection(collectionName).listIndexes();
                for(Document document : documents){
                    System.out.println("索引相关信息-->"+document.toJson());
                }
                System.out.println("当前集合数据量--->"+mongoClient.getDatabase(database).getCollection(collectionName).countDocuments());
            }

        }
    }

    /**
     * 新建mongodb文档数据
     */
    public void insertTest(){
        /**
         * 获取指定的数据库对象
         */
        MongoDatabase database = mongoClient.getDatabase("app_db");
        MongoCollection infoCollection = database.getCollection("info");
        for(int index = 0;index < 100;index++) {
            Document document = new Document();
            document.append("name", "李正鹏")
                    .append("age", 27)
                    .append("sex", "男")
                    .append("年薪", "40w")
                    .append("index", index + 1);
            infoCollection.insertOne(document);
        }
    }

    /**
     * 查询指定的文档数据
     * 查询文档数据应该使用的筛选条件Filters进行数据过滤
     */
    public void searchDocument(){
        MongoDatabase database = mongoClient.getDatabase("app_db");
        MongoCollection infoCollection = database.getCollection("info");
        /**
         * 该查询条件相关说明
         * 查询数据中姓名为李正鹏并且年龄大于等于27的文档数据
         * 1、Sorts用来对查询的结果按照指定的字段进行排序操作()
         *   ascending表示按照升序进行排列
         *   descending表示按照降序进行排列
         * 注意:当需要多个字段进行排序时需要使用Sorts.orderBy方法
         * 例如:下面按照name进行升序排列按照age进行降序排列
         */
        FindIterable<Document> documents = infoCollection.find(
                Filters.and(
                        Filters.eq("name","李正鹏"),
                        Filters.gte("age",27)
                )
        ).sort(Sorts.orderBy(
                /**
                 * 按照name进行升序操作
                 * age进行降序操作
                 */
                Sorts.ascending("name"),
                Sorts.descending("age")
        )).skip(0).limit(10);
        /**
         * 打印当前已经查询出来的数据
         */
        for(Document document : documents){
            System.out.println("文档集合数据--->"+document.toJson());
        }
    }

    /**
     * 删除指定集合中的数据
     */
    public void deleteData(){
        MongoDatabase database = mongoClient.getDatabase("app_db");
        MongoCollection infoCollection = database.getCollection("info");
        /**
         * java驱动中Filters对象用来进行条件筛选
         * mongodb提供多种查询条件用来过滤数据
         * 1、{column:{$eq:'text'}}用来筛选字段与内容匹配的数据
         * 2、{column:${ne:'text'}}用来筛选字段与内容不匹配的数据
         * 3、{column:{$gt:2}}用来筛选字段大于指定值的数据
         * 4、{column:{$gte:2}}用来筛选字段大于等于指定内容的数据
         * 5、{column:{$lt:2}}用来筛选字段小于指定内容的数据
         * 6、{column:{$lte:2}}用来筛选字段小于等于内容的数据
         * 7、{array:{$in:[1,2,3]}}用来筛选数组字段中包含(只要一个即可顺序不要求一致)给出数组
         * 8、{array:${nin:[1,2,3]}}用来筛选数组字段总不包含(这里是不包含所有列出数据)给出元素的数据
         * 9、{array:{$all:[1,2,3]}}用来筛选数组中全包含给出元素(全部包含但是不要求顺序一致)的数据
         * 10、{array:{$size:10}}判断数组长度等于指定的值
         *    例如:db.info_data.find({array:{$size:6}})
         *        筛选数据中array数组长度等于6的所有文档数据
         */
        infoCollection.deleteMany(Filters.and(
                Filters.eq("name","李正鹏"),
                Filters.eq("age",27)
        ));
        /**
         * 删除所有name不等于test的数据
         */
        infoCollection.deleteMany(Filters.ne("name","test"));
        /**
         * 删除所有age小于等于27并且company不等于bank的数据
         */
        infoCollection.deleteMany(Filters.and(
                Filters.lte("age",27),
                Filters.eq("company","bank")
        ));
    }

    /**
     * 更新mongodb中集合中指定的文档数据
     * {$set:{column:value}}更新文档数据中的字段值
     * {$unset:{column:1}}删除文档数据中指定的字段
     * {$inc:{column:value}}数据中字段增加指定的值
     * {$push:{array:value}}将数据添加到字段的尾部
     * {$pushall:{array:[1,2,3]}}将数组数据添加到字段(必须是一个数组类型不存在自动创建)的尾部
     * {$pull:{array:value}}删除数组中指定的数据
     * {$poll:{array:1}}删除数组中尾部最后一个元素或者头部第一个元素
     * {$rename:{old_column:new_column}}更细数据中字段名称为一个新的名称
     */
    public void updateDocument(){
        MongoDatabase database = mongoClient.getDatabase("app_db");
        MongoCollection infoCollection = database.getCollection("info");
        /**
         * 将数据中name为李正鹏并且age大于等于27的数据全部更新
         * 1、Filters对象用来进行条件的筛选
         * 2、Updates对象用来进行条件的更新(同mongodb提供的更新语法一一对应)
         * 注意:使用Updates.combine()进行多个属性的更新
         * combine本身就是组合的意思
         */
        infoCollection.updateMany(Filters.and(
                Filters.eq("name","李正鹏"),
                Filters.gte("age",27)
        ), Updates.combine(
                /**
                 * 该更新参数表明如下的作用
                 * 更新name字段值并且加上mod后缀
                 * age字段的值在当前的基础上增加22
                 * tag字段(数组元素)中增加一个元素
                 */
                Updates.set("name","李正鹏_mod"),
                Updates.inc("age",22),
                Updates.push("tag","程序员")
        ));
    }

    /**
     * 返回当前创建的MongoClient对象
     * @return
     */
    public MongoClient mongoClient(){
        return mongoClient;
    }

    public static void main(String[] args) {
        MongoClientFactory mongoClientFactory = new MongoClientFactory();
        mongoClientFactory.init();
//        mongoClientFactory.insertTest();
//        mongoClientFactory.listInfo();
//        mongoClientFactory.deleteData();
        mongoClientFactory.updateDocument();
        mongoClientFactory.searchDocument();
    }


}
