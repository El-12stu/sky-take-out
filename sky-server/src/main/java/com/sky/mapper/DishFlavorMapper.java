package com.sky.mapper;


import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品ID删除口味表关联数据
     * @param id
     *
     */
    void deleteByDishId(Long id);

    /**
     * 根据菜品ID集合删除口味表关联数据
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);

    /**
     * 根据菜品ID查询口味表数据集合
     * @param dishId
     * @return
     */
    List<DishFlavor> getByDishId(Long dishId);
}
