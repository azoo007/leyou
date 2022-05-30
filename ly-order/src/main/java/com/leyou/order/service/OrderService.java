package com.leyou.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.leyou.common.auth.utils.UserInfo;
import com.leyou.common.constants.LyConstants;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.common.utils.UserHolder;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.Sku;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.dto.OrderVO;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderLogisticsMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderLogistics;
import com.leyou.order.pojo.OrderStatusEnum;
import com.leyou.order.utils.PayHelper;
import com.leyou.user.client.UserClient;
import com.leyou.user.pojo.AddressDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单业务
 */
@Service
@Transactional
@Slf4j
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderLogisticsMapper orderLogisticsMapper;

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private UserClient userClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private PayHelper payHelper;

    //@GlobalTransactional // 添加分布式事务
    public Long buildOrder(OrderDTO orderDTO) {
        //1.插入订单表
        Order order = new Order();
        //订单ID
        order.setOrderId(idWorker.nextId());
        //订单总金额
        //1）取出当前购买的所有skuId集合
        List<Long> skuIds = orderDTO.getCarts().stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        //2）根据skuId集合查询Sku对象集合
        List<Sku> skuList = itemClient.findSkusByIds(skuIds);
        //3）设计Map集合，key：skuId,value：num
        Map<Long,Integer> skuMap = orderDTO.getCarts().stream().collect(Collectors.toMap( CartDTO::getSkuId  , CartDTO::getNum ));
        //4）计算商品总金额
        Long totalFee = skuList.stream().mapToLong(sku -> sku.getPrice()*skuMap.get(sku.getId())).sum();
        order.setTotalFee(totalFee);

        //实付金额 = 总金额-优惠金额+运费
        order.setActualFee(1L);

        //优惠活动
        order.setPromotionIds("1");
        //支付类型
        order.setPaymentType(orderDTO.getPaymentType());
        //邮费
        order.setPostFee(50L);
        //用户ID
        UserInfo userInfo = UserHolder.getUser();
        order.setUserId(userInfo.getId());
        //发票类型
        order.setInvoiceType(0);
        //订单来源
        order.setSourceType(2);
        //订单状态
        order.setStatus(OrderStatusEnum.INIT.value());

        orderMapper.insert(order);

        //2.插入订单明细表
        skuList.forEach(sku -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setId(idWorker.nextId());
            orderDetail.setOrderId(order.getOrderId());//订单ID
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setImage(sku.getImages());
            orderDetail.setNum(skuMap.get(sku.getId()));
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setOwnSpec(sku.getOwnSpec());

            orderDetailMapper.insert(orderDetail);
        });


        //3.插入订单物流表
        //根据地址ID查询收货人地址信息
        AddressDTO address = userClient.findAddressById(userInfo.getId(), orderDTO.getAddressId());
        OrderLogistics orderLogistics = BeanHelper.copyProperties(address,OrderLogistics.class);
        //订单ID
        orderLogistics.setOrderId(order.getOrderId());
        //物流单号
        orderLogistics.setLogisticsNumber("2122222211");
        //物流名称
        orderLogistics.setLogisticsCompany("菜鸟物流");

        orderLogisticsMapper.insert(orderLogistics);

        //4.扣减对应商品的库存量
        itemClient.minusStock(skuMap);

        //模拟异常
        //int i = 100/0;

        return order.getOrderId();
    }

    public OrderVO findOrderById(Long id) {
        //1.查询Order数据
        Order order = orderMapper.selectById(id);
        //拷贝数据
        OrderVO orderVO = BeanHelper.copyProperties(order, OrderVO.class);
        //2.查询物流信息，并封装数据
        OrderLogistics orderLogistics = orderLogisticsMapper.selectById(id);
        orderVO.setLogistics(orderLogistics);
        //3.查询订单明细，并封装数据
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        QueryWrapper<OrderDetail> queryWrapper = Wrappers.query(orderDetail);
        List<OrderDetail> orderDetails = orderDetailMapper.selectList(queryWrapper);
        orderVO.setDetailList(orderDetails);
        return orderVO;
    }

    public String getPayUrl(Long id) {
        //1.从redis取出支付链接
        String payUrl = redisTemplate.opsForValue().get(LyConstants.PAY_URL_PRE+id);

        //2.如果没有，则调用微信支付系统获取支付链接，把链接存入redis
        if(StringUtils.isEmpty(payUrl)){
            //查询订单
            Order order = orderMapper.selectById(id);
            long totalFee = order.getActualFee();
            payUrl = payHelper.getPayUrl(id,totalFee);

            //存入redis数据库
            redisTemplate.opsForValue().set(LyConstants.PAY_URL_PRE+id,payUrl,2, TimeUnit.HOURS);
        }

        //3.如果有，直接返回redis的支付链接
        return payUrl;
    }

    public void wxNotify(Map<String, String> paramMap) {
        //1.获取微信传递的参数
        Long orderId = Long.valueOf(paramMap.get("out_trade_no"));
        Long totalFee = Long.valueOf(paramMap.get("total_fee"));

        //2.查询订单
        Order order = orderMapper.selectById(orderId);

        //3.基本校验
        if(order==null){
            log.error("订单不存在");
            throw new LyException(500,"订单不存在");
        }
        if(order.getActualFee()!=totalFee){
            log.error("支付金额不一致");
            throw new LyException(500,"支付金额不一致");
        }

        //4.更新订单状态和支付时间
        try {
            order.setStatus(OrderStatusEnum.PAY_UP.value());
            order.setPayTime(new Date());
            orderMapper.updateById(order);

            log.info("支付成功，订单信息更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("更新订单信息失败");
            throw new LyException(500,"更新订单信息失败");
        }

    }

    public Integer checkState(Long id) {
        Order order = orderMapper.selectById(id);
        if(order==null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        return order.getStatus();
    }
}
