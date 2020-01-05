package com.atguigu.gmall.sms.service;

import com.atguigu.gmall.sms.vo.SkuVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.sms.entity.SpuBoundsEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 商品spu积分设置
 *
 * @author zhangjialing
 * @email zjl@atguigu.com
 * @date 2020-01-05 12:56:59
 */
public interface SpuBoundsService extends IService<SpuBoundsEntity> {

    PageVo queryPage(QueryCondition params);

}

