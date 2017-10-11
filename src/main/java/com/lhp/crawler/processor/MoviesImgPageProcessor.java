package com.lhp.crawler.processor;

import com.lhp.crawler.enumns.MoviesStatus;
import com.lhp.crawler.model.Movies;
import com.lhp.crawler.repository.MoviesRepository;
import com.lhp.crawler.utils.MoviesUtils;
import com.lhp.crawler.utils.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Date;

@Slf4j
@Component
public class MoviesImgPageProcessor implements PageProcessor {
    @Autowired
    MoviesRepository moviesRepository;

    private int timestamp = (int) (new Date().getTime() / 1000);
    //设置代理参数
    private final String authHeader = authHeader(SystemConfig.getProperty("proxy.orderno"), SystemConfig.getProperty("proxy.secret"), timestamp);
    private Site site = Site.me()
            .setDomain("http://piaofang.maoyan.com")
            .setSleepTime(5000)
            .setCharset("utf-8")
            .setTimeOut(5000)
            .addHeader("Proxy-Authorization", authHeader)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
    @Override
    public void process(Page page) {

        String code = page.getRequest().getHeaders().get("code");
        Elements movieImg = page.getHtml().getDocument().select("img.need-handle-pic");
        if (movieImg == null || movieImg.size() <= 0) {
            this.log.info("没有获取到指定的节点[img]，请重新分析dom结构。"+code);
            return;
        }
        final String src = movieImg.attr("src");
        final String title = movieImg.attr("alt");
        String res = MoviesUtils.downloadImg(src, code, title);
        if (res.equals("0")) {
            Movies movie = moviesRepository.findByCode(code);
            movie.setIsDownload(true);
            moviesRepository.save(movie);
        } else {
            Movies movie = moviesRepository.findByCode(code);
            movie.setIsDownload(false);
            moviesRepository.save(movie);
        }

    }

    public void start(MoviesImgPageProcessor processor, Request request, Downloader httpClientDownloader) {
        Spider.create(processor).setDownloader(httpClientDownloader)
                .addRequest(request).run();
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static String authHeader(String orderno, String secret, int timestamp) {
        //拼装签名字符串
        String planText = String.format("orderno=%s,secret=%s,timestamp=%d", orderno, secret, timestamp);

        //计算签名
        String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(planText).toUpperCase();

        //拼装请求头Proxy-Authorization的值
        String authHeader = String.format("sign=%s&orderno=%s&timestamp=%d", sign, orderno, timestamp);
        return authHeader;
    }
}
