package com.achain.blockchain.game.conf;


import com.achain.blockchain.game.utils.SDKHttpClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-11-29 下午5:22
 */
@Component
@Slf4j
public class Config {


    @Value("${contract_id}")
    public String contractId;

    @Value("${wallet_url}")
    public String walletUrl;

    @Value("${rpc_user}")
    public String rpcUser;


    public long headerBlockCount;

    private final SDKHttpClient httpClient;

    @Autowired
    public Config(SDKHttpClient httpClient) {
        this.httpClient = httpClient;
    }


//    @PostConstruct
//    public void getHeaderBlockCount() {
//        //首先从库里查询最大块,如果查询不到那么就调用rpc查询
//        String result = httpClient.post(walletUrl, rpcUser, "blockchain_get_block_count", new JSONArray());
//        JSONObject createTaskJson = JSONObject.parseObject(result);
//        headerBlockCount = createTaskJson.getLong("result");
//    }


}
