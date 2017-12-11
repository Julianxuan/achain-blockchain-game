package com.achain.blockchain.game.domain.entity;

import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author yujianjian
 * @since 2017-12-11 下午1:56
 */
@TableName("blockchain_record")
@Data
public class BlockchainRecord implements Serializable{

    private static final long serialVersionUID = 1432748946229084964L;

    private Integer id;

    /**
     * 交易所在块号
     */
    private Long blockNum;

    /**
     * 交易id
     */
    private String trxId;

    private String contractId;

    /**
     * 交易时间
     */
    private Date trxTime;

    private Date createTime;

    private Date updateTime;
}
