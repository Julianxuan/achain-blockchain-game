package com.achain.blockchain.game.service;

import com.achain.blockchain.game.domain.entity.BlockchainRecord;
import com.baomidou.mybatisplus.mapper.EntityWrapper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cglib.core.Block;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IBlockchainRecordServiceTest {

    @Autowired
    private IBlockchainRecordService blockchainService;

    @Test
    public void insert() throws Exception{
        BlockchainRecord blockchainRecord = new BlockchainRecord();
        blockchainRecord.setBlockNum(1L);
        blockchainRecord.setContractId("12321");
        blockchainRecord.setTrxId("fdafds");
        blockchainRecord.setTrxTime(new Date());
        boolean insert = blockchainService.insert(blockchainRecord);
        Assert.assertTrue(insert);
    }

    @Test
    public void listAll() throws Exception{
        EntityWrapper<BlockchainRecord> wrapper = new EntityWrapper<>();
        List<BlockchainRecord> list = blockchainService.selectList(wrapper);
        Assert.assertTrue(list.size() > 0);
    }
}