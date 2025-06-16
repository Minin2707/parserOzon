package hicks.parser.repository;

import hicks.parser.model.PriceHistory;
import hicks.parser.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    // Получить последнюю запись цены для товара
    Optional<PriceHistory> findFirstByProductOrderByCheckedAtDesc(Product product);
}