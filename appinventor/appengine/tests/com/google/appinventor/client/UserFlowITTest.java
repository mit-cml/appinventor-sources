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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.Random;
//import com.google.gwt.core.client.Duration;
//import java.time.Duration;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

public class UserFlowITTest extends TestCase {
    private RemoteWebDriver driver;

    @Before
    public void setUp() throws InterruptedException {
        // Set up WebDriver configuration
        //System.setProperty("webdriver.chrome.driver", "/usr/lib/chromium-browser/chromedriver");
        driver = new ChromeDriver();
    }

    @Test
    public void testLoginTestUser() {
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

    @Test
    public void testDismissProjectDialouge() {
        driver.get("http://localhost:8888/_ah/login?continue=http%3A%2F%2Flocalhost%3A8888%2Flogin%2Fgoogle%3Flocale%3Den");
        
        WebElement loginButton = driver.findElement(By.id("btn-login"));
        WebElement isAdminCheckbox = driver.findElement(By.id("isAdmin"));
        WebElement email=driver.findElement(By.id("email"));
        
        email.sendKeys("test@example.com");
        isAdminCheckbox.click();
        loginButton.submit();
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.navigate().to("http://localhost:8888/Ya_tos_form.html?locale=en");
        WebElement termsServiceButton = driver.findElement(By.className("ode-textbutton"));
        termsServiceButton.click();
        WebElement closeButton = driver.findElement(By.className("ode-bottomRow"));
        closeButton.click();
    }

    @Test
    public void testStartNewProject() {
        driver.get("http://localhost:8888/_ah/login?continue=http%3A%2F%2Flocalhost%3A8888%2Flogin%2Fgoogle%3Flocale%3Den");
        
        WebElement loginButton = driver.findElement(By.id("btn-login"));
        WebElement isAdminCheckbox = driver.findElement(By.id("isAdmin"));
        WebElement email=driver.findElement(By.id("email"));
        
        email.sendKeys("test@example.com");
        isAdminCheckbox.click();
        loginButton.submit();
        
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        
        driver.navigate().to("http://localhost:8888/Ya_tos_form.html?locale=en");
        WebElement termsServiceButton = driver.findElement(By.className("ode-textbutton"));
        termsServiceButton.click();
        
        WebElement closeButton = driver.findElement(By.className("ode-bottomRow"));
        closeButton.click();
        
        WebElement project = driver.findElement(By.xpath("/html/body/table/tbody/tr[1]/td/table/tbody/tr/td[2]/table/tbody/tr/td[1]/div"));
        project.click();
        driver.executeScript("['mouseover', 'mousedown', 'mouseup', 'click', 'mouseout'].forEach(action => arguments[0].dispatchEvent(new MouseEvent(action)));", project);
        
        WebElement startnp = driver.findElement(By.xpath("/html/body/div[4]/div/div/table/tbody/tr[3]/td"));
        startnp.click();

        //setting project name
        WebElement projectTitle = driver.findElement(By.xpath("/html/body/div[3]/div/table/tbody/tr[2]/td[2]/div/table/tbody/tr[1]/td/div/div/div/div/div/table/tbody/tr/td/table/tbody/tr[1]/td/table/tbody/tr/td[2]/input"));

        Random random = new Random();
        int randomNumber = random.nextInt(1000) + 1;
        String projectName = "Project"+randomNumber;

        driver.executeScript("arguments[0].setAttribute('value', '" + projectName + "');", projectTitle);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        WebElement Okbutton = driver.findElement(By.xpath("/html/body/div[3]/div/table/tbody/tr[2]/td[2]/div/table/tbody/tr[2]/td/table/tbody/tr/td[2]/button"));
        Okbutton.click();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        //WebElement on which drag and drop operation needs to be performed
        //WebElement fromElement = driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td/table/tbody/tr/td[2]/div/div[2]/table/tbody/tr[2]/td/table/tbody/tr/td[1]/div/div/div/div/div/table/tbody/tr[3]/td/table/tbody/tr[2]/td/table/tbody/tr[1]"));

        //WebElement to which the above object is dropped
        // WebElement toElement = driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td/table/tbody/tr/td[2]/div/div[2]/table/tbody/tr[2]/td/table/tbody/tr/td[2]/div/div/div/div/div/table/tbody/tr/td/div/div[1]/table/tbody/tr[1]/td/table/tbody/tr[4]/td/div/div[1]/div[1]"));
        // Actions builder = new Actions(driver);

        //Building a drag and drop action
        // Action dragAndDrop = builder.clickAndHold(fromElement)
        // .moveToElement(toElement)
        // .release(toElement)
        // .build();

        //Performing the drag and drop action
        //dragAndDrop.perform();

        project.click();
        driver.executeScript("['mouseover', 'mousedown', 'mouseup', 'click', 'mouseout'].forEach(action => arguments[0].dispatchEvent(new MouseEvent(action)));", project);
        WebElement moveToTrash = driver.findElement(By.xpath("/html/body/div[5]/div/div/table/tbody/tr[6]/td"));
        moveToTrash.click();

        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        //WebElement moveToTrash = driver.findElement(By.xpath("/html/body/div[4]/div/div/table/tbody/tr[6]"));
        //moveToTrash.click();
        // Find the fifth option within the dropdown
        /*WebElement fifthOption = driver.findElement(By.id("gwt-uid-5"));

        // Simulate a click event on the fifth option
        String script = "var event = new MouseEvent('click', { bubbles: true, cancelable: true, view: window });" +
                        "arguments[0].dispatchEvent(event);";
        ((JavascriptExecutor) driver).executeScript(script, fifthOption);
        //driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);*/
        driver.switchTo().alert().accept();
    }

  @After
    public void tearDown() {
        // Close the WebDriver
        driver.quit();
    }
    
}





