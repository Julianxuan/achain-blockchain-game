package com.achain.blockchain.game.service;

import com.achain.blockchain.game.domain.entity.BlockchainDogOrder;
import com.achain.blockchain.game.domain.enums.OrderStatus;
import com.baomidou.mybatisplus.service.IService;

import java.util.Date;
import java.util.List;

/**
 * @author yujianjian
 * @since 2017-12-12 下午4:08
 */
public interface IBlockchainDogOrderService extends IService<BlockchainDogOrder> {

    /**
     * 根据狗编号和订单状态查询
     * @param dogId　狗的编号
     * @param orderStatus　订单状态
     * @return 符合条件的订单
     */
    List<BlockchainDogOrder> listByDogIdAndStatus(Integer dogId,OrderStatus orderStatus);

    /**
     * 获取失效的单子,更改状态
     * @param nowTime 当前时间
     * @return 应该置为失效的单子
     */
    List<BlockchainDogOrder> listExpireOrders(Date nowTime);
}
