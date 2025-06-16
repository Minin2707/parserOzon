package hicks.parser.service;

import hicks.parser.model.Subscriber;
import hicks.parser.repository.SubscriberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriberService {
    private final SubscriberRepository subscriberRepository;

    @Autowired
    public SubscriberService(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    @Transactional
    public boolean addSubscriber(String chatId) {
        if (subscriberRepository.existsById(chatId)) {
            return false;
        }

        Subscriber subscriber = new Subscriber();
        subscriber.setChatId(chatId);
        subscriberRepository.save(subscriber);
        return true;
    }

    @Transactional
    public boolean removeSubscriber(String chatId) {
        if (!subscriberRepository.existsById(chatId)) {
            return false;
        }

        subscriberRepository.deleteById(chatId);
        return true;
    }

    public List<Subscriber> getAllSubscribers() {
        return subscriberRepository.findByActiveTrue();
    }
}