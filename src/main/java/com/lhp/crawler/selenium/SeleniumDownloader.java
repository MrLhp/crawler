package com.lhp.crawler.selenium;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.UrlUtils;

import java.util.Map;

@Slf4j
public class SeleniumDownloader implements Downloader {

    private int sleepTime=3000;
    private SeleniumAction action;
    private WebDriverPool webDriverPool=new WebDriverPool();

    public SeleniumDownloader(){
    }

    public SeleniumDownloader(int sleepTime,WebDriverPool pool){
        this(sleepTime,pool,null);
    }
    public SeleniumDownloader(int sleepTime,WebDriverPool pool,SeleniumAction action){
        this.sleepTime=sleepTime;
        this.action=action;
        if(pool!=null){
            webDriverPool=pool;
        }
    }
    public SeleniumDownloader setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }
    public void setOperator(SeleniumAction action){
        this.action=action;
    }
    @Override
    public Page download(Request request, Task task) {
        WebDriver webDriver;
        try {
            webDriver = webDriverPool.get();
        } catch (InterruptedException e) {
            log.warn("interrupted", e);
            return null;
        }
        log.info("downloading page " + request.getUrl());
        Page page = new Page();
        try {
            webDriver.get(request.getUrl());
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            webDriverPool.close(webDriver);
            page.setSkip(true);
            return page;
        }
//        WindowUtil.changeWindow(webDriver);
        WebDriver.Options manage = webDriver.manage();
        Site site = task.getSite();
        if (site.getCookies() != null) {
            for (Map.Entry<String, String> cookieEntry : site.getCookies()
                    .entrySet()) {
                Cookie cookie = new Cookie(cookieEntry.getKey(),
                        cookieEntry.getValue());
                manage.addCookie(cookie);
            }
        }
        manage.window().maximize();
        if(action!=null){
            action.execute(webDriver);
        }
        SeleniumAction reqAction=(SeleniumAction) request.getExtra("action");
        if(reqAction!=null){
            reqAction.execute(webDriver);
        }

        WebElement webElement = webDriver.findElement(By.xpath("/html"));
        String content = webElement.getAttribute("outerHTML");

        page.setRawText(content);
        page.setHtml(new Html(UrlUtils.canonicalizeUrl(content,
                webDriver.getCurrentUrl())));
        page.setUrl(new PlainText(webDriver.getCurrentUrl()));
        page.setRequest(request);
        webDriverPool.returnToPool(webDriver);
        return page;
    }

    @Override
    public void setThread(int i) {

    }
}
