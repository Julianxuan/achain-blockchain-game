package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.domain.entity.BlockchainDogMetingOrder;
import com.achain.blockchain.game.domain.enums.OrderStatus;
import com.achain.blockchain.game.mapper.BlockchainDogMetingOrderMapper;
import com.achain.blockchain.game.service.IBlockchainDogMetingOrderService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yujianjian
 * @since 2017-12-12 下午4:12
 */
@Service
public class BlockchainDogMetingOrderServiceImpl
    extends ServiceImpl<BlockchainDogMetingOrderMapper, BlockchainDogMetingOrder>
    implements IBlockchainDogMetingOrderService {

    @Override
    public List<BlockchainDogMetingOrder> listByDogIdAndStatus(Integer dogId, OrderStatus orderStatus) {
        EntityWrapper<BlockchainDogMetingOrder> wrapper = new EntityWrapper<>();
        wrapper.where("dog_id={0}", dogId).and("status={0}", orderStatus.getIntKey());
        return baseMapper.selectList(wrapper);
    }
}
