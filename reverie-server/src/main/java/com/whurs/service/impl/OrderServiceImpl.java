package com.whurs.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.whurs.constant.MessageConstant;
import com.whurs.constant.StatusConstant;
import com.whurs.context.BaseContext;
import com.whurs.dto.*;
import com.whurs.entity.*;
import com.whurs.exception.AddressBookBusinessException;
import com.whurs.exception.OrderBusinessException;
import com.whurs.exception.ShoppingCartBusinessException;
import com.whurs.mapper.*;
import com.whurs.result.PageResult;
import com.whurs.service.OrderService;
import com.whurs.utils.WeChatPayUtil;
import com.whurs.vo.OrderPaymentVO;
import com.whurs.vo.OrderStatisticsVO;
import com.whurs.vo.OrderSubmitVO;
import com.whurs.vo.OrderVO;
import com.whurs.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;


    /**
     * 用户提交订单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务异常（地址簿为空或购物车为空）,则不能完成下单
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook==null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //判断该用户购物车是否为空
        ShoppingCart shoppingCart=new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if(shoppingCarts==null||shoppingCarts.size()==0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入一条数据
        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orderMapper.insert(orders);
        //向订单明细表插入多条数据
        List<OrderDetail> orderDetailList=new ArrayList<>();
        for (ShoppingCart cart : shoppingCarts) {
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //清空该用户购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        //返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);
        JSONObject jsonObject=new JSONObject();

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "Reverie的外卖小店订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向客户端推送消息
        Map map=new HashMap<>();
        map.put("type",1);// 1表示来单提醒
        map.put("orderId",orders.getId());
        map.put("content","订单号:"+outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

    }

    /**
     * 历史订单分页查询
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        PageHelper.startPage(pageNum,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO=new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        Page<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList=new ArrayList<>();
        // 查询出订单明细，并封装入OrderVO进行响应
        if (page != null && page.getTotal() > 0) {
            for (Orders orders : page) {
                Long orderId = orders.getId();// 订单id
                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);

                orderVOList.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOList);


    }

    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        Orders orders=orderMapper.getById(id);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(details);
        return orderVO;

    }

    /**
     * 用户取消订单
     * @param id
     */
    @Override
    public void userCancelById(Long id) {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(id);
        // 校验订单是否存在
        if(ordersDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer ordersStatus = ordersDB.getStatus();
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if(ordersStatus>2){
            //订单状态>2，退款需与商家沟通，暂时先抛出异常
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders=new Orders();
        orders.setId(ordersDB.getId());
        //商家待接单时，需给用户退款
        if(ordersStatus.equals(Orders.TO_BE_CONFIRMED)){
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 再来一单，将该订单下的商品重新加入购物车
     * @param id
     */
    @Override
    public void repetition(Long id) {
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList=new ArrayList<>();
        for (OrderDetail detail : details) {
            ShoppingCart shoppingCart=new ShoppingCart();
            BeanUtils.copyProperties(detail,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartList.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 管理端订单条件查询
     * @param ordersPageQueryDTO
     * @return
     * - 输入订单号/手机号进行搜索，支持模糊搜索
     * - 根据订单状态进行筛选
     * - 下单时间进行时间筛选
     * - 搜索内容为空，提示未找到相关订单
     * - 搜索结果页，展示包含搜索关键词的内容
     * - 分页展示搜索到的订单数据
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList=new ArrayList<>();
        orderVOList=getOrderVOS(page);
        return new PageResult(page.getTotal(),orderVOList);

    }

    /**
     * 各个状态订单数量统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed=orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed=orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress=orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 商家接单
     * 商家接单其实就是将订单的状态修改为“已接单”
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    /**
     * 商家拒单
     * @param ordersRejectionDTO
     * - 商家拒单其实就是将订单状态修改为“已取消”
     * - 只有订单处于“待接单”状态时可以执行拒单操作
     * - 商家拒单时需要指定拒单原因
     * - 商家拒单时，如果用户已经完成了支付，需要为用户退款
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());
        if(ordersDB==null||!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if(ordersDB.getPayStatus().equals(Orders.PAID)){
            log.info("申请退款:{}",ordersDB.getAmount());
        }
        Orders orders=new Orders();
        orders.setId(ordersRejectionDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(orders);
    }

    /**
     * 商家取消订单
     * - 取消订单其实就是将订单状态修改为“已取消”（派送中和已完成的订单不允许取消）
     * - 商家取消订单时需要指定取消原因
     * - 商家取消订单时，如果用户已经完成了支付，需要为用户退款
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
        //订单为空或派送中或已完成均不允许取消
        if(ordersDB==null||ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)
                ||ordersDB.getStatus().equals(Orders.COMPLETED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus.equals(Orders.PAID)) {
            //用户已支付，需要退款
            log.info("申请退款：{}", ordersDB.getAmount());
        }

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 商家派送订单
     * - 派送订单其实就是将订单状态修改为“派送中”
     * - 只有状态为“待派送”的订单可以执行派送订单操作
     * @param id
     */
    @Override
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * - 完成订单其实就是将订单状态修改为“已完成”
     * - 只有状态为“派送中”的订单可以执行订单完成操作
     * @param id
     */
    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 用户催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);
        // 校验订单是否存在
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //通过websocket向客户端推送消息
        Map map=new HashMap<>();
        map.put("type",2);// 2表示用户催单
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号:"+ordersDB.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 将分页查询结果Orders转换成OrderVO
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVOS(Page<Orders> page) {
        List<OrderVO> orderVOList=new ArrayList<>();
        List<Orders> ordersList = page.getResult();
        if(ordersList!=null&&ordersList.size()>0){
            for (Orders orders : ordersList) {
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                //将订单明细拼接为指定格式字符串
                String dishesString=getDishString(orders);
                orderVO.setOrderDishes(dishesString);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;

    }

    /**
     * 将某个订单对应的菜品明细转换为字符串:菜品*数量
     * @param orders
     * @return
     */
    private String getDishString(Orders orders) {
        List<OrderDetail> details = orderDetailMapper.getByOrderId(orders.getId());
        List<String> collect = details.stream().map(x -> {
            return x.getName() + "*" + x.getNumber();
        }).collect(Collectors.toList());
        return String.join(";",collect);

    }

}
