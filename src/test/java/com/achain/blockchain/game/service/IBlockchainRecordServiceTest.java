package com.achain.blockchain.game.service;

import com.achain.blockchain.game.conf.Config;
import com.achain.blockchain.game.domain.entity.BlockchainRecord;
import com.baomidou.mybatisplus.mapper.EntityWrapper;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IBlockchainRecordServiceTest {

    @Autowired
    private IBlockchainRecordService blockchainService;
    @Autowired
    private Config config;

    @Test
    public void insert() throws Exception {
        BlockchainRecord blockchainRecord = new BlockchainRecord();
        blockchainRecord.setBlockNum(1L);
        blockchainRecord.setContractId("12321");
        blockchainRecord.setTrxId("fdafds");
        blockchainRecord.setTrxTime(new Date());
        boolean insert = blockchainService.insert(blockchainRecord);
        Assert.assertTrue(insert);
    }

    @Test
    public void listAll() throws Exception {
        EntityWrapper<BlockchainRecord> wrapper = new EntityWrapper<>();
        List<BlockchainRecord> list = blockchainService.selectList(wrapper);
        long blockCount = config.headerBlockCount;
        Assert.assertTrue(list.size() > 0);
    }

    @Test
    public void selectMaxBlockNum() throws Exception {
        EntityWrapper<BlockchainRecord> wrapper = new EntityWrapper<>();
        wrapper.orderBy("block_num", false);
        BlockchainRecord blockchainRecord = blockchainService.selectOne(wrapper);
        System.out.println(23123);
    }
}