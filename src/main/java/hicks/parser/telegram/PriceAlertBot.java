package hicks.parser.telegram;

import hicks.parser.model.Product;
import hicks.parser.model.PriceHistory;
import hicks.parser.repository.PriceHistoryRepository;
import hicks.parser.service.ProductService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PriceAlertBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final ProductService productService;
    private final PriceHistoryRepository priceHistoryRepository;
    // Храним ID чатов подписчиков
    private final Set<Long> subscribers = ConcurrentHashMap.newKeySet();

    public PriceAlertBot(
            @Value("${TELEGRAM_BOT_TOKEN}") String botToken,
            @Value("${TELEGRAM_BOT_USERNAME}") String botUsername,
            ProductService productService,
            PriceHistoryRepository priceHistoryRepository) {
        super(botToken);
        this.botUsername = botUsername;
        this.productService = productService;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if ("/start".equals(messageText)) {
                subscribers.add(chatId);
                sendMessage(chatId, "Вы подписались на уведомления о снижении цен на товары. " +
                        "Вы будете получать уведомления, когда цена на любой отслеживаемый товар снизится на 70% или больше.");
            } else if ("/stop".equals(messageText)) {
                subscribers.remove(chatId);
                sendMessage(chatId, "Вы отписались от уведомлений о снижении цен.");
            }
        }
    }

    @Scheduled(fixedDelayString = "${price.check.interval:300000}") // По умолчанию каждые 5 минут
    public void checkPrices() {
        productService.updatePrices();
        List<Product> products = productService.getAllActiveProducts();

        for (Product product : products) {
            Optional<PriceHistory> lastPrice = priceHistoryRepository.findFirstByProductOrderByCheckedAtDesc(product);
            lastPrice.ifPresent(price -> {
                if (price.getPriceChangePercent() != null
                        && price.getPriceChangePercent().compareTo(new BigDecimal("70")) >= 0) {
                    String message = "🚨 Скидка " + price.getPriceChangePercent() + "%!\n\n" +
                            "Товар: " + product.getName() + "\n" +
                            "Текущая цена: " + product.getCurrentPrice() + " ₽\n" +
                            "Предыдущая цена: " + price.getPrice() + " ₽\n" +
                            "URL: " + product.getUrl();

                    // Отправляем уведомление всем подписчикам
                    for (Long subscriberId : subscribers) {
                        sendMessage(subscriberId, message);
                    }
                }
            });
        }
    }

    private void sendMessage(Long chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message to chat " + chatId + ": " + e.getMessage());
            // Если не удалось отправить сообщение, возможно пользователь заблокировал бота
            subscribers.remove(chatId);
        }
    }
}
