package com.achain.blockchain.game.conf;


import com.achain.blockchain.game.domain.entity.BlockchainRecord;
import com.achain.blockchain.game.service.IBlockchainRecordService;
import com.achain.blockchain.game.utils.SDKHttpClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

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

    @Value("${encodeRules}")
    public String encodeRules;


    public long headerBlockCount;

    private final SDKHttpClient httpClient;

    @Autowired
    private IBlockchainRecordService blockchainRecordService;

    @Autowired
    public Config(SDKHttpClient httpClient) {
        this.httpClient = httpClient;
    }


    @PostConstruct
    public void getHeaderBlockCount() {
        EntityWrapper<BlockchainRecord> wrapper = new EntityWrapper<>();
        wrapper.orderBy("block_num", false);
        BlockchainRecord blockchainRecord = blockchainRecordService.selectOne(wrapper);
        if (Objects.nonNull(blockchainRecord)) {
            headerBlockCount = blockchainRecord.getBlockNum();
        } else {
            String result = httpClient.post(walletUrl, rpcUser, "blockchain_get_block_count", new JSONArray());
            JSONObject createTaskJson = JSONObject.parseObject(result);
            headerBlockCount = createTaskJson.getLong("result");
        }
    }


}
