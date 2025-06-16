package hicks.parser;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class ChromeDriverTest {

    @Test
    public void testChromeDriver() {
        try {
            // Автоматическая установка и настройка ChromeDriver
            WebDriverManager.chromedriver().setup();

            // Настройка ChromeDriver
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); // Запуск в фоновом режиме
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            // Создание экземпляра драйвера
            WebDriver driver = new ChromeDriver(options);

            // Тестовый переход на сайт
            driver.get("https://www.ozon.ru");
            System.out.println("Title: " + driver.getTitle());

            // Закрытие браузера
            driver.quit();
            System.out.println("ChromeDriver test completed successfully!");

        } catch (Exception e) {
            System.err.println("ChromeDriver test failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}