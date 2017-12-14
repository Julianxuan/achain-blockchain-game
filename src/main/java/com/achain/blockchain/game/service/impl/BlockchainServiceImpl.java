package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.conf.Config;
import com.achain.blockchain.game.domain.dto.OfflineSignDTO;
import com.achain.blockchain.game.domain.dto.TransactionDTO;
import com.achain.blockchain.game.domain.enums.ContractGameMethod;
import com.achain.blockchain.game.domain.enums.TrxType;
import com.achain.blockchain.game.service.IBlockchainService;
import com.achain.blockchain.game.utils.SDKHttpClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ms.data.ACTPrivateKey;
import com.ms.data.Transaction;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
        log.info("BlockchainServiceImpl|getBlockCount 开始处理");
        String result =
            httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_block_count", new JSONArray());
        JSONObject createTaskJson = JSONObject.parseObject(result);
        return createTaskJson.getLong("result");
    }

    @Override
    public JSONArray getBlock(long blockNum) {
        log.info("BlockchainServiceImpl|getBlock 开始处理[{}]", blockNum);
        String result =
            httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_block", String.valueOf(blockNum));
        JSONObject createTaskJson = JSONObject.parseObject(result);
        return createTaskJson.getJSONObject("result").getJSONArray("user_transaction_ids");
    }


    /**
     * 需要判断交易类型，合约id，合约调用的方法和转账到的地址。
     *
     * @param trxId 交易单号
     */
    @Override
    public TransactionDTO getTransaction(long blockNum, String trxId) {
        log.info("BlockchainServiceImpl|getBlock 开始处理[{}]", trxId);
        String result = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_transaction", trxId);
        JSONObject createTaskJson = JSONObject.parseObject(result);
        JSONArray resultJsonArray = createTaskJson.getJSONArray("result");
        JSONObject operationJson = resultJsonArray.getJSONObject(1)
                                                  .getJSONObject("trx")
                                                  .getJSONArray("operations")
                                                  .getJSONObject(0);
        //判断交易类型
        String operationType = operationJson.getString("type");
        //不是合约调用就忽略
        if (!"transaction_op_type".equals(operationType)) {
            return null;
        }

        JSONObject operationData = operationJson.getJSONObject("data");
        log.info("BlockchainServiceImpl|operationData={}", operationData);

        String resultTrxId =
            resultJsonArray.getJSONObject(1).getJSONObject("trx").getString("result_trx_id");
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(StringUtils.isEmpty(resultTrxId) ? trxId : resultTrxId);
        log.info("getTransaction|transaction_op_type|[blockId={}][trxId={}][result_trx_id={}]", blockNum, trxId,
                 resultTrxId);
        String resultSignee =
            httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_pretty_contract_transaction", jsonArray);
        JSONObject resultJson2 = JSONObject.parseObject(resultSignee).getJSONObject("result");
        //和广播返回的统一
        String origTrxId = resultJson2.getString("orig_trx_id");
        Integer trxType = Integer.parseInt(resultJson2.getString("trx_type"));

        Date trxTime = dealTime(resultJson2.getString("timestamp"));
        JSONArray reserved = resultJson2.getJSONArray("reserved");
        JSONObject temp = resultJson2.getJSONObject("to_contract_ledger_entry");
        String contractId = temp.getString("to_account");
        //不是游戏的合约id就忽略
        if (!config.contractId.equals(contractId)) {
            return null;
        }
        TrxType type = TrxType.getTrxType(trxType);
        if (TrxType.TRX_TYPE_DEPOSIT_CONTRACT == type) {
            TransactionDTO transactionDTO = new TransactionDTO();
            transactionDTO.setTrxId(origTrxId);
            transactionDTO.setBlockNum(blockNum);
            transactionDTO.setTrxTime(trxTime);
            transactionDTO.setContractId(contractId);
            transactionDTO.setCallAbi(ContractGameMethod.RECHARGE.getValue());
            return transactionDTO;
        } else if (TrxType.TRX_TYPE_CALL_CONTRACT == type) {
            String fromAddr = temp.getString("from_account");
            Long amount = temp.getJSONObject("amount").getLong("amount");
            String callAbi = reserved.size() >= 1 ? reserved.getString(0) : null;
            String apiParams = reserved.size() > 1 ? reserved.getString(1) : null;
            //没有方法名
            if (StringUtils.isEmpty(callAbi)) {
                return null;
            }
            jsonArray = new JSONArray();
            jsonArray.add(blockNum);
            jsonArray.add(trxId);
            String data = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_events", jsonArray);
            JSONObject jsonObject = JSONObject.parseObject(data);
            JSONArray jsonArray1 = jsonObject.getJSONArray("result");
            JSONObject resultJson = new JSONObject();
            parseEventData(resultJson, jsonArray1);
            TransactionDTO transactionDTO = new TransactionDTO();
            transactionDTO.setContractId(contractId);
            transactionDTO.setTrxId(origTrxId);
            transactionDTO.setEventParam(resultJson.getString("event_param"));
            transactionDTO.setEventType(resultJson.getString("event_type"));
            transactionDTO.setBlockNum(blockNum);
            transactionDTO.setTrxTime(trxTime);
            transactionDTO.setCallAbi(callAbi);
            transactionDTO.setFromAddr(fromAddr);
            transactionDTO.setAmount(amount);
            transactionDTO.setApiParams(apiParams);
            return transactionDTO;
        }
        return null;
    }


    @Override
    public String networkBroadcast(String message) {
        return httpClient.post(config.walletUrl, config.rpcUser, "network_broadcast_transaction", message);
    }

    @Override
    public Map<String, String> offLineSign(OfflineSignDTO offlineSignDTO) {
        Map<String, String> map = new HashMap<>(3);
        String param = offlineSignDTO.getParam();
        offlineSignDTO.setParam(Optional.ofNullable(param).orElse("\"\""));
        String privateKey = offlineSignDTO.getPrivateKey();
        String method = offlineSignDTO.getMethod();
        String contractId = offlineSignDTO.getContractId();
        if (StringUtils.isEmpty(privateKey) || StringUtils.isEmpty(method) || StringUtils.isEmpty(contractId)) {
            map.put("msg", "param miss");
            map.put("code", "201");
            return map;
        }
        String errorMsg;
        try {
            Transaction trx = new Transaction(new ACTPrivateKey(privateKey), contractId, method, param, 5000L, true);
            map.put("msg", "success");
            map.put("code", "200");
            map.put("data", trx.toJSONString());
            return map;
        } catch (Exception e) {
            errorMsg = e.getMessage();
            log.error("offLineSign|error|offlineSignDTO={}", offlineSignDTO, e);
        }
        map.put("msg", errorMsg);
        map.put("code", "202");
        return map;
    }

    @Override
    public long getBalance(String actAddress) {
        try {
            JSONArray tempJson = new JSONArray();
            tempJson.add(actAddress);
            long result1 = 0L;
            String result =
                httpClient.post(config.walletUrl, config.rpcUser, "blockchain_list_address_balances", tempJson);
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    log.info(jsonArray.getJSONArray(i).toJSONString());
                    log.info(jsonArray.getJSONArray(i).getJSONObject(1).toJSONString());
                    result1 = result1 + jsonArray.getJSONArray(i).getJSONObject(1).getLong("balance");
                }
                return result1;
            }
        } catch (Exception e) {
            log.error("BlockchainServiceImpl|getBalance|[userAddress={}]出现异常", actAddress, e);
        }
        return 0L;
    }

    @Override
    public Map<String, String> offLineRechargeSign(OfflineSignDTO offlineSignDTO) {
        Map<String, String> map = new HashMap<>(3);
        String privateKey = offlineSignDTO.getPrivateKey();
        String contractId = offlineSignDTO.getContractId();
        String param = offlineSignDTO.getParam();
        if (StringUtils.isEmpty(privateKey) || StringUtils.isEmpty(contractId) || StringUtils.isEmpty(param)) {
            map.put("msg", "param miss");
            map.put("code", "201");
            return map;
        }

        String errorMsg;
        try {
            Transaction trx = new Transaction(
                new ACTPrivateKey(privateKey),
                contractId,
                10000L,
                (long) (Double.parseDouble(param) * 100000));
            map.put("msg", "success");
            map.put("code", "200");
            map.put("data", trx.toJSONString());
            return map;
        } catch (Exception e) {
            errorMsg = e.getMessage();
            log.error("offLineRechargeSign|error|offlineSignDTO={}", offlineSignDTO, e);
        }
        map.put("msg", errorMsg);
        map.put("code", "202");
        return map;
    }

    private void parseEventData(JSONObject result, JSONArray jsonArray1) {
        if (Objects.nonNull(jsonArray1) && jsonArray1.size() > 0) {
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

    private Date dealTime(String timestamp) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            return format.parse(timestamp);
        } catch (ParseException e) {
            log.error("dealTime|error|", e);
            return null;
        }
    }



}
