package com.whurs.service.impl;

import com.whurs.context.BaseContext;
import com.whurs.dto.ShoppingCartDTO;
import com.whurs.entity.Dish;
import com.whurs.entity.Setmeal;
import com.whurs.entity.ShoppingCart;
import com.whurs.mapper.DishMapper;
import com.whurs.mapper.SetmealMapper;
import com.whurs.mapper.ShoppingCartMapper;
import com.whurs.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //判断当前购物车的商品是否存在（去购物车表中查询）
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList=shoppingCartMapper.list(shoppingCart);
        //若存在，直接将数量加1
        if(shoppingCartList!=null&&shoppingCartList.size()>0){
            ShoppingCart cart = shoppingCartList.get(0);
            cart.setNumber(cart.getNumber()+1);
            shoppingCartMapper.updateNumberById(cart);
            return;
        }
        //若不存在，向购物车表插入记录
        //判断添加到购物车的是菜品还是套餐
        Long dishId = shoppingCartDTO.getDishId();
        if(dishId!=null){
            //添加的是菜品
            Dish dish = dishMapper.getById(dishId);
            shoppingCart.setName(dish.getName());
            shoppingCart.setAmount(dish.getPrice());
            shoppingCart.setImage(dish.getImage());

        }else{
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setAmount(setmeal.getPrice());
            shoppingCart.setImage(setmeal.getImage());
        }
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCart.setNumber(1);
        shoppingCartMapper.insert(shoppingCart);
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        Long id = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(id);
    }

    /**
     * 从购物车删除商品
     * @param shoppingCartDTO
     */
    @Override
    public void subFromShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart=new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList=shoppingCartMapper.list(shoppingCart);

        if(shoppingCartList!=null&&shoppingCartList.size()>0){
            shoppingCart = shoppingCartList.get(0);
            Integer number = shoppingCart.getNumber();
            //若该记录数量为1，直接删除
            if(number == 1){
                //当前商品在购物车中的份数为1，直接删除当前记录
                shoppingCartMapper.deleteById(shoppingCart.getId());
            }else {
                //当前商品在购物车中的份数不为1，修改份数即可
                shoppingCart.setNumber(shoppingCart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(shoppingCart);
            }

        }
    }
}
