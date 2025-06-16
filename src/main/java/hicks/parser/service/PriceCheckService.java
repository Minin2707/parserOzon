package hicks.parser.service;

import hicks.parser.model.MonitoredProduct;
import hicks.parser.repository.MonitoredProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceCheckService {
    private final MonitoredProductRepository productRepo;

    public PriceCheckService(MonitoredProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    /**
     * Parse current prices and update monitoring info.
     */
    @Transactional
    public void checkPrices() {
        List<MonitoredProduct> products = productRepo.findAll();
        for (MonitoredProduct product : products) {
            BigDecimal price = fetchPrice(product.getProductId());
            product.setLastPrice(price);
            product.setLastChecked(LocalDateTime.now());
            productRepo.save(product);
        }
    }

    /** Placeholder for real parsing logic. */
    private BigDecimal fetchPrice(String productId) {
        // TODO: implement actual price retrieval
        return BigDecimal.ZERO;
    }
}
