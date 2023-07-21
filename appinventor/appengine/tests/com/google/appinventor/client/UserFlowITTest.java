// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import junit.framework.TestCase;

public class UserFlowITTest extends TestCase {
    private WebDriver driver;

    @Before
    public void setUp() throws InterruptedException {
        // Set up WebDriver configuration
        //System.setProperty("webdriver.chrome.driver", "/usr/lib/chromium-browser/chromedriver");
        driver = new ChromeDriver();
    }

    @Test
    public void loginTestUser() {
        // Navigate to the application URL
        //Option to choose google account link
        driver.get("http://localhost:8888/_ah/login?continue=http%3A%2F%2Flocalhost%3A8888%2Flogin%2Fgoogle%3Flocale%3Den");
        
        WebElement loginButton = driver.findElement(By.id("btn-login"));
        WebElement isAdminCheckbox = driver.findElement(By.id("isAdmin"));
        WebElement email=driver.findElement(By.id("email"));
        
        email.sendKeys("test@example.com");
        isAdminCheckbox.click();
        loginButton.submit();
        
        String actualUrl="http://localhost:8888/?locale=en";
        String expectedUrl= driver.getCurrentUrl();
        Assert.assertEquals(expectedUrl,actualUrl);
    }

  @After
    public void tearDown() {
        // Close the WebDriver
        driver.quit();
    }
    
}
