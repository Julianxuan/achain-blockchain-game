package com.achain.blockchain.game.domain.entity;

import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author yujianjian
 * @since 2017-12-12 下午7:40
 */
@TableName("blockchain_dog_user_order")
@Data
public class BlockchainDogUserOrder implements Serializable {

    private static final long serialVersionUID = -9047287224950493382L;

    private Integer id;
    private String trxId;
    private Integer status;
    private String message;
    private String method;
    private Date createTime;
    private Date updateTime;


}
