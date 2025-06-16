package hicks.parser.repository;

import hicks.parser.model.MonitoredProduct;
import hicks.parser.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitoredProductRepository extends JpaRepository<MonitoredProduct, Long> {
    /**
     * Получить все товары с активными подписками.
     */
    List<MonitoredProduct> findBySubscription_ActiveTrue();

    /**
     * Проверьте, существует ли продукт для подписки.
     */
    boolean existsBySubscriptionAndProductId(Subscription subscription, String productId);
}