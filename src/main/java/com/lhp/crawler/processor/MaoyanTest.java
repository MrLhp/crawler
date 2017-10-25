package com.lhp.crawler.processor;

import com.lhp.crawler.bean.ImageRegion;
import com.lhp.crawler.selenium.SeleniumAction;
import com.lhp.crawler.selenium.SeleniumDownloader;
import com.lhp.crawler.utils.ImageUtil;
import com.lhp.crawler.utils.TesseractOcrUtil;
import com.lhp.crawler.utils.WindowUtil;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MaoyanTest implements PageProcessor {
    private static Site site= Site.me().setCharset("UTF-8").setUserAgent(
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {

    }
    public  void start() {

        Spider cnSpider = Spider.create(this).setDownloader(new SeleniumDownloader(5000,null,new TestAction()))
//                .addUrl("https://shop34068488.taobao.com/?spm=a230r.7195193.1997079397.2.JLFlPa")
//                .addUrl("http://piaofang.maoyan.com/company/cinema?date=2017-01-18&webCityId=288&cityTier=0&page=1&cityName=%E6%8F%AD%E9%98%B3");
                .addUrl("http://piaofang.maoyan.com/company/cinema?date=2017-07-12&webCityId=10&cityTier=0&page=1&cityName=%25E4%25B8%258A%25E6%25B5%25B7&typeId=0&noSum=0");
//                .addPipeline(new JsonFilePipeline("D:\\data\\webmagicfile.json"))

        //SpiderMonitor.instance().register(cnSpider);
        cnSpider.run();
    }
    public static void main(String[] args) {
        new MaoyanTest().start();
    }

    private class TestAction implements SeleniumAction {

        @Override
        public void execute(WebDriver driver) {
            WindowUtil.loadAll(driver);
            try {
                Thread.sleep(5000);
//                WebDriverWait wait = new WebDriverWait(driver, 10);
                //wait.until(ExpectedConditions.presenceOfElementLocated(By.id("J_PromoPriceNum")));

                File src=((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
                String srcfile="D:\\data\\"+ UUID.randomUUID().toString()+".png";
                FileUtils.copyFile(src, new File(srcfile));
                List<WebElement> movielist=driver.findElements(By.xpath("//*[@id='cinema-tbody']/tr"));
//                movielist.remove(0);
                for(int i=1;i<movielist.size();i++){
                    int index=i+1;
                    String movieName=driver.findElement(By.xpath("//*[@id='cinema-tbody']/tr["+index+"]/td[2]")).getText();

                    String pattern = "//*[@id='cinema-tbody']/tr["+index+"]/td[3]";
                    WebElement tel=driver.findElement(By.xpath(pattern));

                    Point loc=tel.getLocation();
                    Dimension d=tel.getSize();
                    String cop_path="D:\\data\\crop\\current_piaofang_"+movieName+".png";
                    ImageUtil.crop(srcfile, cop_path, new ImageRegion(loc.x, loc.y, d.width+10, d.height));
                    System.out.println(TesseractOcrUtil.getByLangNum(cop_path));
                    FileUtils.deleteQuietly(new File(srcfile));
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
