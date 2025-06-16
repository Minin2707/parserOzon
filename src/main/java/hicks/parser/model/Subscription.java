package hicks.parser.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

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

    /** Порог падения в долях (0.7 = 70%) */
    @Column(nullable = false)
    private BigDecimal threshold;

    // === getters/setters ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getChatId() { return chatId; }
    public void setChatId(Long chatId) { this.chatId = chatId; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }
}

