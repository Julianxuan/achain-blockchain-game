package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.domain.entity.BlockchainDogUserOrder;
import com.achain.blockchain.game.domain.enums.OrderStatus;
import com.achain.blockchain.game.mapper.BlockchainDogUserOrderMapper;
import com.achain.blockchain.game.service.IBlockchainDogUserOrderService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author yujianjian
 * @since 2017-12-12 下午7:49
 */
@Service
public class BlockchainDogUserOrderServiceImpl extends ServiceImpl<BlockchainDogUserOrderMapper, BlockchainDogUserOrder>
    implements IBlockchainDogUserOrderService {

    @Override
    public BlockchainDogUserOrder getByTrxId(String trxId) {
        EntityWrapper<BlockchainDogUserOrder> wrapper = new EntityWrapper<>();
        wrapper.where("trx_id={0}", trxId);
        List<BlockchainDogUserOrder> list = baseMapper.selectList(wrapper);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public void updateTrx(String trxId, OrderStatus orderStatus, String errorMessage) {
        BlockchainDogUserOrder userOrder = getByTrxId(trxId);
        if (Objects.nonNull(userOrder)) {
            if (OrderStatus.SUCCESS == orderStatus) {
                userOrder.setStatus(orderStatus.getIntKey());
            } else if (OrderStatus.FAIL == orderStatus) {
                userOrder.setStatus(orderStatus.getIntKey());
                userOrder.setErrorMessage(errorMessage);
            }
            baseMapper.updateById(userOrder);
        }
    }
}
