package com.leyou.cart.service;

import com.leyou.cart.pojo.Cart;
import com.leyou.common.auth.utils.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    public void addCart(Cart cart) {
        //1.获取当前登录用户的购物车数据
        BoundHashOperations<String, Object, Object> boundHashOps = getLoginUserCarts();

        //2.判断当前商品是否在购物车中
        String skuIdStr = cart.getSkuId().toString();
        if(boundHashOps.hasKey(skuIdStr)){
            //2.1 如果在，则从购物车中取出该商品，更新商品数量，把更新的商品数据刷新到redis数据库中
            String cartJson = (String)boundHashOps.get(skuIdStr);
            //转换为Cart对象
            Cart oldCart = JsonUtils.toBean(cartJson, Cart.class);
            //更改商品数量
            cart.setNum(oldCart.getNum()+cart.getNum());
        }
        //2.2 把更新的商品数据刷新到redis数据库中
        boundHashOps.put(skuIdStr,JsonUtils.toString(cart));
    }

    /**
     * 获取当前登录用户的购物车数据
     * @return
     */
    public BoundHashOperations<String, Object, Object> getLoginUserCarts() {
        //1.取出登录用户信息
        UserInfo userInfo = UserHolder.getUser();
        //取出用户ID
        String userId = userInfo.getId().toString();

        //2.取出当前登录用户的购物车数据（Map<skuId字符串,Cart对象json字符串>）
        //BoundHashOperations就是一个Map结构： Map<skuId字符串,Cart对象json字符串>
        return redisTemplate.boundHashOps(userId);
    }

    public List<Cart> loadCarts() {
        //1.获取当前登录用户的购物车数据
        BoundHashOperations<String, Object, Object> boundHashOps = getLoginUserCarts();
        //2.取出购物车数据，转换为List<Cart>
        /**
         * boundHashOps.values(): 获取Map集合中的所有value值的集合（List<Object>）
         */
        return boundHashOps.values()
                .stream().map( cartJson-> JsonUtils.toBean((String)cartJson,Cart.class) )
                .collect(Collectors.toList());
    }

    public void updateNum(Long skuId, Integer num) {
        //1.获取当前登录用户的购物车数据
        BoundHashOperations<String, Object, Object> boundHashOps = getLoginUserCarts();

        //2.取出指定商品
        String skuIdStr = skuId.toString();
        String cartJson = (String)boundHashOps.get(skuIdStr);

        //3.转换Cart对象
        Cart cart = JsonUtils.toBean(cartJson, Cart.class);

        //4.修改数量
        cart.setNum(num);

        //5.刷新redis
        boundHashOps.put(skuIdStr,JsonUtils.toString(cart));
    }


    public void deleteCart(Long skuId) {
        //1.获取当前登录用户的购物车数据
        BoundHashOperations<String, Object, Object> boundHashOps = getLoginUserCarts();
        //2.直接删除指定key
        boundHashOps.delete(skuId.toString());
    }

    public void mergeCarts(List<Cart> carts) {
        carts.forEach(cart -> {
            addCart(cart);
        });
    }
}
