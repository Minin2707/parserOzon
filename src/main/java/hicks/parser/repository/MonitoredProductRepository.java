package hicks.parser.repository;

import hicks.parser.model.MonitoredProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoredProductRepository extends JpaRepository<MonitoredProduct, Long> {
}