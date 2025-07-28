package com.whurs.controller.admin;

import com.whurs.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Slf4j
@Api(tags = "店铺相关接口")
public class ShopController {
    public static final String KEY="SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺营业状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺的营业状态:{}",status==1?"营业中":"休息中");
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    /**
     * 查看店铺营业状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("查看店铺营业状态")
    public Result<Integer> getStatus(){
        Integer shop_status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("设置店铺的营业状态:{}",shop_status==1?"营业中":"休息中");
        return Result.success(shop_status);
    }

}
