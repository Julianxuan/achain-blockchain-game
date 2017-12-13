package com.achain.blockchain.game.domain.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * @author yujianjian
 * @since 2017-12-13 上午10:05
 */
@Data
public class OfflineSignDTO implements Serializable{

    private static final long serialVersionUID = 9024112179692864084L;

    private String contractId;

    private String privateKey;

    private String method;

    private String param;


}
