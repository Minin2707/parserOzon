package hicks.parser.model;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subscriptions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "seller_id"}))
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Telegram-chatId подписчика */
    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    /** Идентификатор продавца на Ozon (например "piquadro-2232276") */
    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    /**
     * Порог падения цены в долях.
     * <p>
     * Уведомление отправляется, когда текущая цена товара
     * становится меньше или равна {@code baselinePrice * threshold}.
     * Например, значение {@code 0.7} означает падение цены на 30&nbsp;%.
     */
    @Column(nullable = false)
    private BigDecimal threshold;

    /** Флаг активности подписки */
    @Column(nullable = false)
    private boolean active = true;

    /** Количество подряд неудачных попыток отправки */
    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    /** Список продуктов, которые мониторятся для этой подписки */
    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonitoredProduct> monitoredProducts = new ArrayList<>();

    // === Constructors ===
    public Subscription() {}

    public Subscription(Long chatId, String sellerId, BigDecimal threshold) {
        this.chatId = chatId;
        this.sellerId = sellerId;
        this.threshold = threshold;
    }

    // === Getters and Setters ===
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public List<MonitoredProduct> getMonitoredProducts() {
        return monitoredProducts;
    }

    public void addMonitoredProduct(MonitoredProduct product) {
        monitoredProducts.add(product);
        product.setSubscription(this);
    }

    public void removeMonitoredProduct(MonitoredProduct product) {
        monitoredProducts.remove(product);
        product.setSubscription(null);
    }

    // === equals and hashCode (by id) ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subscription)) return false;
        return id != null && id.equals(((Subscription) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}


