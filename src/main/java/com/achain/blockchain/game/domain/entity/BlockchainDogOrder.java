package com.achain.blockchain.game.domain.entity;

import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author yujianjian
 * @since 2017-12-12 下午3:36
 */
@TableName("blockchain_dog_order")
@Data
public class BlockchainDogOrder implements Serializable{

    private static final long serialVersionUID = -5658403517282904661L;

    private Integer id;
    private Integer dogId;
    private String orderId;
    private String seller;
    private String buyer;
    private Long startingPrice;
    private Long endingPrice;
    private Long transPrice;
    /**订单状态,0-进行中,1-交易成功,2-交易取消,3-交易失效*/
    private Integer status;
    private String trxId;
    private Date beginTime;
    private Date endTime;
    private Date createTime;
    private Date updateTime;



}
