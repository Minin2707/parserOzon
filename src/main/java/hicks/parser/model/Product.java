package hicks.parser.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @Column(name = "product_id")
    private String productId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(name = "current_price", nullable = false)
    private BigDecimal currentPrice;

    @Column(name = "initial_price", nullable = false)
    private BigDecimal initialPrice;

    @Column(name = "last_checked")
    private LocalDateTime lastChecked;

    @Column(nullable = false)
    private boolean active = true;

    // Геттеры и сеттеры
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(BigDecimal initialPrice) {
        this.initialPrice = initialPrice;
    }

    public LocalDateTime getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(LocalDateTime lastChecked) {
        this.lastChecked = lastChecked;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // Метод для проверки, упала ли цена на 70% или больше
    public boolean hasPriceDropped70Percent() {
        if (initialPrice == null || currentPrice == null) {
            return false;
        }
        BigDecimal priceDrop = initialPrice.subtract(currentPrice);
        BigDecimal dropPercentage = priceDrop.multiply(new BigDecimal("100")).divide(initialPrice, 2,
                BigDecimal.ROUND_HALF_UP);
        return dropPercentage.compareTo(new BigDecimal("70")) >= 0;
    }
}