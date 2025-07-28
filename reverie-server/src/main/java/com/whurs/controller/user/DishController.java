package com.whurs.controller.user;

import com.whurs.constant.StatusConstant;
import com.whurs.dto.DishDTO;
import com.whurs.entity.Dish;
import com.whurs.result.Result;
import com.whurs.service.DishService;
import com.whurs.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("根据分类id查询菜品:{}",categoryId);
        //构建redis中的key，规则dish_categoryId
        String key="dish_"+categoryId;
        //查询redis中是否有对应的数据
        List<DishVO>  dishVOList= (List<DishVO>) redisTemplate.opsForValue().get(key);
        //若有，直接返回
        if(dishVOList!=null&&dishVOList.size()>0){
            return Result.success(dishVOList);
        }
        //若没有，从mysql数据库中读取，并缓存到redis中
        //此处是用户端，用户是不能查看已经停售的商品的，在仅传入categoryId的情况下，构造一个dish对象进行查询
        //这样动态查询过滤掉停售的菜品
        Dish dish=new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);
        dishVOList=dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key,dishVOList);
        return Result.success(dishVOList);
    }
}
