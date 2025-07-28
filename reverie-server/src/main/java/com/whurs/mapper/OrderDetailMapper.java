package com.whurs.mapper;

import com.whurs.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderDetailMapper {
    /**
     * 向订单明细表中插入多条数据
     * @param orderDetailList
     */
    void insertBatch(List<OrderDetail> orderDetailList);

    /**
     * 根据订单id查询对应明细
     * @param orderId
     * @return
     */
    List<OrderDetail> getByOrderId(Long orderId);
}
