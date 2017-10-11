package com.lhp.crawler.repository;

import com.lhp.crawler.model.Movies;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MoviesRepository extends JpaRepository<Movies,Long> {
    @Override
    List<Movies> findAll();

    Movies findByCode(String code);

    List<Movies> findAllByStatusIsNull();
}
