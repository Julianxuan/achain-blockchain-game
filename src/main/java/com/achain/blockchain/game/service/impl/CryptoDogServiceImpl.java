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

    private final static Long PER_BLOCK_TIME_MS = 10_000L;

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

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(),
                                                    OrderStatus.FAIL,
                                                    ContractGameMethod.GENERATE_ZERO_DOG,
                                                    eventParam);
        if (CryptoDogEventType.GENERATE_SUCCESS.equals(eventType)) {
            String[] split = eventParam.split("\\|");
            DogDTO dogDTO = JSON.parseObject(split[0], DogDTO.class);
            AuctionDTO auctionDTO = JSON.parseObject(split[1], AuctionDTO.class);
            log.info("generateZeroDog|dogDTO={}|auctionDTO={}", dogDTO, auctionDTO);
            if (Objects.isNull(dogDTO) || Objects.isNull(auctionDTO)) {
                log.error("generateZeroDog|error|params miss");
                return;
            }

            String gene = SymmetricEncoder.aesDecode(config.encodeRules, dogDTO.getGene());
            long coolBlockNum = dogDTO.getCooldown_end_block() - transactionDTO.getBlockNum() < 0
                                ? 0 : (dogDTO.getCooldown_end_block() - transactionDTO.getBlockNum());
            long coolDown = transactionDTO.getTrxTime().getTime() + PER_BLOCK_TIME_MS * coolBlockNum;

            insertNewZeroDog(transactionDTO, dogDTO, gene, coolDown);

            bidZeroDogOrder(transactionDTO, dogDTO, auctionDTO);

            userOrderDTO.setMessage(null);
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }


    @Override
    public void bid(TransactionDTO transactionDTO) {
        log.info("bid|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(),
                                                    OrderStatus.FAIL,
                                                    ContractGameMethod.SALES_BID,
                                                    eventParam);
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

            userOrderDTO.setMessage(null);
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addAuction(TransactionDTO transactionDTO) {
        log.info("addAuction|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(),
                                                    OrderStatus.FAIL,
                                                    ContractGameMethod.SALES_ADD_AUCTION,
                                                    eventParam);
        if (CryptoDogEventType.ADD_AUCTION_SUCCESS.equals(eventType)) {
            AuctionDTO auctionDTO = JSON.parseObject(eventParam, AuctionDTO.class);
            long endTime = transactionDTO.getTrxTime().getTime() + auctionDTO.getDuration() * PER_BLOCK_TIME_MS;
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

            userOrderDTO.setMessage(null);
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }


    @Override
    public void cancelAuction(TransactionDTO transactionDTO) {
        log.info("cancelAuction|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(),
                                                    OrderStatus.FAIL,
                                                    ContractGameMethod.SALES_CANCEL_AUCTION,
                                                    eventParam);
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

            userOrderDTO.setMessage(null);
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }

    @Override
    public void gift(TransactionDTO transactionDTO) {
        log.info("gift|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(),
                                                    OrderStatus.FAIL,
                                                    ContractGameMethod.GIFT,
                                                    eventParam);
        if (CryptoDogEventType.GIFT_SUCCESS.equals(eventType)) {
            DogDTO dogDTO = JSON.parseObject(eventParam, DogDTO.class);
            if (Objects.nonNull(dogDTO)) {
                String newOwner = dogDTO.getOwner();
                BlockchainDogInfo dogInfo = blockchainDogInfoService.getByDogId(dogDTO.getId());
                dogInfo.setOwner(newOwner);
                blockchainDogInfoService.updateById(dogInfo);

                userOrderDTO.setMessage(null);
                userOrderDTO.setStatus(OrderStatus.SUCCESS);
            }
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addMatingTransaction(TransactionDTO transactionDTO) {
        log.info("addMatingTransaction|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(),
                                                    OrderStatus.FAIL,
                                                    ContractGameMethod.MATING_ADD_AUCTION,
                                                    eventParam);
        if (CryptoDogEventType.ADD_MATING_SUCCESS.equals(eventType)) {
            AuctionDTO auctionDTO = JSON.parseObject(eventParam, AuctionDTO.class);
            if (Objects.nonNull(auctionDTO)) {
                long endTime = transactionDTO.getTrxTime().getTime() + auctionDTO.getDuration() * PER_BLOCK_TIME_MS;
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

                userOrderDTO.setMessage(null);
                userOrderDTO.setStatus(OrderStatus.SUCCESS);
            }
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }

    @Override
    public void cancelMatingTransaction(TransactionDTO transactionDTO) {
        log.info("cancelMatingTransaction|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(), OrderStatus.FAIL, ContractGameMethod.MATING_CANCEL_AUCTION, eventParam);
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

            userOrderDTO.setMessage(null);
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void matingTransfer(TransactionDTO transactionDTO) {
        log.info("matingTransfer|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(), OrderStatus.FAIL, ContractGameMethod.MATING_BID, eventParam);
        if (CryptoDogEventType.BID_MATING_SUCCESS.equals(eventType)) {
            String[] split = eventParam.split("\\|");
            MatingDTO matingDTO = JSON.parseObject(split[1], MatingDTO.class);
            DogDTO newDog = JSON.parseObject(split[0], DogDTO.class);
            if (Objects.isNull(matingDTO) || Objects.isNull(newDog)) {
                return;
            }
            List<BlockchainDogMetingOrder> list =
                blockchainDogMetingOrderService.listByDogIdAndStatus(matingDTO.getTo_dog_id(), OrderStatus.ON);
            if (list.size() == 0) {
                return;
            }
            String apiParams = transactionDTO.getApiParams();
            String[] callParams = apiParams.split("\\|");
            String fromDogGene = callParams[1];
            String toDogGene = callParams[3];
            Long cooldown = matingDTO.getCooldown();
            long parentCoolBlockNum = cooldown - transactionDTO.getBlockNum() < 0
                                ? 0 : (cooldown - transactionDTO.getBlockNum());
            long parentCoolDownMs = transactionDTO.getTrxTime().getTime() + PER_BLOCK_TIME_MS * parentCoolBlockNum;
            Date parentCoolDown = new Date(parentCoolDownMs);
            updateGeneOfFatherAndMother(matingDTO.getFrom_dog_id(), fromDogGene, matingDTO.getTo_dog_id(), toDogGene,parentCoolDown);
            BlockchainDogMetingOrder blockchainDogMetingOrder = list.get(0);
            blockchainDogMetingOrder.setBuyer(matingDTO.getFrom_address());
            blockchainDogMetingOrder.setBuyerDogId(matingDTO.getFrom_dog_id());
            blockchainDogMetingOrder.setStatus(OrderStatus.SUCCESS.getIntKey());
            blockchainDogMetingOrder.setTransPrice(matingDTO.getAmount());
            blockchainDogMetingOrderService.updateById(blockchainDogMetingOrder);

            String gene = SymmetricEncoder.aesDecode(config.encodeRules, newDog.getGene());
            long coolBlockNum = newDog.getCooldown_end_block() - transactionDTO.getBlockNum() < 0
                                ? 0 : (newDog.getCooldown_end_block() - transactionDTO.getBlockNum());
            long coolDown = transactionDTO.getTrxTime().getTime() + PER_BLOCK_TIME_MS * coolBlockNum;

            insertNewZeroDog(transactionDTO, newDog, gene, coolDown);

            userOrderDTO.setMessage(null);
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }


    @Override
    public void changeFee(TransactionDTO transactionDTO) {
        log.info("changeFee|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(), OrderStatus.FAIL, ContractGameMethod.CHANGE_FEE, eventParam);
        if (CryptoDogEventType.CHANGE_FEE_SUCCESS.equals(eventType)) {
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }

    @Override
    public void withdrawBenefit(TransactionDTO transactionDTO) {
        log.info("withdrawBenefit|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(), OrderStatus.FAIL, ContractGameMethod.WITHDRAW_BENEFIT, eventParam);
        if (CryptoDogEventType.WITHDRAW_BENEFIT_SUCCESS.equals(eventType)) {
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }

    @Override
    public void queryDog(TransactionDTO transactionDTO) {
        log.info("queryDog|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(), OrderStatus.FAIL, ContractGameMethod.QUERY_DOG, eventParam);
        if (CryptoDogEventType.QUERY_DOG_SUCCESS.equals(eventType)) {
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }

    @Override
    public void changeCFO(TransactionDTO transactionDTO) {
        log.info("changeCFO|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(), OrderStatus.FAIL, ContractGameMethod.CHANGE_CFO, eventParam);
        if (CryptoDogEventType.CHANGE_CFO_SUCCESS.equals(eventType)) {
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }

    @Override
    public void changeCOO(TransactionDTO transactionDTO) {
        log.info("changeCOO|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();

        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(), OrderStatus.FAIL, ContractGameMethod.CHANGE_COO, eventParam);
        if (CryptoDogEventType.CHANGE_COO_SUCCESS.equals(eventType)) {
            userOrderDTO.setStatus(OrderStatus.SUCCESS);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void breeding(TransactionDTO transactionDTO) {
        log.info("breeding|transactionDTO={}", transactionDTO);
        String eventType = transactionDTO.getEventType();
        String eventParam = transactionDTO.getEventParam();
        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(), OrderStatus.FAIL, ContractGameMethod.BREEDING, eventParam);
        if (CryptoDogEventType.BREEDING_SUCCESS.equals(eventType)) {
            String[] split = eventParam.split("\\|");
            Integer fromDogId = Integer.parseInt(split[0]);
            String fromDogGene = split[1];
            Integer toDogId = Integer.parseInt(split[2]);
            String toDogGene = split[3];

            updateGeneOfFatherAndMother(fromDogId, fromDogGene, toDogId, toDogGene,null);

            DogDTO newDog = JSON.parseObject(split[4], DogDTO.class);
            String gene = SymmetricEncoder.aesDecode(config.encodeRules, newDog.getGene());
            long coolDown = transactionDTO.getTrxTime().getTime() + PER_BLOCK_TIME_MS * newDog.getCooldown_end_block();

            BlockchainDogInfo blockchainDogInfo = new BlockchainDogInfo();
            blockchainDogInfo.setDogId(newDog.getId());
            blockchainDogInfo.setOwner(newDog.getOwner());
            blockchainDogInfo.setGene(gene);
            blockchainDogInfo.setBirthTime(new Date(newDog.getBirth_time()));
            blockchainDogInfo.setCooldownEndTime(new Date(coolDown));
            blockchainDogInfo.setMotherId(newDog.getMother_id());
            blockchainDogInfo.setFatherId(newDog.getFather_id());
            blockchainDogInfo.setGeneration(newDog.getGeneration());
            blockchainDogInfo.setFertility(newDog.getFertility() ? 1 : 0);
            blockchainDogInfoService.insert(blockchainDogInfo);

            userOrderDTO.setStatus(OrderStatus.SUCCESS);
            userOrderDTO.setMessage(null);
        }
        blockchainDogUserOrderService.updateTrx(userOrderDTO);
    }


    @Override
    public void recharge(TransactionDTO transactionDTO) {
        log.info("recharge|transactionDTO={}", transactionDTO);
        UserOrderDTO userOrderDTO = getUserOrderDTO(transactionDTO.getTrxId(), OrderStatus.SUCCESS, ContractGameMethod.RECHARGE, null);
        blockchainDogUserOrderService.updateRecharge(userOrderDTO);
    }

    private UserOrderDTO getUserOrderDTO(String trxId, OrderStatus orderStatus, ContractGameMethod contractGameMethod,
                                         String message) {
        return UserOrderDTO.builder()
                           .trxId(trxId)
                           .status(orderStatus)
                           .method(contractGameMethod.getValue())
                           .message(message)
                           .build();
    }

    /**
     * 更改父母狗的基因
     * @param fromDogId 父狗id
     * @param fromDogGene 父狗基因
     * @param toDogId 母狗id
     * @param toDogGene 母狗基因
     */
    private void updateGeneOfFatherAndMother(Integer fromDogId, String fromDogGene, Integer toDogId, String toDogGene,Date coolDown) {
        BlockchainDogInfo fromDog = blockchainDogInfoService.getByDogId(fromDogId);
        fromDogGene = SymmetricEncoder.aesDecode(config.encodeRules, fromDogGene);
        fromDog.setGene(fromDogGene);
        fromDog.setCooldownEndTime(coolDown);
        blockchainDogInfoService.updateById(fromDog);

        BlockchainDogInfo toDog = blockchainDogInfoService.getByDogId(toDogId);
        toDogGene = SymmetricEncoder.aesDecode(config.encodeRules, toDogGene);
        toDog.setGene(toDogGene);
        toDog.setCooldownEndTime(coolDown);
        blockchainDogInfoService.updateById(toDog);
    }

    private void bidZeroDogOrder(TransactionDTO transactionDTO, DogDTO dogDTO, AuctionDTO auctionDTO) {
        long endTime = transactionDTO.getTrxTime().getTime() + auctionDTO.getDuration() * PER_BLOCK_TIME_MS;
        BlockchainDogOrder blockchainDogOrder = new BlockchainDogOrder();
        blockchainDogOrder.setSeller(dogDTO.getOwner());
        blockchainDogOrder.setDogId(dogDTO.getId());
        blockchainDogOrder.setStatus(OrderStatus.ON.getIntKey());
        blockchainDogOrder.setOrderId(auctionDTO.getTrx_id());
        blockchainDogOrder.setStartingPrice(auctionDTO.getStartingPrice());
        blockchainDogOrder.setEndingPrice(auctionDTO.getEndingPrice());
        blockchainDogOrder.setBeginTime(transactionDTO.getTrxTime());
        blockchainDogOrder.setEndTime(new Date(endTime));
        blockchainDogOrder.setTrxId(transactionDTO.getTrxId());
        blockchainDogOrderService.insert(blockchainDogOrder);
    }

    private void insertNewZeroDog(TransactionDTO transactionDTO, DogDTO dogDTO, String gene, long coolDown) {
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
        blockchainDogInfo.setTrxId(transactionDTO.getTrxId());
        blockchainDogInfoService.insert(blockchainDogInfo);
    }


}
