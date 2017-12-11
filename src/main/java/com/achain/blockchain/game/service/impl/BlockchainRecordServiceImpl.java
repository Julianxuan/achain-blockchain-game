package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.domain.entity.BlockchainRecord;
import com.achain.blockchain.game.mapper.BlockchainRecordMapper;
import com.achain.blockchain.game.service.IBlockchainRecordService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 * @author yujianjian
 * @since 2017-12-11 下午2:09
 */
@Service
public class BlockchainRecordServiceImpl extends ServiceImpl<BlockchainRecordMapper, BlockchainRecord>
    implements IBlockchainRecordService {

}
