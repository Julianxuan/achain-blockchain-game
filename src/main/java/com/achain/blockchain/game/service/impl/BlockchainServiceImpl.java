package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.conf.Config;
import com.achain.blockchain.game.domain.dto.TransactionDTO;
import com.achain.blockchain.game.service.IBlockchainService;
import com.achain.blockchain.game.utils.SDKHttpClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fyk
 * @since 2017-11-29 19:13
 */
@Service
@Slf4j
public class BlockchainServiceImpl implements IBlockchainService {

    private final SDKHttpClient httpClient;

    private final Config config;

    @Autowired
    public BlockchainServiceImpl(SDKHttpClient httpClient, Config config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    @Override
    public long getBlockCount() {
        log.info("ActBrowserServiceImpl|getBlockCount 开始处理");
        String result = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_block_count", new JSONArray());
        JSONObject createTaskJson = JSONObject.parseObject(result);
        return createTaskJson.getLong("result");
    }

    @Override
    public JSONArray getBlock(long blockNum) {
        log.info("ActBrowserServiceImpl|getBlock 开始处理[{}]", blockNum);
        String result = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_block", String.valueOf(blockNum));
        JSONObject createTaskJson = JSONObject.parseObject(result);
        return createTaskJson.getJSONObject("result").getJSONArray("user_transaction_ids");
    }



    /**
     * 需要判断交易类型，合约id，合约调用的方法和转账到的地址。
     * @param trxId 交易单号
     * @return
     */
    @Override
    public TransactionDTO getTransaction(long blockNum, String trxId){
        log.info("ActBrowserServiceImpl|getBlock 开始处理[{}]", trxId);
        Map<String, Object> map = new HashMap<>(2);
        String result = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_transaction", trxId);
        JSONObject createTaskJson = JSONObject.parseObject(result);
        JSONObject operationJson = createTaskJson.getJSONArray("result")
                                                .getJSONObject(1)
                                                .getJSONObject("trx")
                                                .getJSONArray("operations")
                                                .getJSONObject(0);
        //判断交易类型
        String operationType = operationJson.getString("type");
        String s = operationJson.getString("result");
        System.out.println(s);
        //不是合约调用就忽略
        if (!"call_contract_op_type".equals(operationType)) {
            return null;
        }

        JSONObject operationData = operationJson.getJSONObject("data");
        //不是游戏的合约id就忽略
        String contract = operationData.getString("contract").replace("ACT", "CON");
        if (!config.contractId.equals(contract)) {
            return null;
        }
        String resultTrxId =
            createTaskJson.getJSONArray("result").getJSONObject(1).getJSONObject("trx").getString("result_trx_id");
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(StringUtils.isEmpty(resultTrxId) ? trxId : resultTrxId);
        log.info("getTransaction|transaction_op_type|[blockId={}][trxId={}][result_trx_id={}]", blockNum, trxId, resultTrxId);
        String resultSignee = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_pretty_contract_transaction", jsonArray);
        JSONObject resultJson2 = JSONObject.parseObject(resultSignee).getJSONObject("result");
        Date trxTime = dealTime(resultJson2.getString("timestamp"));

        jsonArray = new JSONArray();
        jsonArray.add(blockNum);
        jsonArray.add(trxId);
        String data = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_events", jsonArray);
        JSONObject jsonObject = JSONObject.parseObject(data);
        JSONArray jsonArray1 = jsonObject.getJSONArray("result");
        JSONObject resultJson = new JSONObject();
        parseEventData(resultJson, jsonArray1);
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setContractId(contract);
        transactionDTO.setTrxId(trxId);
        transactionDTO.setEventParam(resultJson.getString("event_param"));
        transactionDTO.setEventType(resultJson.getString("event_type"));
        transactionDTO.setBlockNum(blockNum);
        transactionDTO.setTrxTime(trxTime);
        return transactionDTO;
    }


    @Override
    public String networkBroadcast(String message) {
        return  httpClient.post(config.walletUrl, config.rpcUser, "network_broadcast_transaction", message);
    }

    private void parseEventData(JSONObject result, JSONArray jsonArray1) {
        if (null != jsonArray1 && jsonArray1.size() > 0) {
            StringBuffer eventType = new StringBuffer();
            StringBuffer eventParam = new StringBuffer();
            jsonArray1.forEach(json -> {
                JSONObject jso = (JSONObject) json;
                eventType.append(eventType.length() > 0 ? "|" : "").append(jso.getString("event_type"));
                eventParam.append(eventParam.length() > 0 ? "|" : "").append(jso.getString("event_param"));
            });
            result.put("event_type", eventType);
            result.put("event_param", eventParam);
        }
    }

    private Date dealTime(String timestamp){
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            return format.parse(timestamp);
        } catch (ParseException e) {
            log.error("dealTime|error|",e);
            return null;
        }
    }

}
