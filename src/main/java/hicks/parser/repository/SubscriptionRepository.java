package hicks.parser.repository;

import hicks.parser.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;


public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /** Проверить, есть ли уже подписка такого чата на этого продавца */
    boolean existsByChatIdAndSellerId(Long chatId, String sellerId);

    boolean existsByChatId(Long chatId);

    /** Удалить все подписки данного чата (на все магазины) */
    void deleteByChatId(Long chatId);

    /** Удалить подписку чата на конкретного продавца */
    void deleteByChatIdAndSellerId(Long chatId, String sellerId);

    /** Получить список всех chatId для рассылки */
    @Query("SELECT DISTINCT s.chatId FROM Subscription s")
    List<Long> findAllChatIds();

    /** Найти подписку по чату и продавцу */
    Optional<Subscription> findByChatIdAndSellerId(Long chatId, String sellerId);
}

