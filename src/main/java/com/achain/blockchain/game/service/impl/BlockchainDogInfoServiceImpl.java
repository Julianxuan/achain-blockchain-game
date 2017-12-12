package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.domain.entity.BlockchainDogInfo;
import com.achain.blockchain.game.mapper.BlockchainDogInfoMapper;
import com.achain.blockchain.game.service.IBlockchainDogInfoService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 * @author yujianjian
 * @since 2017-12-12 下午4:10
 */
@Service
public class BlockchainDogInfoServiceImpl extends ServiceImpl<BlockchainDogInfoMapper, BlockchainDogInfo>
    implements IBlockchainDogInfoService {
}