package com.lhp.crawler.processor;

import com.lhp.crawler.model.Movies;
import com.lhp.crawler.repository.MoviesRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;

@Slf4j
@Component
public class MoviesPageProcessor implements PageProcessor {
    @Autowired
    MoviesRepository moviesRepository;
    private Site site = Site.me()
            .setDomain("http://piaofang.maoyan.com")
            .setSleepTime(1000)
            .setCharset("utf-8")
            .setTimeOut(5000)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
    private final String searchUrl = "http://piaofang.maoyan.com/search?key=REPLACE_KEY&page=0&size=300";
    @Override
    public void process(Page page) {

        if (page.getUrl().toString().contains("&page=0&size=300")) {//搜索结果页面处理
            searchResultPorcess(page);
        }
    }

    private void searchResultPorcess(Page page) {
        List<String> movieInfoUrl = page.getHtml().xpath("//*[@id=\"search-list\"]/article").all();
        String regex = page.getUrl().regex("key=([^&]*)").toString();
        if (movieInfoUrl == null || movieInfoUrl.isEmpty() || movieInfoUrl.size()==0) {
            this.log.warn("该影片没有匹配成功："+regex);
            return;
        }
        for (String s : movieInfoUrl) {
            Document doc = Jsoup.parse(s);
            this.log.info("《"+regex+"》-匹配url为:"+doc.select("article").attr("data-url"));
        }
    }

    public void start(MoviesPageProcessor processor) {
        List<Movies> moviesList = moviesRepository.findAll();
        for (int i = 0; i < 5; i++) {
            Spider.create(processor).addUrl(searchUrl.replace("REPLACE_KEY",moviesList.get(i).getName())).start();
        }
    }

    @Override
    public Site getSite() {
        return site;
    }
}
