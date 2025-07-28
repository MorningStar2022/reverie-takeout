package com.whurs.mapper;

import com.github.pagehelper.Page;
import com.whurs.dto.GoodsSalesDTO;
import com.whurs.dto.OrdersPageQueryDTO;
import com.whurs.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 历史订单分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /**
     * 查询对应状态的订单数量
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status=#{status}")
    Integer countStatus(Integer status);

    /**
     * 根据订单状态与下单时间查询
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time<#{orderTime}")
    List<Orders> getByStatusAndTimeLt(Integer status, LocalDateTime orderTime);

    /**
     * 统计某日营业额
     * @param map
     * @return
     */
    Double getTurnoverByMap(Map map);

    /**
     * 根据条件动态查询订单数
     * @param map
     */
    Integer countByMap(Map map);

    /**
     * 动态查询某段时间内销量top10
     * @param startTime
     * @param endTime
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime startTime, LocalDateTime endTime);

    Double sumByMap(Map map);

}
