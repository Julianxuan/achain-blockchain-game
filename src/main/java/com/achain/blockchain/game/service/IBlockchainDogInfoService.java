package com.achain.blockchain.game.service;

import com.achain.blockchain.game.domain.entity.BlockchainDogInfo;
import com.baomidou.mybatisplus.service.IService;

/**
 * @author yujianjian
 * @since 2017-12-12 下午4:07
 */
public interface IBlockchainDogInfoService extends IService<BlockchainDogInfo> {

    /**
     * 根据狗编号获取狗信息
     * @param dogId 狗编号
     * @return 狗信息
     */
    BlockchainDogInfo getByDogId(Integer dogId);
}
