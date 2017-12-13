package com.achain.blockchain.game.job;


import com.achain.blockchain.game.conf.Config;
import com.achain.blockchain.game.domain.dto.TransactionDTO;
import com.achain.blockchain.game.domain.entity.BlockchainRecord;
import com.achain.blockchain.game.domain.enums.ContractGameMethod;
import com.achain.blockchain.game.service.IBlockchainRecordService;
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
    private IBlockchainRecordService blockchainRecordService;


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
                    saveTransaction(transactionDTO);
                    dealRpcReturnData(transactionDTO);
                }
            }
        }
        config.headerBlockCount = headerBlockCount;
        log.info("doTransactionJob|结束|nowHeaderBlockNum={}", config.headerBlockCount);
    }

    /**
     * 扫块数据入库
     * @param transactionDTO 数据
     */
    private void saveTransaction(TransactionDTO transactionDTO) {
        BlockchainRecord blockchainRecord = new BlockchainRecord();
        blockchainRecord.setTrxId(transactionDTO.getTrxId());
        blockchainRecord.setTrxTime(transactionDTO.getTrxTime());
        blockchainRecord.setContractId(transactionDTO.getContractId());
        blockchainRecord.setBlockNum(transactionDTO.getBlockNum());
        blockchainRecordService.insert(blockchainRecord);
    }

    /**
     * 处理加密狗rpc方法调用返回的原始数据
     *
     * @param transactionDTO 原始数据dto
     */
    private void dealRpcReturnData(TransactionDTO transactionDTO) {
        ContractGameMethod method = ContractGameMethod.getMethod(transactionDTO.getCallAbi());
        if(method == null){
            return;
        }
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
            case GIFT:
                cryptoDogService.gift(transactionDTO);
                break;
            case MATING_ADD_AUCTION:
                cryptoDogService.addMatingTransaction(transactionDTO);
                break;
            case MATING_CANCEL_AUCTION:
                cryptoDogService.cancelMatingTransaction(transactionDTO);
                break;
            case MATING_BID:
                cryptoDogService.matingTransfer(transactionDTO);
                break;
            case RECHARGE:
                cryptoDogService.recharge(transactionDTO);
                break;
            case CHANGE_FEE:
                cryptoDogService.changeFee(transactionDTO);
                break;
            case CHANGE_CFO:
                cryptoDogService.changeCFO(transactionDTO);
                break;
            case CHANGE_COO:
                cryptoDogService.changeCOO(transactionDTO);
                break;
            case QUERY_DOG:
                cryptoDogService.queryDog(transactionDTO);
                break;
            case WITHDRAW_BENEFIT:
                cryptoDogService.withdrawBenefit(transactionDTO);
                break;
            case BREEDING:
                cryptoDogService.breeding(transactionDTO);
                break;
            default:
                log.error("dealRpcReturnData|没有符合的合约方法|method={}|transactionDTO={}",method,transactionDTO);
                break;
        }
    }


}
