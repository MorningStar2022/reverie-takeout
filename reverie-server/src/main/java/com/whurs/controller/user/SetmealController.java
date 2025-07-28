package com.whurs.controller.user;


import com.whurs.constant.StatusConstant;
import com.whurs.entity.Setmeal;
import com.whurs.result.Result;
import com.whurs.service.SetmealService;
import com.whurs.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api(tags = "C端-套餐相关接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache",key = "#categoryId")
    public Result<List<Setmeal>> list(Long categoryId){
        log.info("根据分类id查询套餐:{}",categoryId);
        Setmeal setmeal=new Setmeal();
        setmeal.setStatus(StatusConstant.ENABLE);
        setmeal.setCategoryId(categoryId);
        List<Setmeal> setmealList=setmealService.list(setmeal);
        return Result.success(setmealList);
    }


    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询对应菜品列表")
    public Result<List<DishItemVO>> getDishList(@PathVariable Long id){
        log.info("根据套餐id查询对应菜品列表:{}",id);
        List<DishItemVO> dishItemVOList=setmealService.getDishItemById(id);
        return Result.success(dishItemVOList);
    }

}
