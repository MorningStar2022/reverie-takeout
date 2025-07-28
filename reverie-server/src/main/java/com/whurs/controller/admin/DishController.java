package com.whurs.controller.admin;

import com.whurs.dto.DishDTO;
import com.whurs.dto.DishPageQueryDTO;
import com.whurs.entity.Dish;
import com.whurs.result.PageResult;
import com.whurs.result.Result;
import com.whurs.service.DishService;
import com.whurs.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 新增菜品
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result saveDish(DishDTO dishDTO){
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        //清除缓存菜品
        cleanCache("dish_"+dishDTO.getCategoryId());
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}",dishPageQueryDTO);
        PageResult pageResult=dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除:{}",ids);
        dishService.deleteBatch(ids);
        //清除缓存菜品
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品,用于页面回显:{}",id);
        DishVO dishVO=dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息，包括口味
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result updateDish(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息:{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        //清除缓存菜品
        cleanCache("dish_*");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售或停售")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("菜品起售或停售：{},{}",id,status);
        dishService.startOrStop(id,status);
        //清除缓存菜品
        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据categoryid获取菜品数据")
    public Result<List<Dish>> listByCategory(Long categoryId){
        List<Dish> dishList=dishService.getByCategory(categoryId);
        return Result.success(dishList);
    }

    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }


}
