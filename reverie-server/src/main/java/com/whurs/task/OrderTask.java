package com.whurs.task;

import com.whurs.entity.Orders;
import com.whurs.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     * 从0s开始每分钟触发一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder(){
        log.info("自动处理超时订单:{}",LocalDateTime.now());//超过15分钟未支付则订单超时
        LocalDateTime time=LocalDateTime.now().minusMinutes(15);
        List<Orders>ordersList=orderMapper.getByStatusAndTimeLt(Orders.PENDING_PAYMENT,time);
        if(ordersList!=null&&ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时，自动取消");
                orderMapper.update(orders);
            }
        }
    }


    /**
     * 处理一直处于派送中的订单
     * 从0s开始每天凌晨1点触发一次
     */
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨1点处理一次
    public void processDeliveryOrder(){
        log.info("自动处理派送中订单:{}",LocalDateTime.now());
        LocalDateTime time=LocalDateTime.now().minusMinutes(60);
        List<Orders>ordersList=orderMapper.getByStatusAndTimeLt(Orders.DELIVERY_IN_PROGRESS,time);
        if(ordersList!=null&&ordersList.size()>0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
