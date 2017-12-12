package com.achain.blockchain.game.domain.enums;

/**
 * @author yujianjian
 * @since 2017-12-12 下午3:27
 */
public enum OrderStatus {

    ON(0, "订单进行中"),
    SUCCESS(1, "成功"),
    CANCEL(2, "订单取消"),
    EXPIRE(3, "订单失效"),
    FAIL(4,"订单失败");


    private final int key;
    private final String desc;

    OrderStatus(int key, String desc) {
        this.key = key;
        this.desc = desc;
    }


    public int getIntKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }


}
