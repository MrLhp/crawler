package com.lhp.crawler.processor;

import com.lhp.crawler.enumns.MoviesStatus;
import com.lhp.crawler.model.Movies;
import com.lhp.crawler.repository.MoviesRepository;
import com.lhp.crawler.utils.MoviesUtils;
import com.lhp.crawler.utils.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.scheduler.PriorityScheduler;
import us.codecraft.webmagic.scheduler.Scheduler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Component
public class MoviesPageProcessor implements PageProcessor {

    Scheduler scheduler = new PriorityScheduler();
    Spider spider;
    SystemConfig systemConfig = new SystemConfig();


    @Autowired
    MoviesRepository moviesRepository;
    @Autowired
    MoviesImgPageProcessor imgPageProcessor;


    private int timestamp = (int) (new Date().getTime() / 1000);
    final String ip = "forward.xdaili.cn";//这里以正式服务器ip地址为准
    final int port = 80;//这里以正式服务器端口地址为准


    //设置代理参数
    private final String authHeader = authHeader(SystemConfig.getProperty("proxy.orderno"), SystemConfig.getProperty("proxy.secret"), timestamp);
    private Site site = Site.me()
            .setDomain("http://piaofang.maoyan.com")
            .setSleepTime(10000)
            .setCharset("utf-8")
            .setTimeOut(5000)
            .addHeader("Proxy-Authorization", authHeader)
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
    private final String searchUrl = "http://piaofang.maoyan.com/search?key=REPLACE_KEY&page=0&size=300";

    @Override
    public void process(Page page) {

        if (page.getUrl().toString().contains("&page=0&size=300")) {//搜索结果列表处理
            searchResultProcess(page);
        } else if (page.getUrl().toString().contains("movie")) {//影片详情页处理

        }
    }

    private void searchResultProcess(Page page) {
        List<String> movieInfoUrl = page.getHtml().xpath("//*[@id=\"search-list\"]/article").all();
        String searchKeyName = page.getUrl().regex("key=([^&]*)").toString();
        String code = page.getRequest().getHeaders().get("code");

        if (movieInfoUrl == null || movieInfoUrl.isEmpty() || movieInfoUrl.size() == 0) {
            this.log.warn(String.format("该影片匹配失败：《%s》", searchKeyName));
            Movies movie = moviesRepository.findByCode(code);
            movie.setStatus(MoviesStatus.匹配失败);
            moviesRepository.save(movie);
            return;
        }

        Map<String, String> resMap = new HashMap<>();
        for (String info : movieInfoUrl) {//解析结果
            Document doc = Jsoup.parse(info);
            String mName = doc.select("div.title").text();

            if (mName.equals(searchKeyName)) {
                String year = code.substring(code.length() - 4);
                String onDays = doc.select("p").get(0).text();

                Pattern p = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2}) ([\\u4e00-\\u9fa5][\\u4e00-\\u9fa5])");
                Matcher m = p.matcher(onDays);

                String detailUrl = doc.select("article").attr("data-url");

                if (StringUtils.isNotBlank(onDays) && m.find() && m.group(1).equals(year)) {
                    this.log.info(String.format("《%s》-匹配成功-《%s》", searchKeyName, onDays));
                    //结果添加到抓取队列
//                    page.addTargetRequest(new Request(site.getDomain() + detailUrl).addHeader("code", code).setPriority(0));
                    imgPageProcessor.start(imgPageProcessor,new Request(site.getDomain() + detailUrl).addHeader("code", code),getDownloader());
                    Movies movie = moviesRepository.findByCode(code);
                    movie.setStatus(MoviesStatus.匹配成功);
                    movie.setUrl(site.getDomain()+detailUrl);
                    moviesRepository.save(movie);
                } else{
                    resMap.put(detailUrl, code);
                    this.log.warn(String.format("解析字段与日期年份不匹配：[%s]:[%s]", onDays, year));
                }
            } else {
                this.log.info(String.format("剔除结果：《%s》与参数《%s》不匹配", mName, searchKeyName));
            }

        }

        for (String key : resMap.keySet()) {
            //抓取名称匹配成功的
            if (key.contains("movie")) {
//                page.addTargetRequest(new Request(site.getDomain() + key).addHeader("code", resMap.get(key)).setPriority(0));
                imgPageProcessor.start(imgPageProcessor,new Request(site.getDomain() + key).addHeader("code", resMap.get(key)),getDownloader());
                Movies movie = moviesRepository.findByCode(resMap.get(key));
                movie.setStatus(MoviesStatus.自动匹配);
                movie.setUrl(site.getDomain()+key);
                moviesRepository.save(movie);
            }
        }
    }

    public Downloader getDownloader() {
        //以下订单号，secret参数 须自行改动
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy(ip, port)));
        return httpClientDownloader;
    }
    public void start(MoviesPageProcessor processor) {


        List<Movies> moviesList = moviesRepository.findAll();

        spider = spider.create(processor);
        spider.setDownloader(getDownloader());
        spider.setScheduler(scheduler);
        for (Movies movies : moviesList) {
            scheduler.push(new Request(searchUrl.replace("REPLACE_KEY", movies.getName()))
                    .addHeader("code", movies.getCode()).setPriority(1), spider);
        }
        spider.thread(3).start();
        /*for (int i = 0; i < 10; i++) {
            scheduler.push(new Request(searchUrl.replace("REPLACE_KEY", moviesList.get(i).getName()))
                    .addHeader("code", moviesList.get(i).getCode()).setPriority(1), spider);
        }
        spider.start();*/
    }

    /**
     * http://www.xdaili.cn/usercenter/order
     *
     * @param orderno
     * @param secret
     * @param timestamp
     * @return
     */

    public static String authHeader(String orderno, String secret, int timestamp) {
        //拼装签名字符串
        String planText = String.format("orderno=%s,secret=%s,timestamp=%d", orderno, secret, timestamp);

        //计算签名
        String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(planText).toUpperCase();

        //拼装请求头Proxy-Authorization的值
        String authHeader = String.format("sign=%s&orderno=%s&timestamp=%d", sign, orderno, timestamp);
        return authHeader;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
