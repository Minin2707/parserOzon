package hicks.parser.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "monitored_products")
public class MonitoredProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Уникальный идентификатор товара на Ozon (itemId/productId) */
    @Column(name = "product_id", nullable = false)
    private String productId;

    /** Базовая (изначальная) цена, от которой считаем изменение */
    @Column(name = "baseline_price", nullable = false)
    private BigDecimal baselinePrice;

    /** Флаг, был ли уже отправлен alert о падении цены */
    @Column(nullable = false)
    private boolean notified;

    /** Связь с подпиской, для которой мониторим этот товар */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    // === Конструкторы ===
    public MonitoredProduct() {}

    public MonitoredProduct(String productId, BigDecimal baselinePrice, Subscription subscription) {
        this.productId = productId;
        this.baselinePrice = baselinePrice;
        this.subscription = subscription;
        this.notified = false;
    }

    // === Getters и Setters ===
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public BigDecimal getBaselinePrice() {
        return baselinePrice;
    }

    public void setBaselinePrice(BigDecimal baselinePrice) {
        this.baselinePrice = baselinePrice;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    // === equals и hashCode (по id) ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MonitoredProduct)) return false;
        return id != null && id.equals(((MonitoredProduct) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "MonitoredProduct{" +
                "id=" + id +
                ", productId='" + productId + '\'' +
                ", baselinePrice=" + baselinePrice +
                ", notified=" + notified +
                '}';
    }
}
