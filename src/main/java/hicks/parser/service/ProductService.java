package hicks.parser.service;

import hicks.parser.model.Product;
import hicks.parser.model.PriceHistory;
import hicks.parser.repository.ProductRepository;
import hicks.parser.repository.PriceHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final OzonProductParser ozonParser;

    @Autowired
    public ProductService(ProductRepository productRepository,
            PriceHistoryRepository priceHistoryRepository,
            OzonProductParser ozonParser) {
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.ozonParser = ozonParser;
    }

    @Transactional
    public Product addProduct(String url) {
        // Проверяем, существует ли уже товар с таким URL
        Optional<Product> existingProduct = productRepository.findByUrl(url);
        if (existingProduct.isPresent()) {
            return existingProduct.get();
        }

        // Парсим новый товар
        Product product = ozonParser.parseProduct(url);
        return productRepository.save(product);
    }

    @Transactional
    public void updateProductPrices() {
        List<Product> activeProducts = productRepository.findByActiveTrue();
        for (Product product : activeProducts) {
            try {
                Optional<BigDecimal> newPrice = ozonParser.parsePrice(product.getUrl());
                if (newPrice.isPresent() && !newPrice.get().equals(product.getCurrentPrice())) {
                    product.setCurrentPrice(newPrice.get());
                    product.setLastChecked(LocalDateTime.now());
                    productRepository.save(product);
                }
            } catch (Exception e) {
                System.err
                        .println("Error updating price for product " + product.getProductId() + ": " + e.getMessage());
            }
        }
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public Optional<Product> getProductByUrl(String url) {
        return productRepository.findByUrl(url);
    }

    @Transactional
    public void deactivateProduct(String productId) {
        productRepository.findById(productId).ifPresent(product -> {
            product.setActive(false);
            productRepository.save(product);
        });
    }

    @Transactional
    public void updatePrices() {
        List<Product> activeProducts = productRepository.findByActiveTrue();
        for (Product product : activeProducts) {
            try {
                Optional<BigDecimal> newPrice = ozonParser.parsePrice(product.getUrl());
                if (newPrice.isPresent()) {
                    BigDecimal oldPrice = product.getCurrentPrice();
                    product.setCurrentPrice(newPrice.get());
                    product.setLastChecked(LocalDateTime.now());
                    productRepository.save(product);

                    // Сохраняем новую цену в историю
                    savePriceHistory(product, newPrice.get(), oldPrice);
                }
            } catch (Exception e) {
                System.err
                        .println("Error updating price for product " + product.getProductId() + ": " + e.getMessage());
            }
        }
    }

    private void savePriceHistory(Product product, BigDecimal newPrice, BigDecimal oldPrice) {
        PriceHistory history = new PriceHistory();
        history.setProduct(product);
        history.setPrice(newPrice);
        history.setCheckedAt(LocalDateTime.now());

        // Если есть старая цена, вычисляем процент изменения
        if (oldPrice != null && oldPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal priceChange = oldPrice.subtract(newPrice);
            BigDecimal priceChangePercent = priceChange
                    .multiply(new BigDecimal("100"))
                    .divide(oldPrice, 2, RoundingMode.HALF_UP);
            history.setPriceChangePercent(priceChangePercent);
        }

        priceHistoryRepository.save(history);
    }
}