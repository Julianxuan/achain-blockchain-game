package com.achain.blockchain.game.domain.entity;

import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author yujianjian
 * @since 2017-12-12 下午3:35
 */
@TableName("blockchain_dog_info")
@Data
public class BlockchainDogInfo implements Serializable{

    private static final long serialVersionUID = 2919726852864028990L;

    private Integer id;
    private Integer dogId;
    private String nickname;
    private String owner;
    private String gene;
    private Date birthTime;
    private Date cooldownEndTime;
    private Integer motherId;
    private Integer fatherId;
    private Integer generation;
    /**是否可育,0-否,1-是*/
    private Integer fertility;
    private Date createTime;
    private Date updateTime;
    private String trxId;


}
