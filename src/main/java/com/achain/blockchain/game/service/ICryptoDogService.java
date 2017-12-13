package com.achain.blockchain.game.service;

import com.achain.blockchain.game.domain.dto.TransactionDTO;

/**
 * @author yujianjian
 * @since 2017-12-12 上午11:20
 */
public interface ICryptoDogService {


    /**
     * 处理生成零代狗rpc方法的结果数据解析
     *
     * @param transactionDTO 原始数据
     */
    void generateZeroDog(TransactionDTO transactionDTO);

    /**
     * 买卖加密狗交易方法
     *
     * @param transactionDTO 原始数据
     */
    void bid(TransactionDTO transactionDTO);

    /**
     * 买卖加密狗下订单方法
     *
     * @param transactionDTO 原始数据
     */
    void addAuction(TransactionDTO transactionDTO);

    /**
     * 买卖加密狗取消订单方法
     *
     * @param transactionDTO 原始数据
     */
    void cancelAuction(TransactionDTO transactionDTO);

    /**
     * 赠送加密狗方法
     * @param transactionDTO 原始数据
     */
    void gift(TransactionDTO transactionDTO);

    /**
     * 繁衍挂单
     * @param transactionDTO 原始数据
     */
    void addMatingTransaction(TransactionDTO transactionDTO);

    /**
     * 取消繁衍的单子
     * @param transactionDTO 原始数据
     */
    void cancelMatingTransaction(TransactionDTO transactionDTO);

    /**
     * 繁衍下单
     * @param transactionDTO 原始数据
     */
    void matingTransfer(TransactionDTO transactionDTO);

    /**
     * 更改费率,只有CFO可以调用
     * @param transactionDTO 原始数据
     */
    void changeFee(TransactionDTO transactionDTO);

    /**
     * 提现,只有CFO可以调用
     * @param transactionDTO　原始数据
     */
    void withdrawBenefit(TransactionDTO transactionDTO);

    /**
     * 查询狗的信息
     * @param transactionDTO 原始数据
     */
    void queryDog(TransactionDTO transactionDTO);

    /**
     * 更改CFO,只有CEO能调用
     * @param transactionDTO 原始数据
     */
    void changeCFO(TransactionDTO transactionDTO);

    /**
     * 更改COO,只有CEO能调用
     * @param transactionDTO 原始数据
     */
    void changeCOO(TransactionDTO transactionDTO);

    /**
     * 繁衍后生成狗,只有COO能调用
     * @param transactionDTO 原始数据
     */
    void breeding(TransactionDTO transactionDTO);

    /**
     * 用户合约充值
     * @param transactionDTO 原始数据
     */
    void recharge(TransactionDTO transactionDTO);
}
