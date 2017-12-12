package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.domain.entity.BlockchainDogInfo;
import com.achain.blockchain.game.mapper.BlockchainDogInfoMapper;
import com.achain.blockchain.game.service.IBlockchainDogInfoService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yujianjian
 * @since 2017-12-12 下午4:10
 */
@Service
public class BlockchainDogInfoServiceImpl extends ServiceImpl<BlockchainDogInfoMapper, BlockchainDogInfo>
    implements IBlockchainDogInfoService {

    @Override
    public BlockchainDogInfo getByDogId(Integer dogId) {
        EntityWrapper<BlockchainDogInfo> wrapper = new EntityWrapper<>();
        wrapper.where("dog_id={0}", dogId);
        List<BlockchainDogInfo> list = baseMapper.selectList(wrapper);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}