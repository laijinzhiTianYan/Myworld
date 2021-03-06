package com.oneworld.back.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.oneworld.back.dao.MenuDao;
import com.oneworld.back.service.MenuService;
import com.oneworld.back.utils.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zhangqiao
 * @date 2019/10/11 17:18
 */
@Service
public class MenuServiceImpl implements MenuService {
    @Autowired
    private MenuDao menuDao;

    @Override
    public JSONObject getAllMenu(JSONObject jsonObject) {
        CommonUtil.fillPageParam(jsonObject);
        int count = menuDao.countMenus(jsonObject);
        List<Integer> countEach = menuDao.countPermissionEachMenu();
        List<JSONObject> list = menuDao.getMenuList(jsonObject);
        return CommonUtil.menuSuccessPage(jsonObject, list, countEach,count);
    }

    @Override
    public JSONObject deleteMenu(JSONObject jsonObject) {
        menuDao.deleteMenu(jsonObject);
        return CommonUtil.successJson();
    }

    @Override
    public JSONObject addMenu(JSONObject jsonObject) {
        jsonObject.put("requiredPermission",1);
        menuDao.addMenu(jsonObject);
        return CommonUtil.successJson();
    }

    @Override
    public JSONObject updateMenu(JSONObject jsonObject) {
        jsonObject.put("requiredPermission",1);
        menuDao.updateMenu(jsonObject);
        return CommonUtil.successJson();
    }

    @Override
    public JSONObject getUserPermission(JSONObject jsonObject) {
        JSONObject result = new JSONObject();
        List<JSONObject> menus = menuDao.getUserPermission(jsonObject);
        result.put("info",menus);
        result.put("code",100);
        result.put("msg","请求成功！");
        return result;
    }
}
