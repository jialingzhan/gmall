package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class AttrValueVO extends ProductAttrValueEntity {
    //需要将setAttrValue的值改一下，因为页面传过来的是valueSelected
    public void setValueSelected(List<Object> valueSelected){
        String join = StringUtils.join(valueSelected, ",");
        this.setAttrValue(join);
    }

}
