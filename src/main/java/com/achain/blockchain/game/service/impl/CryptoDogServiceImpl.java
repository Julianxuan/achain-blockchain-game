package com.achain.blockchain.game.service.impl;

import com.achain.blockchain.game.conf.Config;
import com.achain.blockchain.game.domain.consts.CryptoDogEventType;
import com.achain.blockchain.game.domain.dto.AuctionDTO;
import com.achain.blockchain.game.domain.dto.DogDTO;
import com.achain.blockchain.game.domain.dto.MatingDTO;
import com.achain.blockchain.game.domain.dto.TransactionDTO;
import com.achain.blockchain.game.domain.dto.UserOrderDTO;
import com.achain.blockchain.game.domain.entity.BlockchainDogInfo;
import com.achain.blockchain.game.domain.entity.BlockchainDogMetingOrder;
import com.achain.blockchain.game.domain.entity.BlockchainDogOrder;
import com.achain.blockchain.game.domain.enums.ContractGameMethod;
import com.achain.blockchain.game.domain.enums.OrderStatus;
import com.achain.blockchain.game.service.IBlockchainDogInfoService;
import com.achain.blockchain.game.service.IBlockchainDogMetingOrderService;
import com.achain.blockchain.game.service.IBlockchainDogOrderService;
import com.achain.blockchain.game.service.IBlockchainDogUserOrderService;
import com.achain.blockchain.game.service.ICryptoDogService;
import com.achain.blockchain.game.utils.SymmetricEncoder;
import com.alibaba.fastjson.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-12-12 上午11:20
 */
@Service
@Slf4j
public class CryptoDogServiceImpl implements ICryptoDogService {

    private final static Long PER_BLOCK_TIME = 10_000L;

