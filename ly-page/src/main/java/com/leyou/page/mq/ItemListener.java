package com.leyou.page.mq;

import com.leyou.common.constants.MQConstants;
import com.leyou.page.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
监听商品上下架
 */
@Component
@Slf4j
public class ItemListener {
    @Autowired
    private PageService pageService;

    /**
     * 上架->创建静态页
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value= MQConstants.Queue.PAGE_ITEM_UP),
                    exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
                    key = MQConstants.RoutingKey.ITEM_UP_KEY
            )
    )
    public void createStaticPage(Long spuId){
        try {
            pageService.createStaticPage(spuId);
            log.info("【静态页同步】静态页创建成功，ID:"+spuId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【静态页同步】静态页创建失败，原因："+e.getMessage());
        }

    }

    /**
     * 下架->删除静态页
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value= MQConstants.Queue.PAGE_ITEM_DOWN),
                    exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
                    key = MQConstants.RoutingKey.ITEM_DOWN_KEY
            )
    )
    public void deleteStaticPage(Long spuId){
        try {
            pageService.deleteStaticPage(spuId);
            log.info("【静态页同步】静态页删除成功，ID:"+spuId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【静态页同步】静态页删除失败，原因："+e.getMessage());
        }

    }
}
