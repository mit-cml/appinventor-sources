package com.google.appinventor.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.google.gwt.junit.client.GWTTestCase;

public class TopToolbarTest extends GWTTestCase {
    private WebDriver driver;

    @Before
    public void setUpdriver() {
        // Set up WebDriver configuration
        System.setProperty("webdriver.chrome.driver", "${lib.dir}/chromedriver/chromedriver.exe");
        driver = new ChromeDriver();
    }

    //@Test
    public void testTitle() {
        // Navigate to the application URL
        driver.get("http://localhost:8888/?locale=en");

        // Get the application title
        String title = driver.getTitle();

        TopToolbar topToolbar = new TopToolbar();

        // Assert that the application title is correct
        Assert.assertEquals("Projects", title);
    }

    @After
    public void tearDowndriver() {
        // Close the WebDriver
        driver.quit();
    }
    
    @Override
    public String getModuleName() {
        return "com.google.appinventor.YaClient";
    }
}
