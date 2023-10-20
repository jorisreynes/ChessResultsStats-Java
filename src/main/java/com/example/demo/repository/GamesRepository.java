
package com.example.demo.repository;

import com.example.demo.model.Game;
import org.springframework.data.repository.CrudRepository;

public interface GamesRepository extends CrudRepository<Game, Long> {
}
