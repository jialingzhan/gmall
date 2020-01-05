package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.AttrAttrgroupRelationDao;
import com.atguigu.gmall.pms.dao.AttrDao;
import com.atguigu.gmall.pms.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.vo.AttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.AttrGroupDao;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrGroupDao attrGroupDao;
    @Autowired
    private AttrAttrgroupRelationDao relationDao;
    @Autowired
    private AttrDao attrDao;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo queryByCidPage(Long cid, QueryCondition condition) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(condition),
                new QueryWrapper<AttrGroupEntity>().eq("catelog_id",cid)
        );

        return new PageVo(page);
    }

    @Override
    public AttrGroupVo queryById(Long gid) {
        //查询分组
        AttrGroupVo attrGroupVo = new AttrGroupVo();
        AttrGroupEntity entity = attrGroupDao.selectById(gid);//需要取出attr_group_id
        BeanUtils.copyProperties(entity,attrGroupVo);//赋值给这些属性，我觉得大概全都是名字

        //查询分组下的关联关系
        List<AttrAttrgroupRelationEntity> relations = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", gid));

        //判断关联关系是否为空，如果为空，直接返回
        if (CollectionUtils.isEmpty(relations)){
            return attrGroupVo;
        }
        attrGroupVo.setRelations(relations);

        //查询分组下的所有规格的id
        List<Long> collect = relations.stream().map(relation -> relation.getAttrId()).collect(Collectors.toList());
        //查询分组下的所有规格参数
        List<AttrEntity> entityList = attrDao.selectBatchIds(collect);
        attrGroupVo.setAttrEntities(entityList);

        return attrGroupVo;
    }

    @Override
    public List<AttrGroupVo> querySpuByCid(Long cid) {
        //先求出gid
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", cid));
        List<AttrGroupVo> attrGroupVos = attrGroupEntities.stream().map(attrGroupEntity -> {
            AttrGroupVo attrGroupVo = queryById(attrGroupEntity.getAttrGroupId());
            return attrGroupVo;
        }).collect(Collectors.toList());
        return attrGroupVos;
    }

}