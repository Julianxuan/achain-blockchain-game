package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.domain.consts.CryptoDogEventType;
import com.achain.blockchain.game.domain.dto.TransactionDTO;
import com.achain.blockchain.game.service.ICryptoDogService;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-12-12 上午11:20
 */
@Service
@Slf4j
public class CryptoDogServiceImpl implements ICryptoDogService {

    @Override
    public void generateZeroDog(TransactionDTO transactionDTO) {
        log.info("generateZeroDog|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.GENERATE_SUCCESS.equals(eventType)) {

        } else {

        }
    }

    @Override
    public void bid(TransactionDTO transactionDTO) {
        log.info("bid|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.BID_SUCCESS.equals(eventType)) {

        } else {

        }
    }

    @Override
    public void addAuction(TransactionDTO transactionDTO) {
        log.info("addAuction|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.ADD_AUCTION_SUCCESS.equals(eventType)) {

        } else {

        }
    }

    @Override
    public void cancelAuction(TransactionDTO transactionDTO) {
        log.info("cancelAuction|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.CANCEL_AUCTION_SUCCESS.equals(eventType)) {

        } else {

        }
    }
}
