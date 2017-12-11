package com.achain.blockchain.game.job;


import com.achain.blockchain.game.conf.Config;
import com.achain.blockchain.game.domain.dto.TransactionDTO;
import com.achain.blockchain.game.service.IBlockchainService;
import com.alibaba.fastjson.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fyk
 * @since 2017-11-30 10:29
 */
@Component
@Slf4j
public class TransactionJob {

    private final Config config;

    private final IBlockchainService blockchainService;


    @Autowired
    public TransactionJob(Config config, IBlockchainService blockchainService) {
        this.config = config;
        this.blockchainService = blockchainService;
    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void doTransactionJob() {
        log.info("doTransactionJob|定时任务开始");
        long headerBlockCount = blockchainService.getBlockCount();
        if (headerBlockCount <= config.headerBlockCount) {
            log.info("doTransactionJob|最大块号为[{}],不需要进行扫块", headerBlockCount);
            return;
        }
        for (long blockCount = config.headerBlockCount + 1; blockCount <= headerBlockCount; ++blockCount) {
            JSONArray transactionList = blockchainService.getBlock(blockCount);
            if (transactionList.isEmpty()) {
                continue;
            }
            for (Object transaction : transactionList) {
                TransactionDTO transactionDTO = blockchainService.getTransaction(blockCount, (String) transaction);
                if (Objects.nonNull(transactionDTO)) {
                    //TODO
                }


            }
        }
        config.headerBlockCount = headerBlockCount;
    }


}
