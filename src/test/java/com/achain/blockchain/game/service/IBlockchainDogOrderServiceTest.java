package com.achain.blockchain.game.service;

import com.achain.blockchain.game.domain.entity.BlockchainDogOrder;
import com.achain.blockchain.game.domain.enums.OrderStatus;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IBlockchainDogOrderServiceTest {

    @Autowired
    private IBlockchainDogOrderService blockchainDogOrderService;

    @Test
    public void listByDogIdAndStatus() {
        List<BlockchainDogOrder> list =
            blockchainDogOrderService.listByDogIdAndStatus(0, OrderStatus.ON);
        Assert.assertTrue(list.size() > 0);
    }
}