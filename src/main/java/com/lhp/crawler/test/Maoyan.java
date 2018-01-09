package com.lhp.crawler.test;

import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import us.codecraft.webmagic.downloader.PhantomJSDownloader;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Maoyan {
    public static void main(String[] args) {
        DesiredCapabilities dcaps = new DesiredCapabilities();
        //截屏支持
        dcaps.setCapability("takesScreenshot", true);
        //css搜索支持
        dcaps.setCapability("cssSelectorsEnabled", true);
        //js支持
        dcaps.setJavascriptEnabled(true);

        //浏览器头
        dcaps.setCapability(
                PhantomJSDriverService.PHANTOMJS_PAGE_CUSTOMHEADERS_PREFIX
                        + "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.62 Safari/537.36");
        //驱动支持
        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,"/home/haipei/dev/phantomjs-2.5.0-beta-ubuntu-xenial/bin/phantomjs");
        //创建无界面浏览器对象

        PhantomJSDriver driver = new PhantomJSDriver(dcaps);

        try {
            driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
            //让浏览器访问猫眼票房页面
            driver.get("www.baidu.com");

            File src=((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            String srcfile="/home/haipei/data/"+ UUID.randomUUID().toString()+".png";
            FileUtils.copyFile(src, new File(srcfile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
