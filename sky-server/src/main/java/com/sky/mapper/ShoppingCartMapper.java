package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 动态条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据ID修改商品数量
     * @param shoppingCart
     */
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 购物车插入数据
     * @param shoppingCart
     */
    void insert(ShoppingCart shoppingCart);


    /**
     * 清空购物车
     * @param shoppingCart
     */
    void clean(ShoppingCart shoppingCart);

    /**
     * 删除购物车的一件商品
     * @param shoppingCart
     */
    void delete(ShoppingCart shoppingCart);
}
