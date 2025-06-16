package hicks.parser.service;

import hicks.parser.model.MonitoredProduct;
import hicks.parser.repository.MonitoredProductRepository;
import hicks.parser.telegram.PriceAlertBot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceCheckService {
    private final MonitoredProductRepository productRepo;
    private final PriceAlertBot alertBot;

    public PriceCheckService(MonitoredProductRepository productRepo, PriceAlertBot alertBot) {
        this.productRepo = productRepo;
        this.alertBot = alertBot;
    }

    /**
     * Parse current prices and update monitoring info.
     */
    @Transactional
    public void checkPrices() {
        // обрабатываем только товары, связанные с активными подписками
        List<MonitoredProduct> products = productRepo.findBySubscription_ActiveTrue();
        for (MonitoredProduct product : products) {
            if (!product.getSubscription().isActive()) {
                // защита от неконсистентных данных
                continue;
            }
            BigDecimal price = fetchPrice(product.getProductId());
            product.setLastPrice(price);
            product.setLastChecked(LocalDateTime.now());

            BigDecimal thresholdPrice = product.getBaselinePrice()
                    .multiply(product.getSubscription().getThreshold());

            if (price.compareTo(thresholdPrice) <= 0 && !product.isNotified()) {
                Long chatId = product.getSubscription().getChatId();
                String text = "Цена на товар " + product.getProductId() +
                        " упала до " + price;
                alertBot.sendAlert(chatId, text);
                product.setNotified(true);
            } else if (price.compareTo(thresholdPrice) > 0 && product.isNotified()) {
                product.setNotified(false);
            }

            productRepo.save(product);
        }
    }

    /** Placeholder for real parsing logic. */
    private BigDecimal fetchPrice(String productId) {
        // TODO: implement actual price retrieval
        return BigDecimal.ZERO;
    }
}
