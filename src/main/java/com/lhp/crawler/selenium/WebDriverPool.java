package com.lhp.crawler.selenium;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class WebDriverPool {
    private int CAPACITY=5;
    private AtomicInteger refCount = new AtomicInteger(0);
    private static final String DRIVER_PHANTOMJS = "phantomjs";

    /**
     * store webDrivers available
     */
    private BlockingDeque<WebDriver> innerQueue = new LinkedBlockingDeque<WebDriver>(
            CAPACITY);

    private static String PHANTOMJS_PATH;
    private static DesiredCapabilities caps = DesiredCapabilities.phantomjs();
    static {
        PHANTOMJS_PATH = "D:\\haipei\\devEnv\\phantomjs\\bin\\phantomjs.exe";
        caps.setJavascriptEnabled(true);
        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
                PHANTOMJS_PATH);
        caps.setCapability("takesScreenshot", true);
        caps.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX
                        + "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.62 Safari/537.36");
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS,
                "--load-images=no");

    }

    public WebDriverPool() {
    }

    public WebDriverPool(int poolsize) {
        this.CAPACITY = poolsize;
        innerQueue = new LinkedBlockingDeque<WebDriver>(poolsize);
    }

    public WebDriver get() throws InterruptedException {
        WebDriver poll = innerQueue.poll();
        if (poll != null) {
            return poll;
        }
        if (refCount.get() < CAPACITY) {
            synchronized (innerQueue) {
                if (refCount.get() < CAPACITY) {

                    WebDriver mDriver = new PhantomJSDriver(caps);
                    // 尝试性解决：https://github.com/ariya/phantomjs/issues/11526问题
                    mDriver.manage().timeouts()
                            .pageLoadTimeout(60, TimeUnit.SECONDS);
                     mDriver.manage().window().setSize(new Dimension(1366,
                     768));
                    innerQueue.add(mDriver);
                    refCount.incrementAndGet();
                }
            }
        }
        return innerQueue.take();
    }

    public void returnToPool(WebDriver webDriver) {
        // webDriver.quit();
        // webDriver=null;
        innerQueue.add(webDriver);
    }

    public void close(WebDriver webDriver) {
        refCount.decrementAndGet();
        webDriver.close();
        webDriver.quit();
        webDriver = null;
    }

    public void shutdown() {
        try {
            for (WebDriver driver : innerQueue) {
                close(driver);
            }
            innerQueue.clear();
        } catch (Exception e) {
//            e.printStackTrace();
            this.log.warn("webdriverpool关闭失败",e);
        }
    }
}
