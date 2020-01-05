package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkusVO extends SkuInfoEntity{
    private List<String> images;
    //积分
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;
    //满减
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;
    //打折
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

    private List<SkuSaleAttrValueEntity> saleAttrs;
}
