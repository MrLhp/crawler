package com.lhp.crawler.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WindowUtil {
    /**
     * 滚动窗口。
     * @param driver
     * @param height
     */
    public static void scroll(WebDriver driver, int height){
        ((JavascriptExecutor)driver).executeScript("window.scrollTo(0,"+height+" );");
    }
    /**
     * 重新调整窗口大小，以适应页面，需要耗费一定时间。建议等待合理的时间。
     * @param driver
     */
    public static void loadAll(WebDriver driver){
        Dimension od=driver.manage().window().getSize();
        int width=driver.manage().window().getSize().width;
        //尝试性解决：https://github.com/ariya/phantomjs/issues/11526问题
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        long height=(Long)((JavascriptExecutor)driver).executeScript("return document.body.scrollHeight;");
        driver.manage().window().setSize(new Dimension(width, (int)height));
        driver.navigate().refresh();
    }
    public static void taskScreenShot(WebDriver driver,File saveFile){
        File src=((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(src, saveFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static void changeWindow(WebDriver driver){
        // 获取当前页面句柄
        String handle = driver.getWindowHandle();
        // 获取所有页面的句柄，并循环判断不是当前的句柄，就做选取switchTo()
        for (String handles : driver.getWindowHandles()) {
            if (handles.equals(handle))
                continue;
            driver.switchTo().window(handles);
        }
    }
}
