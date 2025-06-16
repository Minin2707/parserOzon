package hicks.parser.service;


import java.math.BigDecimal;
import java.util.List;

import hicks.parser.model.Subscription;
import hicks.parser.ozon.OzonSellers;
import hicks.parser.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepo;
    private final OzonSellers ozonSellers;
    private final SellerProductService sellerProductService;

    public SubscriptionService(SubscriptionRepository subscriptionRepo, OzonSellers ozonSellers,SellerProductService sellerProductService) {
        this.subscriptionRepo = subscriptionRepo;
        this.ozonSellers = ozonSellers;
        this.sellerProductService = sellerProductService;
    }

    /**
     * Добавить нового подписчика.
     * Если chatId уже есть — не дублируем.
     */
    @Transactional
    public void addSubscriber(Long chatId, BigDecimal threshold) {
        for (String sellerId : ozonSellers.getSellers()) {
            subscriptionRepo
                    .findByChatIdAndSellerId(chatId, sellerId)
                    .ifPresentOrElse(existing -> {
                        if (!existing.isActive()) {
                            existing.setActive(true);
                            existing.setFailureCount(0);
                            subscriptionRepo.save(existing);
                        }
                    }, () -> {
                        Subscription s = new Subscription(chatId, sellerId, threshold);
                        subscriptionRepo.save(s);
                        sellerProductService.loadInitialProducts(s);
                    });
        }
    }

    /**
     * Удалить подписчика (всё подписки этого chatId).
     * Если в будущем будет per-seller, можно указывать sellerId.
     */
    @Transactional
    public void removeSubscriber(Long chatId) {
        subscriptionRepo.deleteByChatId(chatId);
    }

    /**
     * Вернуть список всех chatId для рассылки.
     */
    @Transactional(readOnly = true)
    public List<Long> getAllSubscribers() {
        return subscriptionRepo.findAllChatIds();
    }

    /**
     * Увеличить счётчик сбоев отправки сообщения. Если количество
     * подряд неудачных попыток превысит 3, подписка временно
     * деактивируется.
     */
    @Transactional
    public void recordFailure(Long chatId) {
        List<Subscription> list = subscriptionRepo.findByChatId(chatId);
        for (Subscription s : list) {
            int count = s.getFailureCount() + 1;
            s.setFailureCount(count);
            if (count >= 3) {
                s.setActive(false);
                s.setFailureCount(0);
            }
        }
        subscriptionRepo.saveAll(list);
    }

    /**
     * Для расширенной логики: подписка на конкретного sellerId.
     */

}

