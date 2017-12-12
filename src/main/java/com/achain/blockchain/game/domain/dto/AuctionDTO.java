package com.achain.blockchain.game.domain.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * @author yujianjian
 * @since 2017-12-12 下午4:23
 */
@Data
public class AuctionDTO implements Serializable {

    private static final long serialVersionUID = -8489898062224677828L;

    private Integer tokenId;
    private Long startingPrice;
    private Long endingPrice;
    private Long duration;
    /**订单号*/
    private String trx_id;

}
