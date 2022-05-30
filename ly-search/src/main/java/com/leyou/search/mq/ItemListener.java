package com.leyou.search.mq;

import com.leyou.common.constants.MQConstants;
import com.leyou.search.service.SearchService;
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
    private SearchService searchService;

    /**
     * 上架->导入索引
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value= MQConstants.Queue.SEARCH_ITEM_UP),
                    exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
                    key = MQConstants.RoutingKey.ITEM_UP_KEY
            )
    )
    public void createIndex(Long spuId){
        try {
            searchService.createIndex(spuId);
            log.info("【索引同步】索引创建成功，ID:"+spuId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【索引同步】索引创建失败，原因："+e.getMessage());
        }

    }

    /**
     * 下架->删除索引
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value= MQConstants.Queue.SEARCH_ITEM_DOWN),
                    exchange = @Exchange(name = MQConstants.Exchange.ITEM_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
                    key = MQConstants.RoutingKey.ITEM_DOWN_KEY
            )
    )
    public void deleteIndex(Long spuId){
        try {
            searchService.deleteIndex(spuId);
            log.info("【索引同步】索引删除成功，ID:"+spuId);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【索引同步】索引删除失败，原因："+e.getMessage());
        }

    }
}
