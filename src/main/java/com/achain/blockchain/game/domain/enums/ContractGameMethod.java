package com.achain.blockchain.game.domain.enums;

import java.util.Arrays;

/**
 * @author yujianjian
 * @since 2017-12-11 下午7:56
 */
public enum ContractGameMethod {
    /**
     *
     */
    GENERATE_ZERO_DOG(0, "generate_zero_dog", "生成零代狗"),
    SALES_BID(1, "bid", "买卖交易"),
    SALES_ADD_AUCTION(2, "addAuction", "买卖下单"),
    SALES_CANCEL_AUCTION(3, "cancelAuction", "买卖取消单子"),
    QUERY_DOG(4, "query_dog", "查询狗的信息"),
    CHANGE_CFO(5, "change_CFO", "更改CFO"),
    CHANGE_COO(6, "change_COO", "更改COO"),
    MATING_ADD_AUCTION(7, "addMatingTransaction", "繁衍挂单"),
    MATING_BID(8, "mating_transfer", "繁衍单子交易"),
    BREEDING(9, "breeding", "繁衍后生成的狗"),
    MATING_CANCEL_AUCTION(10, "cancelMatingTransaction", "取消繁衍的单子"),
    CHANGE_FEE(11,"change_fee","更改费率的方法"),
    WITHDRAW_BENEFIT(12,"withdraw_benefit","提现方法");


    private final int key;
    private final String value;
    private final String desc;

    ContractGameMethod(int key, String value, String desc) {
        this.key = key;
        this.value = value;
        this.desc = desc;
    }


    public int getIntKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    public String getValue() {
        return value;
    }

    public static ContractGameMethod getMethod(String value) {
        return Arrays.stream(ContractGameMethod.values())
                     .filter(contractGameMethod -> contractGameMethod.value.equals(value))
                     .findFirst().orElse(null);
    }

}
