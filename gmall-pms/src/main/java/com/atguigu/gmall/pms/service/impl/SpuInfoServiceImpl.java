package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.AttrValueVO;
import com.atguigu.gmall.pms.vo.SkusVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import com.atguigu.gmall.sms.vo.SkuVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDao;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService descService;
    @Autowired
    private ProductAttrValueService valueService;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesService imagesService;
    @Autowired
    private SkuSaleAttrValueService attrValueService;
    @Autowired
    private GmallSmsClient smsClient;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCatId(QueryCondition condition, Long catId) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        if (catId!=0){
            wrapper.eq("catalog_id",catId);
        }
        String key = condition.getKey();
        if (StringUtils.isNotBlank(key)){
            //查询条件不为空
            wrapper.and(wrapp->wrapp.like("id",key).or().like("spu_name",key));
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(condition),
                wrapper
        );
        return new PageVo(page);
    }

    @Override
    public void bigSave(SpuInfoVO spuInfoVO) {
        //首先来存好spu有关的信息
        //pms_sku_info，插入一条数据即可
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuInfoVO,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUodateTime(spuInfoEntity.getCreateTime());
        save(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        //pms_spu_info_desc,可以直接批处理
        List<String> spuImages = spuInfoVO.getSpuImages();
        List<SpuInfoDescEntity> spuInfoDescEntities = spuImages.stream().map(s -> {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setDecript(s);
            spuInfoDescEntity.setSpuId(spuId);
            return spuInfoDescEntity;
        }).collect(Collectors.toList());
        descService.saveBatch(spuInfoDescEntities);

        //pms_product_attr_value
        List<AttrValueVO> baseAttrs = spuInfoVO.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(baseAttrValueVO -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            BeanUtils.copyProperties(baseAttrValueVO, valueEntity);
            valueEntity.setSpuId(spuId);
            valueEntity.setAttrSort(0);
            valueEntity.setQuickShow(0);
            return valueEntity;
        }).collect(Collectors.toList());
        valueService.saveBatch(productAttrValueEntities);

        //其次应该是sku的相关信息
        List<SkusVO> skus = spuInfoVO.getSkus();
        Long brandId = spuInfoVO.getBrandId();
        Long catalogId = spuInfoVO.getCatalogId();

        skus.forEach((SkusVO skusVO) -> {
            //pms_sku_info
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            BeanUtils.copyProperties(skusVO,skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
            skuInfoEntity.setBrandId(brandId);
            skuInfoEntity.setCatalogId(catalogId);
            if (!CollectionUtils.isEmpty(spuImages)){
                skuInfoEntity.setSkuDefaultImg(spuImages.get(0));
            }
            skuInfoEntity.setSkuCode(UUID.randomUUID().toString());
            skuInfoDao.insert(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();

            //pms_sku_images
            List<String> images = skusVO.getImages();
            List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                SkuImagesEntity imagesEntity = new SkuImagesEntity();
                imagesEntity.setSkuId(skuId);
                imagesEntity.setImgSort(0);
                imagesEntity.setImgUrl(image);
                imagesEntity.setDefaultImg(StringUtils.equals(image, spuImages.get(0)) ? 1 : 0);
                return imagesEntity;
            }).collect(Collectors.toList());
            imagesService.saveBatch(skuImagesEntities);

            //pms_sku_sale_attr_value
            List<SkuSaleAttrValueEntity> saleAttrs = skusVO.getSaleAttrs();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = saleAttrs.stream().map(saleAttr -> {
                SkuSaleAttrValueEntity valueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(saleAttr, valueEntity);
                valueEntity.setAttrSort(0);
                valueEntity.setSkuId(skuId);
                return valueEntity;
            }).collect(Collectors.toList());
            attrValueService.saveBatch(skuSaleAttrValueEntities);

            //最后是营销相关的信息，需要用到openfeign
            SkuVO skuVO = new SkuVO();
            BeanUtils.copyProperties(skusVO,skuVO);
            skuVO.setSku_id(skuId);
            smsClient.saveAll(skuVO);

        });


    }

}