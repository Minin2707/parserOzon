package hicks.parser.service;


import java.util.List;

import hicks.parser.model.Subscription;
import hicks.parser.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepo;

    public SubscriptionService(SubscriptionRepository subscriptionRepo) {
        this.subscriptionRepo = subscriptionRepo;
    }

    /**
     * Добавить нового подписчика.
     * Если chatId уже есть — не дублируем.
     */
    @Transactional
    public void addSubscriber(Long chatId) {
        boolean exists = subscriptionRepo.existsByChatId(chatId);
        if (!exists) {
            Subscription sub = new Subscription();
            sub.setChatId(chatId);
            // Заодно можно сразу выставить sellerId и threshold, если их заранее знаем:
            // sub.setSellerId("piquadro-2232276");
            // sub.setThreshold(new BigDecimal("0.7"));
            subscriptionRepo.save(sub);
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

