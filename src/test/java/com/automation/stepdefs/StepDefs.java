package com.automation.stepdefs;



import com.automation.core.WebDriverFactory;
import com.automation.pageobjects.*;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class StepDefs {

    private static final Logger logger = LogManager.getLogger(StepDefs.class);

    
    WebDriver driver;
    String base_url = "https://amazon.in";
    int implicit_wait_timeout_in_sec = 20;
    Scenario scn; // this is set in the @Before method

    CmnPageObjects cmnPageObjects;
    HomePageObjects homePageObjects;
    SignInPageObjects signInPageObjects;
    SearchPageObjects searchPageObjects;
    ProductDescriptionPageObjects productDescriptionPageObjects;

   
    @Before
    public void setUp(Scenario scn) throws Exception {
        this.scn = scn; //Assign this to class variable, so that it can be used in all the step def methods

        //Get the browser name by default it is chrome
        String browserName = WebDriverFactory.getBrowserName();
        driver = WebDriverFactory.getWebDriverForBrowser(browserName);
        logger.info("Browser invoked.");

        //Init Page Object Model Objects
        cmnPageObjects = new CmnPageObjects(driver);
        homePageObjects = new HomePageObjects(driver);
        signInPageObjects = new SignInPageObjects(driver);
        searchPageObjects = new SearchPageObjects(driver);
        productDescriptionPageObjects = new ProductDescriptionPageObjects(driver);
    }

    
    @After(order=1)
    public void cleanUp(){
        WebDriverFactory.quitDriver();
        scn.log("Browser Closed");
    }

    
    @After(order=2)
    public void takeScreenShot(Scenario s) {
        if (s.isFailed()) {
            TakesScreenshot scrnShot = (TakesScreenshot)driver;
            byte[] data = scrnShot.getScreenshotAs(OutputType.BYTES);
            scn.attach(data, "image/png","Failed Step Name: " + s.getName());
        }else{
            scn.log("Test case is passed, no screen shot captured");
        }
    }

    
    @Given("User navigated to the home application url")
    public void user_navigated_to_the_home_application_url() {
        WebDriverFactory.navigateToTheUrl(base_url);
        scn.log("Browser navigated to URL: " + base_url);

        String expected = "Online Shopping site in India: Shop Online for Mobiles, Books, Watches, Shoes and More - Amazon.in";
        cmnPageObjects.validatePageTitleMatch(expected);
    }

    @When("User Search for product {string}")
    public void user_search_for_product(String productName) {
        cmnPageObjects.SetSearchTextBox(productName);
        cmnPageObjects.ClickOnSearchButton();
        scn.log("Product Searched: " + productName);
    }

    @Then("Search Result page is displayed")
    public void search_result_page_is_displayed() {
        searchPageObjects.ValidateProductSearchIsSuccessfull();
    }

    @When("User click on any product")
    public void user_click_on_any_product() {
        searchPageObjects.ClickOnTheProductLink(0);//0 here means click on the first link
     }

    @Then("Product Description is displayed in new tab")
    public void product_description_is_displayed_in_new_tab() {
        WebDriverFactory.switchBrowserToTab();
        scn.log("Switched to the new window/tab");

        productDescriptionPageObjects.ValidateProductTileIsCorrectlyDisplayed();
     //   productDescriptionPageObjects.ValidateAddToCartButtonIsCorrectlyDisplayed();
    }
    
    //***************************************************************************************
    //*******************************Search Feature*********************************************
    //***************************************************************************************
    
    //Scenario-01
    @When("User add the products with defined price range and quantity listed below")
    public void user_add_the_products_with_defined_price_range_and_quantity_listed_below(List<Map<String,String>> data) {
        for (int i=0; i<=data.size()-1;i++){
            searchAndAddProducts(data,i);
            scn.log("First Product added and searched. " + data.get(i).toString());
        } 
        
        }

    @Then("User cart is updated with the products and quantity")
    public void user_cart_is_updated_with_the_products_and_quantity() {
       
    }
    
    // Common Method to Iterated
    public void searchAndAddProducts(List<Map<String,String>> data, int index){
        String product_name = data.get(index).get("ITEM");
        int product_price_limit = Integer.parseInt(data.get(index).get("PRICE_LESS_THAN"));
        String product_quantity = data.get(index).get("QUANTITY");

       
        user_search_for_product(product_name);

       
        List<WebElement> list_product_links = driver.findElements(By.xpath("//div[@class='sg-row']//a[@class='a-link-normal a-text-normal']"));

        
        List<WebElement> list_product_prices = driver.findElements(By.xpath("//div[@class='sg-row']//span[@class='a-price-whole']"));

        int product_link_index = -1;// this value is kept negative, to check later
        //Loop through the List
        for (int i=0;i< list_product_prices.size();i++){
            //Value is to be captured, then , (comma) is to be removed and then it is to be converted to a integer.
            //Below all done in a single step and value stored in temp variable
            int temp = Integer.parseInt(list_product_prices.get(i).getText().replace(",",""));
            if (temp<product_price_limit){// if product is less then the price mentioned
                product_link_index = i;
                scn.log("Product found with in the price range. ");
                break;
            }
        }

        //If no product is found in the above loop.
        if (product_link_index==-1){
            scn.log("No product found with in the price range");
            Assert.fail("No product found on page 1 which has price less then mentioned amount");
        }

        //if a product with required price is found then click on the link.
        //Save the name of the Product
        String product_text = list_product_links.get(product_link_index).getText();
        scn.log("Product found with in the price range: " + product_text);
        list_product_links.get(product_link_index).click();

        //Product description page will be opened
        product_description_is_displayed_in_new_tab();
        scn.log("Product Description is displayed in new tab.");

        //On Product Description Page Select Quantity as mentioned in the feature file
        productDescriptionPageObjects.selectQuantity(product_quantity);
        scn.log("Quantity Selected. " + product_quantity);

        //Click on add to cart Button on product Description Page
        productDescriptionPageObjects.clickOnAddToCartButton();
        scn.log("Add to cart to button clicked.");
       productDescriptionPageObjects.checkAddedToCartMessageIsDisplayed();
        scn.log("Add to cart message is displayed");

        driver.close();
        scn.log("Product description tab is closed.");

       
        WebDriverFactory.switchToOriginalTab();
        scn.log("Driver switched to original tab/window");
    
    }
    

    //Scenario-02 
    @When("User enters minimum price as {string} and maximum price as {string}")
    public void user_enters_minimum_price_as_and_maximum_price_as_mentioned_in_below_table(String min, String max) {
        searchPageObjects.FilterSearchResultByPrice(min,max);
    }

    @Then("Verify that Search results gets filtered with price range between {int} and {int}")
    public void search_results_gets_filtered_with_price_range_between_and(int min, int max) {
        searchPageObjects.VerifyThatSearchedProductsAreInPriceRange(min,max);
    }
  
    }



    



