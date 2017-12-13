package com.achain.blockchain.game.job;

import com.achain.blockchain.game.domain.entity.BlockchainDogMetingOrder;
import com.achain.blockchain.game.domain.entity.BlockchainDogOrder;
import com.achain.blockchain.game.domain.enums.OrderStatus;
import com.achain.blockchain.game.service.IBlockchainDogMetingOrderService;
import com.achain.blockchain.game.service.IBlockchainDogOrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-12-13 下午5:54
 */
@Component
@Slf4j
public class OrderStatusJob {


    @Autowired
    private IBlockchainDogMetingOrderService blockchainDogMetingOrderService;
    @Autowired
    private IBlockchainDogOrderService blockchainDogOrderService;

    @Scheduled(fixedDelay = 10 * 1000)
    public void orderJob() {
        Date currentTime = new Date();
        log.info("orderJob|begin|time={}",currentTime);

        List<BlockchainDogOrder> blockchainDogOrders = blockchainDogOrderService.listExpireOrders(currentTime);
        blockchainDogOrders.forEach((BlockchainDogOrder order) -> order.setStatus(OrderStatus.EXPIRE.getIntKey()));
        if(blockchainDogOrders.size() > 0){
            log.info("orderJob|更新交易单子为失效|size={}",blockchainDogOrders.size());
            blockchainDogOrderService.updateBatchById(blockchainDogOrders);
        }
        List<BlockchainDogMetingOrder> blockchainDogMetingOrders =
            blockchainDogMetingOrderService.listExpireOrders(currentTime);
        blockchainDogMetingOrders.forEach((BlockchainDogMetingOrder order) -> order.setStatus(OrderStatus.EXPIRE.getIntKey()));
        if(blockchainDogMetingOrders.size() > 0){
            log.info("orderJob|更新繁衍交易单子为失效|size={}",blockchainDogMetingOrders.size());
            blockchainDogMetingOrderService.updateBatchById(blockchainDogMetingOrders);
        }
        log.info("orderJob|end|cost {} ms",System.currentTimeMillis() - currentTime.getTime());
    }
}
