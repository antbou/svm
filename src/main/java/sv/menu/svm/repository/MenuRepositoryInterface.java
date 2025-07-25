package sv.menu.svm.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import sv.menu.svm.domain.Menu;

import java.time.LocalDate;
import java.util.List;

public interface MenuRepositoryInterface extends MongoRepository<Menu, String> {
    @Query("{ 'date' : { $gte: ?0, $lte: ?1 } }")
    List<Menu> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
