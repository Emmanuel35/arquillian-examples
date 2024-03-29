/*
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.example.ui;

import static org.junit.Assert.*;
import static org.jboss.arquillian.graphene.Graphene.*;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.findby.FindByJQuery;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * @author <a href="http://community.jboss.org/people/dan.j.allen">Dan Allen</a>
 * @author <a href="http://community.jboss.org/people/kpiwko">Karel Piwko</a>
 */
@RunWith(Arquillian.class)
public class LoginScreenSeleniumTest {
    private static final String WEBAPP_SRC = "src/main/webapp";
            
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "login.war")
            .addClasses(LoginController.class, User.class, Credentials.class)
            // .addAsWebResource(new File(WEBAPP_SRC), "login.xhtml")
            // .addAsWebResource(new File(WEBAPP_SRC), "home.xhtml")
            .merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)
                .importDirectory(WEBAPP_SRC).as(GenericArchive.class),
                "/", Filters.include(".*\\.xhtml$"))
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsWebInfResource(
                new StringAsset("<faces-config version=\"2.0\"/>"),
                "faces-config.xml");
    }
    
    @Drone
    private WebDriver browser;
    
    @FindBy(id = "login")
    private WebElement loginButton;

    @FindBy(tagName = "li")                     // 2. injects a first element with given tag name
    private WebElement facesMessage;

    @FindByJQuery("p:visible")                  // 3. injects an element using jQuery selector
    private WebElement signedAs;

    @FindBy(css = "input[type=submit]")
    private WebElement whoAmI;
    
    @FindBy                                     // 1. injects an element by default location strategy ("idOrName")
    private WebElement userName;

    @FindBy
    private WebElement password;
    
    @ArquillianResource
    URL deploymentUrl;
    
    @Test
    public void should_login_with_valid_credentials() {
    	browser.get(deploymentUrl.toExternalForm() + "login.jsf");      // 1. open the tested page

        userName.sendKeys("demo");                                      // 3. control the page
        password.sendKeys("demo");

        guardHttp(loginButton).click();

        assertEquals("Welcome", facesMessage.getText().trim());
        whoAmI.click();
        waitAjax().until().element(signedAs).is().present();
        assertTrue(signedAs.getText().contains("demo"));

    }
}
