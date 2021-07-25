package com.lizhengpeng.learn.mongodblearn.boot;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mongodb相关调用方法
 * @author idealist
 */
@Service
public class MongoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 测试新增相关对象
     */
    public void insertData(){
        for(int index = 0;index < 100;index++){
            UserInfo info = new UserInfo();
            info.setAge(index);
            info.setName("李正鹏");
            info.setSex("男");
            /**
             * SpringData中save方法同时具备更新操作与新增操作
             * 1、主键不存在时则执行更新操作
             * 2、主键存在时则执行数据更新操作(因此通常来说查询之后再做更新其实比较容易)
             */
            mongoTemplate.save(info);
        }
    }

    /**
     * 查询相关用户信息
     * @return
     */
    public List<UserInfo> findUserInfo(){
        /**
         * Mongodb整合后Query表示查询条件
         * Criteria类似Java驱动中的Filters对象
         */
        Query query = new Query();
        /**
         * Criteria代表的时相关查询条件
         * and表示与查询条件
         * {$and:[criteria1,criteria2]}
         * {$or:[criteria1,criteria2]}
         */
        Criteria criteriaAnd = new Criteria();
        /**
         * 1、andOperator可同时传入多个Criteria对象来构建{$and:array}数组查询条件
         * 2、orOperator可同时传入多个Criteria对象来构建{$or:array}数组查询条件
         */
        criteriaAnd.andOperator(Criteria.where("age").gte(20),Criteria.where("user_sex").is("男"));
        query.addCriteria(criteriaAnd);
        /**
         * 该查询条件表示查询age大于等于50并且user_sex等于男的文档数据
         * 并且使用age进行降序排列操作
         * Sort.by(Direction,property).and(Sort)
         * 该方法用于按照多个字段进行排序的操作
         */
        query.with(Sort.by(Sort.Direction.ASC,"name").and(Sort.by(Sort.Direction.DESC,"age")));
        /**
         * 对当前数据进行的分页查询操作
         */
        query.with(PageRequest.of(0,10));
        /**
         * 查询集合数据并且返回相关数据
         */
        List<UserInfo> infoList = mongoTemplate.find(query,UserInfo.class);
        return infoList;
    }

    /**
     * 更新数据消息
     * 1、当对象的ID存在时(ObjectId)执行MongoTemplate对象的save方法也可以执行更新操作({$set:{column,value}})
     * 2、直接使用查询语法进行更新操作(相当于Filters,Updates)
     */
    public void updateData(){
        /**
         * 执行查询后直接更新操作
         */
        Query query = new Query();
        Criteria filterCriteria = new Criteria();
        filterCriteria.andOperator(
                Criteria.where("name").is("李正鹏"),
                Criteria.where("age").lte(10)
        );
        query.addCriteria(filterCriteria);
        /**
         * 更新数据使用Update对象进行更新操作
         * {$set:{column:value}} 更新文档属性
         * {$unset:{column:1}} 删除文档的属性
         * {$inc:{column:value}} 增加文档属性指定的值
         * {$push:{array:value}} 将对象添加到数组中
         * {$pushall:{array:[1,2,3,4]}} 将数组全部添加到集合中
         * {$pull:{array:value}} 将指定的数据从数组中删除
         * ${pop:{array:1}} 从数组的头部或者尾部删除数据
         * {$rename:{old_column,new_column}} 更新文档的属性
         * SpringData Mongodb中Update对象(原生使用Updates对象)包含以上方法
         */
        Update update = new Update();
        update.set("name","李正鹏_mod");
        update.inc("age",100);
        update.rename("user_sex","sex");
        /**
         * 进行数组的更新操作
         */
        mongoTemplate.updateMulti(query,update,UserInfo.class);
    }

    /**
     * 删除指定的数据
     * 1、Query进行查询条件的构建
     * 2、Criteria进行具体条件的约束(Criteria.where(column).is[is相当于原生的{column:{$eq:value}}的用法(即判断是否相等)])
     */
    public void deleteData(){
        /**
         * 构建查询条件
         *
         */
        Query documentFilter = new Query();
        Criteria filter = new Criteria();
        filter.andOperator(Criteria.where("name").is("李正鹏"),Criteria.where("age").gte(12));
        documentFilter.addCriteria(filter);
        /**
         * 进行多条件数据的删除操作
         * 在已经获取了Document(PO)对象的情况下也可以使用remove(PO)对象进行数据的删除操作
         */
        mongoTemplate.remove(documentFilter,UserInfo.class);
    }

}
