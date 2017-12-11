package com.achain.blockchain.game.controller;

import com.achain.blockchain.game.service.IBlockchainService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-12-11 上午11:08
 */
@RestController
@RequestMapping("/api/act")
@Slf4j
public class BroadcastController {


    @Autowired
    private IBlockchainService blockchainService;


    /**
     * 交易广播接口
     */
    @RequestMapping(value = "/network_broadcast_transaction", method = RequestMethod.POST)
    public String networkBroadcastTransaction(String message) {
        log.info("ActRPCTransactionController|network_broadcast_transaction|收到消息|[message={}]", message);
        String result = null;
        try {
            result = blockchainService.networkBroadcast(message);
        } catch (Exception e) {
            log.info("ActRPCTransactionController|network_broadcast_transaction|执行异常", e);
        }
        log.info("ActRPCTransactionController|network_broadcast_transaction|返回结果|[result={}]", result);
        return result;
    }
}
