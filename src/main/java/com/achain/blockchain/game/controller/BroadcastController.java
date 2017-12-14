package com.achain.blockchain.game.controller;

import com.achain.blockchain.game.domain.dto.OfflineSignDTO;
import com.achain.blockchain.game.service.IBlockchainService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    /**
     * 离线签名接口
     * @param offlineSignDTO 签名数据
     * @return 签名后的data
     */
    @PostMapping("offline/sign")
    public Map<String,String> offLineSign(@RequestBody OfflineSignDTO offlineSignDTO){
        log.info("offLineSign|offlineSignDTO={}",offlineSignDTO);
        return blockchainService.offLineSign(offlineSignDTO);
    }

    /**
     * 合约充值离线签名接口
     * @param offlineSignDTO 签名数据
     * @return 签名后的data
     */
    @PostMapping("offLineRechargeSign")
    public Map<String,String> offLineRechargeSign(@RequestBody OfflineSignDTO offlineSignDTO){
        log.info("offLineRechargeSign|offlineSignDTO={}",offlineSignDTO);
        return blockchainService.offLineRechargeSign(offlineSignDTO);
    }


    /**
     * 查询账户act余额,获得的余额需要除以10的五次方
     * @param actAddress act地址
     * @return 余额
     */
    @GetMapping("balance")
    public Long getBalance(String actAddress){
        log.info("getBalance|actAddress={}",actAddress);
        if(StringUtils.isEmpty(actAddress)){
            return 0L;
        }
        return blockchainService.getBalance(actAddress);
    }
}
