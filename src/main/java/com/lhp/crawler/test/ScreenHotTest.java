package com.lhp.crawler.test;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;

public class ScreenHotTest {

    private WebDriver driver;

    @BeforeClass
    public static void setupClass() {
        System.setProperty("phantomjs.binary.path",
                "/home/haipei/dev/phantomjs-2.5.0-beta-ubuntu-xenial/bin/phantomjs");
    }

    @Before
    public void setupTest() {
        driver = new PhantomJSDriver();
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void test() throws IOException {
        driver.get("www.baidu.com");

        File scrFile = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File(("screenshot.jpg")));
    }
}