    @Autowired
    private Config config;
    @Autowired
    private IBlockchainDogInfoService blockchainDogInfoService;
    @Autowired
    private IBlockchainDogOrderService blockchainDogOrderService;
    @Autowired
    private IBlockchainDogUserOrderService blockchainDogUserOrderService;
    @Autowired
    private IBlockchainDogMetingOrderService blockchainDogMetingOrderService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void generateZeroDog(TransactionDTO transactionDTO) {
        log.info("generateZeroDog|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.GENERATE_SUCCESS.equals(eventType)) {
            String[] split = eventParam.split("\\|");
            DogDTO dogDTO = JSON.parseObject(split[0], DogDTO.class);
            AuctionDTO auctionDTO = JSON.parseObject(split[1], AuctionDTO.class);
            log.info("generateZeroDog|dogDTO={}|auctionDTO={}", dogDTO, auctionDTO);
            if (Objects.isNull(dogDTO) || Objects.isNull(auctionDTO)) {
                log.error("generateZeroDog|error|params miss");
                return;
            }

            String gene = SymmetricEncoder.AESDncode(config.encodeRules, dogDTO.getGene());
            long coolDown = transactionDTO.getTrxTime().getTime() + PER_BLOCK_TIME * dogDTO.getCooldown_end_block();

            BlockchainDogInfo blockchainDogInfo = new BlockchainDogInfo();
            blockchainDogInfo.setDogId(dogDTO.getId());
            blockchainDogInfo.setOwner(dogDTO.getOwner());
            blockchainDogInfo.setGene(gene);
            blockchainDogInfo.setBirthTime(new Date(dogDTO.getBirth_time()));
            blockchainDogInfo.setCooldownEndTime(new Date(coolDown));
            blockchainDogInfo.setMotherId(dogDTO.getMother_id());
            blockchainDogInfo.setFatherId(dogDTO.getFather_id());
            blockchainDogInfo.setGeneration(dogDTO.getGeneration());
            blockchainDogInfo.setFertility(dogDTO.getFertility() ? 1 : 0);
            blockchainDogInfo.setIsPregnant(dogDTO.getIs_pregnant() ? 1 : 0);
            blockchainDogInfoService.insert(blockchainDogInfo);

            long endTime = transactionDTO.getTrxTime().getTime() + auctionDTO.getDuration() * PER_BLOCK_TIME;
            BlockchainDogOrder blockchainDogOrder = new BlockchainDogOrder();
            blockchainDogOrder.setSeller(split[2]);
            blockchainDogOrder.setDogId(auctionDTO.getTokenId());
            blockchainDogOrder.setStatus(OrderStatus.ON.getIntKey());
            blockchainDogOrder.setOrderId(auctionDTO.getTrx_id());
            blockchainDogOrder.setStartingPrice(auctionDTO.getStartingPrice());
            blockchainDogOrder.setEndingPrice(auctionDTO.getEndingPrice());
            blockchainDogOrder.setBeginTime(transactionDTO.getTrxTime());
            blockchainDogOrder.setEndTime(new Date(endTime));
            blockchainDogOrder.setTrxId(transactionDTO.getTrxId());
            blockchainDogOrderService.insert(blockchainDogOrder);

            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.SUCCESS)
                                                    .method(ContractGameMethod.GENERATE_ZERO_DOG.getValue())
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        } else {
            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.FAIL)
                                                    .method(ContractGameMethod.GENERATE_ZERO_DOG.getValue())
                                                    .errorMessage(eventParam)
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        }

    }

    @Override
    public void bid(TransactionDTO transactionDTO) {
        log.info("bid|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.BID_SUCCESS.equals(eventType)) {
            DogDTO dogDTO = JSON.parseObject(eventParam, DogDTO.class);
            if (Objects.isNull(dogDTO)) {
                return;
            }
            List<BlockchainDogOrder> list =
                blockchainDogOrderService.listByDogIdAndStatus(dogDTO.getId(), OrderStatus.ON);
            if (list.size() == 0) {
                return;
            }
            BlockchainDogOrder blockchainDogOrder = list.get(0);
            blockchainDogOrder.setStatus(OrderStatus.SUCCESS.getIntKey());
            blockchainDogOrder.setBuyer(transactionDTO.getFromAddr());
            blockchainDogOrder.setTransPrice(transactionDTO.getAmount());
            blockchainDogOrderService.updateById(blockchainDogOrder);
            BlockchainDogInfo dogInfo = blockchainDogInfoService.getByDogId(dogDTO.getId());
            dogInfo.setOwner(blockchainDogOrder.getBuyer());
            blockchainDogInfoService.updateById(dogInfo);

            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.SUCCESS)
                                                    .method(ContractGameMethod.SALES_BID.getValue())
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        } else {
            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.FAIL)
                                                    .method(ContractGameMethod.SALES_BID.getValue())
                                                    .errorMessage(eventParam)
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        }
    }


    @Override
    public void addAuction(TransactionDTO transactionDTO) {
        log.info("addAuction|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.ADD_AUCTION_SUCCESS.equals(eventType)) {
            AuctionDTO auctionDTO = JSON.parseObject(eventParam, AuctionDTO.class);
            long endTime = transactionDTO.getTrxTime().getTime() + auctionDTO.getDuration() * PER_BLOCK_TIME;
            BlockchainDogOrder blockchainDogOrder = new BlockchainDogOrder();
            blockchainDogOrder.setSeller(transactionDTO.getFromAddr());
            blockchainDogOrder.setDogId(auctionDTO.getTokenId());
            blockchainDogOrder.setStatus(OrderStatus.ON.getIntKey());
            blockchainDogOrder.setOrderId(auctionDTO.getTrx_id());
            blockchainDogOrder.setStartingPrice(auctionDTO.getStartingPrice());
            blockchainDogOrder.setEndingPrice(auctionDTO.getEndingPrice());
            blockchainDogOrder.setBeginTime(transactionDTO.getTrxTime());
            blockchainDogOrder.setEndTime(new Date(endTime));
            blockchainDogOrder.setTrxId(transactionDTO.getTrxId());
            blockchainDogOrderService.insert(blockchainDogOrder);

            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.SUCCESS)
                                                    .method(ContractGameMethod.SALES_ADD_AUCTION.getValue())
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        } else {
//            String apiParams = transactionDTO.getApiParams();
//            if (StringUtils.isEmpty(apiParams)) {
//                return;
//            }
//            String[] callParams = apiParams.split("\\|");
//            Integer expectLength = 4;
//            if (callParams.length < expectLength) {
//                return;
//            }
//            AuctionDTO auctionDTO = getAuction(callParams);
//            if (Objects.isNull(auctionDTO)) {
//                return;
//            }
//            long endTime = transactionDTO.getTrxTime().getTime() + auctionDTO.getDuration() * PER_BLOCK_TIME;
//            BlockchainDogOrder blockchainDogOrder = new BlockchainDogOrder();
//            blockchainDogOrder.setSeller(transactionDTO.getFromAddr());
//            blockchainDogOrder.setDogId(auctionDTO.getTokenId());
//            blockchainDogOrder.setStatus(OrderStatus.FAIL.getIntKey());
//            blockchainDogOrder.setOrderId(auctionDTO.getTrx_id());
//            blockchainDogOrder.setStartingPrice(auctionDTO.getStartingPrice());
//            blockchainDogOrder.setEndingPrice(auctionDTO.getEndingPrice());
//            blockchainDogOrder.setBeginTime(transactionDTO.getTrxTime());
//            blockchainDogOrder.setEndTime(new Date(endTime));
//            blockchainDogOrder.setTrxId(transactionDTO.getTrxId());
//            blockchainDogOrderService.insert(blockchainDogOrder);
            //更新订单
            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.FAIL)
                                                    .method(ContractGameMethod.SALES_ADD_AUCTION.getValue())
                                                    .errorMessage(eventParam)
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        }
    }


    @Override
    public void cancelAuction(TransactionDTO transactionDTO) {
        log.info("cancelAuction|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.CANCEL_AUCTION_SUCCESS.equals(eventType)) {
            int tokenId = Integer.parseInt(eventParam);
            List<BlockchainDogOrder> list =
                blockchainDogOrderService.listByDogIdAndStatus(tokenId, OrderStatus.ON);
            if (list.size() == 0) {
                return;
            }
            BlockchainDogOrder blockchainDogOrder = list.get(0);
            blockchainDogOrder.setStatus(OrderStatus.CANCEL.getIntKey());
            blockchainDogOrderService.updateById(blockchainDogOrder);

            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.SUCCESS)
                                                    .method(ContractGameMethod.SALES_CANCEL_AUCTION.getValue())
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        } else {
            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.FAIL)
                                                    .method(ContractGameMethod.SALES_CANCEL_AUCTION.getValue())
                                                    .errorMessage(eventParam)
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        }
    }

    @Override
    public void gift(TransactionDTO transactionDTO) {
        log.info("gift|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.GIFT_SUCCESS.equals(eventType)) {
            DogDTO dogDTO = JSON.parseObject(eventParam, DogDTO.class);
            if (Objects.nonNull(dogDTO)) {
                String newOwner = dogDTO.getOwner();
                BlockchainDogInfo dogInfo = blockchainDogInfoService.getByDogId(dogDTO.getId());
                dogInfo.setOwner(newOwner);
                blockchainDogInfoService.updateById(dogInfo);
                UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                        .trxId(transactionDTO.getTrxId())
                                                        .status(OrderStatus.SUCCESS)
                                                        .method(ContractGameMethod.GIFT.getValue())
                                                        .build();
                blockchainDogUserOrderService.updateTrx(userOrderDTO);
            }
        } else {
            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.FAIL)
                                                    .method(ContractGameMethod.GIFT.getValue())
                                                    .errorMessage(eventParam)
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        }
    }

    @Override
    public void addMatingTransaction(TransactionDTO transactionDTO) {
        log.info("addMatingTransaction|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.ADD_MATING_SUCCESS.equals(eventType)) {
            AuctionDTO auctionDTO = JSON.parseObject(eventParam, AuctionDTO.class);
            if (Objects.nonNull(auctionDTO)) {
                long endTime = transactionDTO.getTrxTime().getTime() + auctionDTO.getDuration() * PER_BLOCK_TIME;
                BlockchainDogMetingOrder blockchainDogMetingOrder = new BlockchainDogMetingOrder();
                blockchainDogMetingOrder.setSeller(transactionDTO.getFromAddr());
                blockchainDogMetingOrder.setSellerDogId(auctionDTO.getTokenId());
                blockchainDogMetingOrder.setStatus(OrderStatus.ON.getIntKey());
                blockchainDogMetingOrder.setOrderId(auctionDTO.getTrx_id());
                blockchainDogMetingOrder.setStartingPrice(auctionDTO.getStartingPrice());
                blockchainDogMetingOrder.setEndingPrice(auctionDTO.getEndingPrice());
                blockchainDogMetingOrder.setBeginTime(transactionDTO.getTrxTime());
                blockchainDogMetingOrder.setEndTime(new Date(endTime));
                blockchainDogMetingOrder.setTrxId(transactionDTO.getTrxId());
                blockchainDogMetingOrderService.insert(blockchainDogMetingOrder);

                UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                        .trxId(transactionDTO.getTrxId())
                                                        .status(OrderStatus.SUCCESS)
                                                        .method(ContractGameMethod.MATING_ADD_AUCTION.getValue())
                                                        .build();
                blockchainDogUserOrderService.updateTrx(userOrderDTO);
            }
        } else {
            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.FAIL)
                                                    .method(ContractGameMethod.MATING_ADD_AUCTION.getValue())
                                                    .errorMessage(eventParam)
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);

        }

    }

    @Override
    public void cancelMatingTransaction(TransactionDTO transactionDTO) {
        log.info("cancelMatingTransaction|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.CANCEL_MATING_SUCCESS.equals(eventType)) {
            int tokenId = Integer.parseInt(eventParam);
            List<BlockchainDogMetingOrder> list =
                blockchainDogMetingOrderService.listByDogIdAndStatus(tokenId, OrderStatus.ON);
            if (list.size() == 0) {
                return;
            }
            BlockchainDogMetingOrder blockchainDogMetingOrder = list.get(0);
            blockchainDogMetingOrder.setStatus(OrderStatus.CANCEL.getIntKey());
            blockchainDogMetingOrderService.updateById(blockchainDogMetingOrder);

            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.SUCCESS)
                                                    .method(ContractGameMethod.MATING_CANCEL_AUCTION.getValue())
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        } else {
            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.FAIL)
                                                    .method(ContractGameMethod.MATING_CANCEL_AUCTION.getValue())
                                                    .errorMessage(eventParam)
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        }
    }

    @Override
    public void matingTransfer(TransactionDTO transactionDTO) {
        log.info("matingTransfer|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        if (CryptoDogEventType.CANCEL_MATING_SUCCESS.equals(eventType)) {
            MatingDTO matingDTO = JSON.parseObject(eventParam, MatingDTO.class);
            if(Objects.isNull(matingDTO)){
                return;
            }
            List<BlockchainDogMetingOrder> list =
                blockchainDogMetingOrderService.listByDogIdAndStatus(matingDTO.getTo_dog_id(), OrderStatus.ON);
            if (list.size() == 0) {
                return;
            }
            BlockchainDogMetingOrder blockchainDogMetingOrder = list.get(0);
            blockchainDogMetingOrder.setBuyer(matingDTO.getFrom_address());
            blockchainDogMetingOrder.setBuyerDogId(matingDTO.getFrom_dog_id());
            blockchainDogMetingOrder.setStatus(OrderStatus.SUCCESS.getIntKey());
            blockchainDogMetingOrder.setTransPrice(matingDTO.getAmount());
            blockchainDogMetingOrderService.updateById(blockchainDogMetingOrder);

            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.SUCCESS)
                                                    .method(ContractGameMethod.MATING_BID.getValue())
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        }else {
            UserOrderDTO userOrderDTO = UserOrderDTO.builder()
                                                    .trxId(transactionDTO.getTrxId())
                                                    .status(OrderStatus.FAIL)
                                                    .method(ContractGameMethod.MATING_BID.getValue())
                                                    .errorMessage(eventParam)
                                                    .build();
            blockchainDogUserOrderService.updateTrx(userOrderDTO);
        }
    }


    @Override
    public void changeFee(TransactionDTO transactionDTO) {

    }

    @Override
    public void withdrawBenefit(TransactionDTO transactionDTO) {

    }

    @Override
    public void queryDog(TransactionDTO transactionDTO) {

    }

    @Override
    public void changeCFO(TransactionDTO transactionDTO) {

    }

    @Override
    public void changeCOO(TransactionDTO transactionDTO) {

    }

    @Override
    public void breeding(TransactionDTO transactionDTO) {

    }

    private AuctionDTO getAuction(String[] callParams) {
        try {
            AuctionDTO auctionDTO = new AuctionDTO();
            Integer tokenId = Integer.parseInt(callParams[0]);
            BigDecimal startPrice = new BigDecimal(callParams[1]).multiply(new BigDecimal(100_000));
            BigDecimal endPrice = new BigDecimal(callParams[2]).multiply(new BigDecimal(100_000));
            Long startingPrice = (long) Double.parseDouble(startPrice.toString());
            Long endingPrice = (long) Double.parseDouble(endPrice.toString());
            Long duration = Long.parseLong(callParams[3]);
            auctionDTO.setTokenId(tokenId);
            auctionDTO.setEndingPrice(startingPrice);
            auctionDTO.setEndingPrice(endingPrice);
            auctionDTO.setDuration(duration);
            return auctionDTO;
        } catch (NumberFormatException e) {
            log.error("addAuction|error|", e);
        }
        return null;
    }
}
