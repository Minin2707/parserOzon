package hicks.parser.repository;

import hicks.parser.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, String> {
    List<Subscriber> findByActiveTrue();
}