package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;

import java.util.List;


/**
 * 商品三级分类
 *
 * @author zhangjialing
 * @email zjl@atguigu.com
 * @date 2020-01-01 14:00:29
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageVo queryPage(QueryCondition params);

    List<CategoryEntity> queryCategory(Integer level, Long parentCid);
}

