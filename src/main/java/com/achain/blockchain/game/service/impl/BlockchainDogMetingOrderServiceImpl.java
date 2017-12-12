package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.domain.entity.BlockchainDogMetingOrder;
import com.achain.blockchain.game.mapper.BlockchainDogMetingOrderMapper;
import com.achain.blockchain.game.service.IBlockchainDogMetingOrderService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 * @author yujianjian
 * @since 2017-12-12 下午4:12
 */
@Service
public class BlockchainDogMetingOrderServiceImpl
    extends ServiceImpl<BlockchainDogMetingOrderMapper, BlockchainDogMetingOrder>
    implements IBlockchainDogMetingOrderService {
}
