package hicks.parser;

import hicks.parser.service.ProductService;
import hicks.parser.service.OzonProductParser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class ParserApplication {
	private final ProductService productService;
	private final OzonProductParser ozonParser;
	private static final String SELLER_URL = "https://www.ozon.ru/seller/piquadro-official-store-651979/?miniapp=seller_651979";

	public ParserApplication(ProductService productService, OzonProductParser ozonParser) {
		this.productService = productService;
		this.ozonParser = ozonParser;
	}

	public static void main(String[] args) {
		SpringApplication.run(ParserApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		System.out.println("Выполняем первичный парсинг товаров магазина...");
		try {
			// Парсим все товары с страницы продавца
			List<String> productUrls = ozonParser.parseSellerProducts(SELLER_URL);
			System.out.println("Найдено товаров: " + productUrls.size());

			// Для каждого товара получаем информацию и сохраняем
			for (String url : productUrls) {
				try {
					productService.addProduct(url);
				} catch (Exception e) {
					System.err.println("Ошибка при добавлении товара " + url + ": " + e.getMessage());
				}
			}
			System.out.println("Первичный парсинг завершен.");
		} catch (Exception e) {
			System.err.println("Ошибка при первичном парсинге: " + e.getMessage());
		}
	}
}
