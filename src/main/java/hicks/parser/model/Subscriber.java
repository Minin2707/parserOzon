package hicks.parser.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "subscribers")
public class Subscriber {
    @Id
    @Column(name = "chat_id")
    private String chatId;

    @Column(nullable = false)
    private boolean active = true;

    // Геттеры и сеттеры
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}