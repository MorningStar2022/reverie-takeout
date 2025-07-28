package com.whurs.controller.user;

import com.whurs.dto.OrdersPaymentDTO;
import com.whurs.dto.OrdersSubmitDTO;
import com.whurs.result.PageResult;
import com.whurs.result.Result;
import com.whurs.service.OrderService;
import com.whurs.vo.OrderPaymentVO;
import com.whurs.vo.OrderSubmitVO;
import com.whurs.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端-订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;
    /**
     * 用户提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户提交订单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户提交订单：{}",ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO=orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 查询历史订单
     */
    @GetMapping("/historyOrders")
    @ApiOperation("查询历史订单")
    public Result<PageResult> page(int page,int pageSize,Integer status){
        log.info("查询历史订单");
        PageResult pageResult=orderService.pageQuery4User(page,pageSize,status);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id) {
        log.info("查询订单id:{}",id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 用户取消订单
     *
     * @return
     *
     * - 待支付和待接单状态下，用户可直接取消订单
     * - 商家已接单状态下，用户取消订单需电话沟通商家
     * - 派送中状态下，用户取消订单需电话沟通商家
     * - 如果在待接单状态下取消订单，需要给用户退款
     * - 取消订单后需要将订单状态修改为“已取消”
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable Long id) throws Exception {
        orderService.userCancelById(id);
        return Result.success();
    }

    /**
     * 再来一单
     *
     * @param id
     * @return
     * 再来一单就是将原订单中的商品重新加入到购物车中
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id) {
        orderService.repetition(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable Long id){
        log.info("用户催单:{}",id);
        orderService.reminder(id);
        return Result.success();
    }
}
