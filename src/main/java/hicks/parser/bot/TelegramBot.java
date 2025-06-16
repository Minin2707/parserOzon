package hicks.parser.bot;

import hicks.parser.model.Product;
import hicks.parser.model.Subscriber;
import hicks.parser.service.ProductService;
import hicks.parser.service.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final ProductService productService;
    private final SubscriberService subscriberService;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    public TelegramBot(ProductService productService, SubscriberService subscriberService) {
        this.productService = productService;
        this.subscriberService = subscriberService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        switch (messageText) {
            case "/start" -> handleStart(chatId);
            case "/stop" -> handleStop(chatId);
            default ->
                sendMessage(chatId, "Используйте /start для подписки на уведомления или /stop для отмены подписки.");
        }
    }

    private void handleStart(String chatId) {
        if (subscriberService.addSubscriber(chatId)) {
            sendMessage(chatId, "Вы успешно подписались на уведомления о снижении цен!");
        } else {
            sendMessage(chatId, "Вы уже подписаны на уведомления.");
        }
    }

    private void handleStop(String chatId) {
        if (subscriberService.removeSubscriber(chatId)) {
            sendMessage(chatId, "Вы отписались от уведомлений о снижении цен.");
        } else {
            sendMessage(chatId, "Вы не были подписаны на уведомления.");
        }
    }

    @Scheduled(fixedDelayString = "${parser.check.interval}")
    public void checkPrices() {
        productService.updateProductPrices();
        List<Product> products = productService.getAllActiveProducts();
        List<Subscriber> subscribers = subscriberService.getAllSubscribers();

        for (Product product : products) {
            if (product.getCurrentPrice().compareTo(product.getInitialPrice()) < 0) {
                String message = String.format(
                        "💰 Снижение цены!\n\n" +
                                "Товар: %s\n" +
                                "Старая цена: %.2f ₽\n" +
                                "Новая цена: %.2f ₽\n" +
                                "Ссылка: %s",
                        product.getName(),
                        product.getInitialPrice(),
                        product.getCurrentPrice(),
                        product.getUrl());

                for (Subscriber subscriber : subscribers) {
                    sendMessage(subscriber.getChatId(), message);
                }
            }
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.enableMarkdown(true);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message to " + chatId + ": " + e.getMessage());
        }
    }
}