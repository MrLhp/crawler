package com.lhp.crawler.controller;

import com.lhp.crawler.processor.MoviesPageProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/movies")
public class MoviesController {
    @Autowired
    MoviesPageProcessor moviesPageProcessor;

    @GetMapping("/start")
    public @ResponseBody String startCrawler() {
        new Thread(()->moviesPageProcessor.start(moviesPageProcessor)).start();
        return "爬虫开启!";
    }
}
