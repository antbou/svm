package sv.menu.svm.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import sv.menu.svm.domain.Menu;

import java.time.LocalDate;
import java.util.List;

public interface MenuRepositoryInterface extends MongoRepository<Menu, String> {
    List<Menu> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
