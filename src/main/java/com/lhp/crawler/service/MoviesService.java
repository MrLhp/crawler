package com.lhp.crawler.service;

import com.lhp.crawler.repository.MoviesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MoviesService {
    @Autowired
    MoviesRepository moviesRepository;
}
