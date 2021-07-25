package com.lizhengpeng.learn.mongodblearn.boot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * mongodb用例相关测试
 * @author idealist
 */
@RestController
public class UserInfoController {

    @Autowired
    private MongoService mongoService;

    /**
     * 新增用户数据测试
     * @return
     */
    @GetMapping("/insert")
    public String insertData(){
        mongoService.insertData();
        return "新增数据成功";
    }

    /**
     * 查询用户的数据
     */
    @GetMapping("/find")
    public List<UserInfo> getInfo(){
        return mongoService.findUserInfo();
    }

    /**
     * 更新数据操作
     * @return
     */
    @GetMapping("/update")
    public String update(){
        mongoService.updateData();
        return "更新数据操作成功";
    }

    /**
     * 删除文档集合数据
     * @return
     */
    @GetMapping("/delete")
    public String removeData(){
        mongoService.deleteData();
        return "删除文档集合数据";
    }

}
