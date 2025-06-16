package hicks.parser.service;

import hicks.parser.model.Subscription;
import hicks.parser.ozon.OzonSellers;

import hicks.parser.repository.SubscriptionRepository;
import hicks.parser.service.SubscriptionService;
import hicks.parser.service.SellerProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {

    private SubscriptionRepository repository;
    private OzonSellers sellers;
    private SubscriptionService service;
    private SellerProductService productService;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(SubscriptionRepository.class);
        sellers = new OzonSellers();
        sellers.setSellers(List.of("seller1"));
        productService = Mockito.mock(SellerProductService.class);
        productService = Mockito.mock(SellerProductService.class);
        service = new SubscriptionService(repository, sellers, productService);
    }

    @Test
    void startCommandActivatesExistingSubscription() {
        Long chatId = 1L;
        BigDecimal threshold = BigDecimal.valueOf(0.7);
        Subscription existing = new Subscription(chatId, "seller1", threshold);
        existing.setActive(false);
        existing.setFailureCount(2);

        when(repository.findByChatIdAndSellerId(chatId, "seller1"))
                .thenReturn(Optional.of(existing));

        service.addSubscriber(chatId, threshold);

        assertTrue(existing.isActive());
        assertEquals(0, existing.getFailureCount());

        verify(repository).save(existing);
        verifyNoInteractions(productService);
    }

    @Test
    void newSubscriptionCreatesProducts() {
        Long chatId = 2L;
        BigDecimal threshold = BigDecimal.valueOf(0.7);

        when(repository.findByChatIdAndSellerId(chatId, "seller1"))
                .thenReturn(Optional.empty());

        service.addSubscriber(chatId, threshold);

        verify(repository).save(any(Subscription.class));
        verify(productService).loadInitialProducts(any(Subscription.class));
    }
}