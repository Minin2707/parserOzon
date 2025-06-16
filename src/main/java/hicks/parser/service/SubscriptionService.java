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

    public SubscriptionService(SubscriptionRepository subscriptionRepo, OzonSellers ozonSellers) {
        this.subscriptionRepo = subscriptionRepo;
        this.ozonSellers = ozonSellers;
    }

    /**
     * Добавить нового подписчика.
     * Если chatId уже есть — не дублируем.
     */
    @Transactional
    public void addSubscriber(Long chatId, BigDecimal threshold) {
        for (String sellerId : ozonSellers.getSellers()) {
            // проверяем, не подписан ли уже
            boolean exists = subscriptionRepo
                    .existsByChatIdAndSellerId(chatId, sellerId);
            if (!exists) {
                // создаём и сохраняем новую подписку
                Subscription s = new Subscription(chatId, sellerId, threshold);
                subscriptionRepo.save(s);
            }
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
     * Для расширенной логики: подписка на конкретного sellerId.
     */

}

