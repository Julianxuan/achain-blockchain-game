package com.achain.blockchain.game.service;

import com.achain.blockchain.game.domain.dto.UserOrderDTO;
import com.achain.blockchain.game.domain.entity.BlockchainDogUserOrder;
import com.baomidou.mybatisplus.service.IService;

/**
 * @author yujianjian
 * @since 2017-12-12 下午7:47
 */
public interface IBlockchainDogUserOrderService extends IService<BlockchainDogUserOrder> {

    /**
     * 根据trxId获取订单信息
     * @param trxId 链上订单号
     * @return 订单信息
     */
    BlockchainDogUserOrder getByTrxId(String trxId);

    /**
     * 更新订单
     * @param userOrderDTO　更新的信息
     */
    void updateTrx(UserOrderDTO userOrderDTO);
}
