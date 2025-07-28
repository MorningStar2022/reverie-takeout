package com.whurs.service;

import com.whurs.dto.ShoppingCartDTO;
import com.whurs.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     * @return
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空购物车
     */
    void cleanShoppingCart();

    /**
     * 从购物车删除商品
     * @param shoppingCartDTO
     */
    void subFromShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
