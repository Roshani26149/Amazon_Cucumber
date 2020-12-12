@ui @healthcheck
Feature: E-commerce Project Web Site Health Check

 
  Background: Navigation to the URL
    Given User navigated to the home application url

  Scenario: User is able to Open the browser, navigate to the URL and Search for Product
    When User Search for product "Laptop"
    Then Search Result page is displayed

  Scenario: User is click on the Product and check the Product Details
    And User Search for product "earphone"
    When User click on any product
    Then Product Description is displayed in new tab

  Scenario Outline: User is able to search multiple products
    When User Search for product "<product_name>"
    Then Search Result page is displayed
    Examples:
      |product_name|
      | laptop     |
      | earphone   |
      | computer   |

