package hicks.parser;

import hicks.parser.service.OzonProductParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class OzonProductParserTest {
    @Autowired
    private OzonProductParser ozonProductParser;

    @Test
    public void testParseSellerProducts() {
        String sellerUrl = "https://www.ozon.ru/seller/piquadro-official-store-651979/?miniapp=seller_651979";
        List<String> products = ozonProductParser.parseSellerProducts(sellerUrl);
        System.out.println("Найдено товаров: " + products.size());
        for (String url : products) {
            System.out.println(url);
        }
    }
}