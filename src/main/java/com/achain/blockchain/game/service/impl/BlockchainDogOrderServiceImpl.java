package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.domain.entity.BlockchainDogOrder;
import com.achain.blockchain.game.mapper.BlockchainDogOrderMapper;
import com.achain.blockchain.game.service.IBlockchainDogOrderService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 * @author yujianjian
 * @since 2017-12-12 下午4:11
 */
@Service
public class BlockchainDogOrderServiceImpl extends ServiceImpl<BlockchainDogOrderMapper, BlockchainDogOrder>
    implements IBlockchainDogOrderService {
}
