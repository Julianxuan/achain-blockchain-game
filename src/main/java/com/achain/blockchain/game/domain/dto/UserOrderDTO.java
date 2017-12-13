package com.achain.blockchain.game.domain.dto;

import com.achain.blockchain.game.domain.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yujianjian
 * @since 2017-12-12 下午8:15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOrderDTO {

    private String trxId;
    private OrderStatus status;
    private String message;
    private String method;
}
