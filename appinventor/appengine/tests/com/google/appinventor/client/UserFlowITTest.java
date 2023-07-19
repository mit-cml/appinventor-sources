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

//import com.google.gwt.junit.client.GWTTestCase;

import junit.framework.TestCase;

public class UserFlowITTest extends TestCase {
    private WebDriver driver;

    @Before
    public void setUpdriver() throws InterruptedException {
        // Set up WebDriver configuration
        System.setProperty("webdriver.chrome.driver", "${lib.dir}/chromedriver/chromedriver.exe");
        driver = new ChromeDriver();
    }

    @Test
    public void testLogin() {
        // Navigate to the application URL
        //Option to choose google account link
        //driver.get("http://localhost:8888/_ah/login?continue=http%3A%2F%2Flocalhost%3A8888%2Flogin%2Fgoogle%3Flocale%3Den");
        //login page
        driver.get("http://localhost:8888/login/");

        // Submit the login button of test email to login
        //driver.findElement(By.id("btn-login")).submit();
        //driver.findElement(By.linkText("Click Here to use your Google Account to login")).submit();

        // Assert that the application title is correct
        //assertEquals(element.getText(), "Log In");
    }

    @After
    public void tearDowndriver() {
        // Close the WebDriver
        driver.quit();
    }
    
}
