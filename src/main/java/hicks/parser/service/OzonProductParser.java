package hicks.parser.service;

import hicks.parser.model.Product;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.nio.file.Paths;

@Service
public class OzonProductParser {
    private WebDriver webDriver;
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d+[.,]\\d+)");
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("/product/.*?-(\\d+)/");
    private static final Random RANDOM = new Random();
    private static final String DEBUG_DIR = "debug_logs";

    // Константы для задержек
    private static final int MIN_PAGE_LOAD_DELAY = 3000; // 3 секунды
    private static final int MAX_PAGE_LOAD_DELAY = 7000; // 7 секунд
    private static final int MIN_SCROLL_DELAY = 500; // 0.5 секунды
    private static final int MAX_SCROLL_DELAY = 2000; // 2 секунды
    private static final int MIN_ACTION_DELAY = 800; // 0.8 секунды
    private static final int MAX_ACTION_DELAY = 2500; // 2.5 секунды

    private static final int MAX_RETRY_ATTEMPTS = 3; // Максимальное количество попыток перезагрузки страницы

    @Autowired
    public OzonProductParser() {
        // Пустой конструктор для Spring
    }

    @PostConstruct
    public void init() {
        // Создаем директорию для отладочных файлов
        try {
            Files.createDirectories(Paths.get(DEBUG_DIR));
            System.out.println("Создана директория для отладочных файлов: " +
                    new File(DEBUG_DIR).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Ошибка при создании директории для отладочных файлов: " + e.getMessage());
        }

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.managed_default_content_settings.images", 2);
        options.setExperimentalOption("prefs", prefs);
        this.webDriver = new ChromeDriver(options);
        // Скрываем navigator.webdriver
        ((JavascriptExecutor) webDriver).executeScript(
                "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
    }

    /**
     * Имитация задержки при загрузке страницы
     */
    private void pageLoadDelay() {
        try {
            int delay = MIN_PAGE_LOAD_DELAY + RANDOM.nextInt(MAX_PAGE_LOAD_DELAY - MIN_PAGE_LOAD_DELAY);
            System.out.println("Ожидание загрузки страницы: " + delay + "мс");
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Имитация задержки при прокрутке страницы
     */
    private void scrollDelay() {
        try {
            int delay = MIN_SCROLL_DELAY + RANDOM.nextInt(MAX_SCROLL_DELAY - MIN_SCROLL_DELAY);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Имитация задержки между действиями пользователя
     */
    private void actionDelay() {
        try {
            int delay = MIN_ACTION_DELAY + RANDOM.nextInt(MAX_ACTION_DELAY - MIN_ACTION_DELAY);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Имитация прокрутки страницы как человек
     */
    private void humanScroll() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) webDriver;
            long pageHeight = (long) js.executeScript("return document.body.scrollHeight");
            long viewportHeight = (long) js.executeScript("return window.innerHeight");
            long currentPosition = 0;

            while (currentPosition < pageHeight) {
                // Случайная скорость прокрутки
                int scrollStep = 100 + RANDOM.nextInt(200);
                currentPosition = Math.min(currentPosition + scrollStep, pageHeight);

                // Плавная прокрутка
                js.executeScript("window.scrollTo({top: " + currentPosition + ", behavior: 'smooth'})");
                scrollDelay();

                // Иногда делаем паузу подольше
                if (RANDOM.nextDouble() < 0.2) { // 20% шанс
                    actionDelay();
                }
            }

            // Возвращаемся в начало страницы
            js.executeScript("window.scrollTo({top: 0, behavior: 'smooth'})");
            actionDelay();
        } catch (Exception e) {
            System.err.println("Ошибка при прокрутке страницы: " + e.getMessage());
        }
    }

    private void saveDebugInfo(String prefix) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String screenshotPath = DEBUG_DIR + File.separator + prefix + "_" + timestamp + ".png";
            String htmlPath = DEBUG_DIR + File.separator + prefix + "_" + timestamp + ".html";

            // Сохраняем скриншот
            File screenshot = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.FILE);
            Files.copy(screenshot.toPath(), new File(screenshotPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Сохранен скриншот: " + new File(screenshotPath).getAbsolutePath());

            // Сохраняем HTML
            String pageSource = webDriver.getPageSource();
            Files.writeString(new File(htmlPath).toPath(), pageSource);
            System.out.println("Сохранен HTML: " + new File(htmlPath).getAbsolutePath());

            // Анализируем состояние страницы
            System.out.println("\nИнформация о странице:");
            System.out.println("URL: " + webDriver.getCurrentUrl());
            System.out.println("Заголовок: " + webDriver.getTitle());
            System.out.println("Размер страницы: " + pageSource.length() + " байт");

            // Проверяем наличие различных элементов
            checkPageElements();

        } catch (Exception e) {
            System.err.println("Ошибка при сохранении отладочной информации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkPageElements() {
        try {
            // Проверяем наличие различных элементов на странице
            List<String> selectorsToCheck = Arrays.asList(
                    "div[data-widget='searchResultsV2']",
                    "div[data-widget='card']",
                    "a[href*='/product/']",
                    "div[class*='captcha']",
                    "div[class*='error']",
                    "div[class*='blocked']",
                    "div[class*='maintenance']",
                    "button#reload-button", // Кнопка Обновить
                    "h1" // Заголовки, в том числе "Доступ ограничен"
            );

            System.out.println("\nПроверка элементов на странице:");
            for (String selector : selectorsToCheck) {
                List<WebElement> elements = webDriver.findElements(By.cssSelector(selector));
                System.out.println("Селектор '" + selector + "': найдено элементов - " + elements.size());
                if (!elements.isEmpty()) {
                    System.out.println("Первый элемент: '"
                            + elements.get(0).getText().substring(0, Math.min(100, elements.get(0).getText().length()))
                            + "'");
                }
            }

            // Проверяем наличие JavaScript ошибок
            List<Object> jsErrors = (List<Object>) ((JavascriptExecutor) webDriver)
                    .executeScript(
                            "return window.performance.getEntries().filter(e => e.initiatorType === 'script' && e.duration > 1000);");
            if (!jsErrors.isEmpty()) {
                System.out.println("\nОбнаружены медленные JavaScript запросы:");
                jsErrors.forEach(System.out::println);
            }

        } catch (Exception e) {
            System.err.println("Ошибка при проверке элементов страницы: " + e.getMessage());
        }
    }

    public List<String> parseSellerProducts(String sellerUrl) {
        List<String> productUrls = new ArrayList<>();
        int retryCount = 0;
        while (retryCount < MAX_RETRY_ATTEMPTS) {
            try {
                System.out.println(
                        "\nНачинаем парсинг страницы продавца: " + sellerUrl + " (Попытка " + (retryCount + 1) + ")");
                webDriver.get(sellerUrl);
                pageLoadDelay();

                // Имитация поведения человека
                humanScroll();
                actionDelay();

                // Добавляем дополнительные заголовки для имитации реального браузера
                ((JavascriptExecutor) webDriver).executeScript(
                        "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});" +
                                "Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3, 4, 5]});" +
                                "Object.defineProperty(navigator, 'languages', {get: () => ['ru-RU', 'ru', 'en-US', 'en']});");
                actionDelay();

                // Проверяем, не заблокирован ли доступ
                if (webDriver.getTitle().contains("Доступ ограничен") ||
                        webDriver.findElements(By.cssSelector("h1:contains('Доступ ограничен')")).size() > 0) {

                    System.err.println("Доступ ограничен. Пытаемся нажать кнопку \"Обновить\".");
                    saveDebugInfo("access_denied");

                    try {
                        WebElement reloadButton = webDriver.findElement(By.id("reload-button"));
                        reloadButton.click();
                        System.out.println("Кнопка \"Обновить\" нажата. Ожидаем...");
                        pageLoadDelay(); // Ожидаем после нажатия
                        retryCount++;
                        continue; // Повторяем попытку парсинга
                    } catch (Exception e) {
                        System.err.println("Не удалось нажать кнопку \"Обновить\": " + e.getMessage());
                        saveDebugInfo("reload_button_error");
                        throw new RuntimeException("Доступ ограничен и не удалось нажать кнопку Обновить.", e);
                    }
                }

                // Увеличиваем таймаут и добавляем проверку на наличие капчи
                WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));

                try {
                    // Проверяем наличие капчи
                    if (webDriver.findElements(By.cssSelector("div[class*='captcha']")).size() > 0) {
                        System.err.println("Обнаружена капча на странице!");
                        saveDebugInfo("captcha_detected");
                        throw new RuntimeException("Обнаружена капча на странице");
                    }

                    // Пробуем разные селекторы для поиска товаров
                    List<WebElement> productElements = new ArrayList<>();

                    // Вариант 1: поиск по текущему селектору
                    try {
                        System.out.println("Пробуем найти товары по основному селектору...");
                        wait.until(ExpectedConditions.presenceOfElementLocated(
                                By.cssSelector("div[data-widget='searchResultsV2']")));
                        productElements = webDriver.findElements(
                                By.cssSelector("a[href*='/product/']"));
                        System.out.println("Найдено товаров по основному селектору: " + productElements.size());
                    } catch (Exception e) {
                        System.err.println("Не удалось найти товары по основному селектору, пробуем альтернативные...");

                        // Вариант 2: поиск по карточкам товаров
                        productElements = webDriver.findElements(
                                By.cssSelector("div[data-widget='card'] a[href*='/product/']"));
                        System.out.println("Найдено товаров по карточкам: " + productElements.size());

                        // Вариант 3: поиск по всем ссылкам на товары
                        if (productElements.isEmpty()) {
                            productElements = webDriver.findElements(
                                    By.cssSelector("a[href*='/product/']"));
                            System.out.println("Найдено товаров по всем ссылкам: " + productElements.size());
                        }
                    }

                    // Проверяем, что нашли товары
                    if (productElements.isEmpty()) {
                        System.err.println("Не удалось найти товары на странице. Сохраняем отладочную информацию...");
                        saveDebugInfo("no_products_found");
                        throw new RuntimeException("Не удалось найти товары на странице");
                    }

                    // Собираем URL товаров
                    for (WebElement element : productElements) {
                        String url = element.getAttribute("href");
                        if (url != null && url.contains("/product/") && !productUrls.contains(url)) {
                            productUrls.add(url);
                            System.out.println("Добавлен товар: " + url);
                        }
                    }

                    System.out.println("Найдено " + productUrls.size() + " уникальных товаров");
                    return productUrls;

                } catch (Exception e) {
                    System.err.println("Ошибка при поиске товаров: " + e.getMessage());
                    saveDebugInfo("search_error");
                    throw e;
                }
            } catch (Exception e) {
                System.err.println("Ошибка при парсинге товаров продавца: " + e.getMessage());
                saveDebugInfo("parser_error");
                if (e.getMessage().contains("Доступ ограничен") && retryCount < MAX_RETRY_ATTEMPTS) {
                    System.out.println("Повторная попытка парсинга...");
                    retryCount++;
                    actionDelay(); // Дополнительная задержка перед повторной попыткой
                } else {
                    throw new RuntimeException("Failed to parse seller products: " + e.getMessage(), e);
                }
            }
        }
        throw new RuntimeException(
                "Не удалось получить доступ к странице продавца после " + MAX_RETRY_ATTEMPTS + " попыток.");
    }

    public Product parseProduct(String url) {
        try {
            webDriver.get(url);
            pageLoadDelay();

            // Имитация поведения человека
            humanScroll();
            actionDelay();

            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));
            WebElement productNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("h1[data-widget='webProductHeading']")));
            String productName = productNameElement.getText();
            String productId = extractProductId(url);
            if (productId == null) {
                throw new IllegalArgumentException("Could not extract product ID from URL: " + url);
            }
            Optional<BigDecimal> price = parsePrice(url);
            if (price.isEmpty()) {
                throw new IllegalArgumentException("Could not parse price from URL: " + url);
            }
            Product product = new Product();
            product.setProductId(productId);
            product.setName(productName);
            product.setUrl(url);
            product.setCurrentPrice(price.get());
            product.setInitialPrice(price.get());
            product.setLastChecked(LocalDateTime.now());
            product.setActive(true);
            return product;
        } catch (Exception e) {
            System.err.println("Error parsing product from URL: " + url + ", error: " + e.getMessage());
            saveDebugInfo("product_parser_error");
            throw new RuntimeException("Failed to parse product: " + e.getMessage(), e);
        }
    }

    public Optional<BigDecimal> parsePrice(String url) {
        try {
            webDriver.get(url);
            pageLoadDelay();

            // Имитация поведения человека
            humanScroll();
            actionDelay();

            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(20));
            WebElement priceElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div[data-widget='webPrice'] span[data-widget='price']")));
            String priceText = priceElement.getText();
            Matcher matcher = PRICE_PATTERN.matcher(priceText);
            if (matcher.find()) {
                String priceStr = matcher.group(1).replace(',', '.');
                return Optional.of(new BigDecimal(priceStr));
            }
            System.err.println("Could not extract price from text: " + priceText);
            saveDebugInfo("price_parse_error");
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error parsing price from URL: " + url + ", error: " + e.getMessage());
            saveDebugInfo("price_parser_error");
            return Optional.empty();
        }
    }

    public String extractProductId(String url) {
        Matcher matcher = PRODUCT_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public void close() {
        if (webDriver != null) {
            webDriver.quit();
        }
    }
}