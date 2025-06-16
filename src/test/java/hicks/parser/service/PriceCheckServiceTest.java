package hicks.parser.service;

import hicks.parser.model.MonitoredProduct;
import hicks.parser.model.Subscription;
import hicks.parser.repository.MonitoredProductRepository;
import hicks.parser.telegram.PriceAlertBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PriceCheckServiceTest {

    @Mock
    private MonitoredProductRepository productRepo;
    @Mock
    private PriceAlertBot alertBot;

    private PriceCheckService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PriceCheckService(productRepo, alertBot);
    }

    @Test
    void skipInactiveSubscriptions() {
        Subscription activeSub = new Subscription();
        activeSub.setActive(true);
        activeSub.setThreshold(BigDecimal.ONE);
        activeSub.setChatId(1L);

        Subscription inactiveSub = new Subscription();
        inactiveSub.setActive(false);
        inactiveSub.setThreshold(BigDecimal.ONE);
        inactiveSub.setChatId(2L);

        MonitoredProduct activeProduct = new MonitoredProduct();
        activeProduct.setProductId("p1");
        activeProduct.setBaselinePrice(BigDecimal.TEN);
        activeProduct.setSubscription(activeSub);

        MonitoredProduct inactiveProduct = new MonitoredProduct();
        inactiveProduct.setProductId("p2");
        inactiveProduct.setBaselinePrice(BigDecimal.TEN);
        inactiveProduct.setSubscription(inactiveSub);

        when(productRepo.findBySubscription_ActiveTrue())
                .thenReturn(List.of(activeProduct, inactiveProduct));

        service.checkPrices();

        verify(productRepo).findBySubscription_ActiveTrue();
        verify(productRepo).save(activeProduct);
        verify(productRepo, never()).save(inactiveProduct);
        verify(alertBot).sendAlert(eq(activeSub.getChatId()), anyString());
        verify(alertBot, never()).sendAlert(eq(inactiveSub.getChatId()), anyString());
    }
}
