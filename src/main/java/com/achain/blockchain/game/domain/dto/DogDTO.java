package com.achain.blockchain.game.domain.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * @author yujianjian
 * @since 2017-12-12 下午4:19
 */
@Data
public class DogDTO implements Serializable {

    private static final long serialVersionUID = -713872090466225954L;

    private Integer id;
    private String owner;
    private String gene;
    private Long birth_time;
    private Long cooldown_end_block;
    private Integer mother_id;
    private Integer father_id;
    private Integer generation;
    private Boolean fertility;

}
