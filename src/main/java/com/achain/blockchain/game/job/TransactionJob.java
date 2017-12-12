package com.achain.blockchain.game.job;


import com.achain.blockchain.game.conf.Config;
import com.achain.blockchain.game.domain.dto.TransactionDTO;
import com.achain.blockchain.game.domain.enums.ContractGameMethod;
import com.achain.blockchain.game.service.IBlockchainService;
import com.achain.blockchain.game.service.ICryptoDogService;
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

    private final ICryptoDogService cryptoDogService;


    @Autowired
    public TransactionJob(Config config,
                          IBlockchainService blockchainService,
                          ICryptoDogService cryptoDogService) {
        this.config = config;
        this.blockchainService = blockchainService;
        this.cryptoDogService = cryptoDogService;
    }

    @Scheduled(fixedDelay = 10 * 1000)
    public void doTransactionJob() {
        log.info("doTransactionJob|开始|HeaderBlockNum={}", config.headerBlockCount);
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
                    dealRpcReturnData(transactionDTO);
                }
            }
        }
        config.headerBlockCount = headerBlockCount;
        log.info("doTransactionJob|结束|nowHeaderBlockNum={}", config.headerBlockCount);
    }

    /**
     * 处理加密狗rpc方法调用返回的原始数据
     *
     * @param transactionDTO 原始数据dto
     */
    private void dealRpcReturnData(TransactionDTO transactionDTO) {
        ContractGameMethod method = ContractGameMethod.getMethod(transactionDTO.getCallAbi());
        switch (method) {
            case GENERATE_ZERO_DOG:
                cryptoDogService.generateZeroDog(transactionDTO);
                break;
            case SALES_BID:
                cryptoDogService.bid(transactionDTO);
                break;
            case SALES_ADD_AUCTION:
                cryptoDogService.addAuction(transactionDTO);
                break;
            case SALES_CANCEL_AUCTION:
                cryptoDogService.cancelAuction(transactionDTO);
                break;
            default:
        }
    }


}
