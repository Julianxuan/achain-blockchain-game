package com.achain.blockchain.game.service;

import com.achain.blockchain.game.domain.dto.OfflineSignDTO;
import com.achain.blockchain.game.utils.HttpUtils;
import com.alibaba.fastjson.JSON;

/**
 * @author yujianjian
 * @since 2017-12-13 下午7:45
 */
public class InterfaceTest {

    private String basicUrl = "http://127.0.0.1:8340/api/act/";

    public static void main(String[] args) throws Exception {
        InterfaceTest test = new InterfaceTest();
        //String result = test.getBalance();
        //String result = test.networkBroadcastTransaction();
        String result = test.offLineSign();
        System.out.println(result);
    }


    public String networkBroadcastTransaction() throws Exception {
        String url = basicUrl + "network_broadcast_transaction";
        String result = HttpUtils.broadcastPost(url, "afdsaf");
        return result;
    }


    public String offLineSign() throws Exception {
        String url = basicUrl + "offline/sign";
        OfflineSignDTO offlineSignDTO = new OfflineSignDTO();
        offlineSignDTO.setParam("aa|bb");
        offlineSignDTO.setMethod("grab");
        offlineSignDTO.setContractId("fdaf");
        offlineSignDTO.setPrivateKey("fdsafsdafas");
        String result = HttpUtils.postJson(url, JSON.toJSONString(offlineSignDTO));
        return result;
    }


    public String getBalance() throws Exception {
        String url = basicUrl + "balance?actAddress=fasdfas";
        String result = HttpUtils.get(url);
        return result;
    }
}
