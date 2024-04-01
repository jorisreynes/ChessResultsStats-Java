
package com.example.demo.repository;

import com.example.demo.model.Game;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface GamesRepository extends ElasticsearchRepository<Game, Long> {
	List<Game> findByPlayerusername(String playerusername);
}
