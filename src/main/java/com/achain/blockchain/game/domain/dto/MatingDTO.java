package com.achain.blockchain.game.domain.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * @author yujianjian
 * @since 2017-12-12 下午9:37
 */
@Data
public class MatingDTO implements Serializable{

    private static final long serialVersionUID = -8560544246284178490L;

    private String from_address;
    private Integer from_dog_id;
    /**
     * 卖方
     */
    private String to_address;
    /**
     * 卖方的狗
     */
    private Integer to_dog_id;
    private Long amount;
}
