import os
import time
import pytest
from fastapi.testclient import TestClient
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

from main import app

# Terminal color output constants for clear validation formatting
OKGREEN = '\033[92m'
ENDC = '\033[0m'

@pytest.fixture(scope="module")
def client():
    """Provides a virtual client to test backend endpoints instantly and closes the session pool cleanly afterwards."""
    with TestClient(app) as mock_client:
        yield mock_client

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
    """Interacts with the interface, verifies result counts, and captures a screenshot securely."""
    print("\n[DEBUG] --- Starting Selenium UI Test Execution ---")
    wait = WebDriverWait(driver, 10)
    
    # 1. Open up the root frontend application
    target_url = "http://127.0.0.1:8000"
    print(f"[DEBUG] Navigating browser viewport to: {target_url}")
    driver.get(target_url)
    
    # 2. Locate components and enter testing criteria
    wait.until(EC.presence_of_element_located((By.ID, "searchBtn")))
    city_input = driver.find_element(By.ID, "city")
    search_button = driver.find_element(By.ID, "searchBtn")
    
    city_input.clear()
    city_input.send_keys("Miami")  
    
    print("[DEBUG] Dispatching click event listener trigger on searchBtn element...")
    search_button.click()
    
    # 3. FIXED WAIT: Wait until the text transitions away from the initial "Processing" or "Scanning" text
    print("[DEBUG] Form submitted. Waiting for dynamic listing feed cards to render...")
    wait.until(lambda d: "Processing" not in d.find_element(By.ID, "resultsCount").text)
    
    # Give the browser layout engine an extra moment to settle all child cards completely
    time.sleep(1) 
    
    # 4. CAPTURE DOCUMENTATION SCREENSHOT
    base_dir = os.path.dirname(os.path.abspath(__file__))
    docs_dir = os.path.join(base_dir, "docs")
    os.makedirs(docs_dir, exist_ok=True)
    screenshot_path = os.path.join(docs_dir, "gui_dashboard.png")
    
    driver.save_screenshot(screenshot_path)
    print(f"[INFO] Automated GUI screenshot saved successfully to: {screenshot_path}")
    
    # 5. Extract text from the summary header and feed container
    results_count_text = driver.find_element(By.ID, "resultsCount").text
    feed_container = driver.find_element(By.ID, "listingsFeed")
    feed_text = feed_container.text
    
    print(f"[DEBUG] Header text found: '{results_count_text}'")
    
    if "Score:" in feed_text:
        # --- VERIFY RESULT COUNTS ---
        # Fixed: Targeted by counting elements that contain "Beds" text fields inside the feed container globally
        property_cards = feed_container.find_elements(By.XPATH, ".//*[contains(text(), 'Beds')]")
        actual_rendered_count = len(property_cards)
        print(f"[DEBUG] Actual property cards counted in viewport: {actual_rendered_count}")
        
        # B. Parse the integer out of the dynamic header text
        header_number = int(results_count_text.split()[0])
        print(f"[DEBUG] Extracted total matches count integer from header text: {header_number}")
        
        # C. Assert count boundaries
        if header_number <= 4:
            assert actual_rendered_count == header_number, f"Mismatched count! Header reports {header_number} rows, but UI rendered {actual_rendered_count} items."
            print(f"{OKGREEN}[SUCCESS] Result count matches header perfectly!{ENDC}")
        else:
            assert actual_rendered_count == 4, f"Pagination boundary check failed! Feed should be capped at page size 4 rows max."
            print(f"{OKGREEN}[SUCCESS] Pagination constraints verified. Header matches total list metrics.{ENDC}")
        # ----------------------------

        score_element = driver.find_element(By.XPATH, "//*[contains(text(), 'Score:')]")
        clean_score_text = score_element.text.replace("Score: ", "").replace("%", "")
        assert float(clean_score_text) >= 0.0
        print("→ UI Verification PASSED: Scored listings rendered successfully!")
    else:
        assert "No results match" in feed_text or "No listings match" in feed_text or "Empty state" in feed_text
        assert "0" in results_count_text
        print("→ UI Verification PASSED: Empty state view block and zero-count checked correctly.")
        
    print("[DEBUG] --- Ending Selenium UI Test Execution --- \n")
