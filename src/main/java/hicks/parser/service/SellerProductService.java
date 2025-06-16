package hicks.parser.service;

import hicks.parser.model.MonitoredProduct;
import hicks.parser.model.Subscription;
import hicks.parser.repository.MonitoredProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Обслуживание для загрузки продавцов из Ozon и хранения их как
 * {@link MonitoredProduct} Записи для подписки.
 */
@Service
public class SellerProductService {
    private final MonitoredProductRepository productRepository;

    public SellerProductService(MonitoredProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     *Загрузить продукты для данной подписки и создать записи мониторинга
     * с начальными ценами. Существующие продукты игнорируются.
     */
    @Transactional
    public void loadInitialProducts(Subscription subscription) {
        List<ProductInfo> products = fetchSellerProducts(subscription.getSellerId());
        for (ProductInfo info : products) {
            boolean exists = productRepository
                    .existsBySubscriptionAndProductId(subscription, info.productId());
            if (!exists) {
                MonitoredProduct product = new MonitoredProduct(
                        info.productId(), info.price(), subscription);
                productRepository.save(product);
            }
        }
    }

    /**
     * Заполнитель для реальной интеграции с Ozon.
     */
    private List<ProductInfo> fetchSellerProducts(String sellerId) {
        // TODO implement actual retrieval of products from Ozon
        return new ArrayList<>();
    }

    /** Простое DTO Holding Product и его цена. */
    private record ProductInfo(String productId, BigDecimal price) {}
}
