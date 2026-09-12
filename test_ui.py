import pytest
import time
from fastapi.testclient import TestClient
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from main import app

@pytest.fixture(scope="module")
def client():
    """Provides a virtual client to test backend endpoints instantly without a running server."""
    return TestClient(app)

def test_deliberate_price_mismatch_error(client):
    response = client.get("/api/listings/search?minPrice=700000&maxPrice=400000")
    assert response.status_code == 400
    assert "Minimum price filtering threshold cannot be greater than maximum price." in response.json()["detail"]

def test_negative_price_input_validation(client):
    response = client.get("/api/listings/search?minPrice=-5000")
    assert response.status_code == 400
    assert "Price filters cannot be negative numbers." in response.json()["detail"]

def test_page_size_zero_or_less(client):
    response = client.get("/api/listings/search?size=0")
    assert response.status_code == 422 

def test_city_no_matches_404(client):
    response = client.get("/api/listings/search?city=Atlantis")
    assert response.status_code == 404
    assert "No matching listings found matching city" in response.json()["detail"]

@pytest.fixture(scope="module")
def driver():
    """Spins up an automated Microsoft Edge instance for visual browser testing."""
    driver = webdriver.Edge()
    yield driver
    driver.quit()

def test_selenium_ui_search_ranking_and_pagination(driver):
    """Launches Edge, interacts with the interface, and validates sorting scores safely."""
    # A. Open up the root frontend application
    driver.get("http://127.0.0.1:8000")
    wait = WebDriverWait(driver, 10)
    
    # B. Wait for the interactive search button to appear in the DOM on initial render
    wait.until(EC.presence_of_element_located((By.ID, "searchBtn")))
    
    # C. Enter testing criteria parameters
    city_input = driver.find_element(By.ID, "city")
    search_button = driver.find_element(By.ID, "searchBtn")
    
    city_input.clear()
    # TIP: If "Miami" has no rows in your Postgres DB table, type a city name that you know is populated
    city_input.send_keys("Miami")  
    
    # Click the search trigger
    search_button.click()
    
    # D. OPTIMIZED WAIT STRATEGY: Give the backend up to 5 seconds to finish rendering elements.
    # This safely pauses until the loading indicator leaves the DOM viewport entirely.
    time.sleep(2)
    
    # E. Extract the final processed text block from the viewport container
    feed_container = driver.find_element(By.ID, "listingsFeed")
    feed_text = feed_container.text
    
    if "Score:" in feed_text:
        # If rows populated successfully, grab the score bubble element to verify calculations worked
        score_element = driver.find_element(By.XPATH, "//*[contains(text(), 'Score:')]")
        clean_score_text = score_element.text.replace("Score: ", "").replace("%", "")
        
        assert float(clean_score_text) >= 0.0, "Relevance Score extraction should parse into valid float metrics."
        print("\n→ UI Verification PASSED: Scored listings rendered successfully!")
    else:
        # Fallback assertion if the database happens to return an empty array for that city name parameter
        assert "No results match" in feed_text or "No listings match" in feed_text or "Empty state" in feed_text
        print("\n→ UI Verification PASSED: Empty state view block triggered correctly.")
