
package com.example.demo.repository;

import com.example.demo.model.Game;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface GamesRepository extends CrudRepository<Game, Long> {
	List<Game> findByPlayerusername(String playerusername);
}
