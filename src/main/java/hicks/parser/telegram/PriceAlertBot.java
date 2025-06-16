package hicks.parser.telegram;


import hicks.parser.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Component
public class PriceAlertBot extends TelegramLongPollingBot {

    // username хранится здесь и отдается в getBotUsername()
    private final String botUsername;
    // сервис для регистрации chatId и дальнейшей рассылки
    private final SubscriptionService subscriptionService;

    /**
     * Конструктор: токен передаём в суперкласс,
     * username и subscriptionService — сохраняем локально.
     */
    public PriceAlertBot(
            @Value("${TELEGRAM_BOT_TOKEN}") String botToken,
            @Value("${TELEGRAM_BOT_USERNAME}") String botUsername,
            SubscriptionService subscriptionService) {
        super(botToken);               // вместо deprecated getBotToken()
        this.botUsername = botUsername;
        this.subscriptionService = subscriptionService;
    }

    /**
     * Единственный метод для username — токен теперь в AbsSender хранится
     * родительским классом, поэтому getBotToken() не переопределяем.
     */
    @Override
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Ловим только /start и сохраняем подписчика.
     * Больше действий от пользователя не требуется.
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String txt = update.getMessage().getText().trim();
            Long chatId = update.getMessage().getChatId();
            if ("/start".equals(txt)) {
                subscriptionService.addSubscriber(chatId);
                sendText(chatId, "✅ Вы подписаны! Буду присылать уведомления о падении цены.");
            }
        }
    }

    /**
     * Метод для внешнего вызова из шедулера
     * — рассылает alert о падении цены.
     */
    public void sendAlert(Long chatId, String text) {
        sendText(chatId, text);
    }

    /**
     * Вспомогательный метод чтобы не дублировать код.
     */
    private void sendText(Long chatId, String text) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .build();
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
            // логируем неудачную отправку,
            // можно пометить подписчика как недоступного
        }
    }
}

