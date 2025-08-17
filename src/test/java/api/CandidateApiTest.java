package api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CandidateApiTest {

    private String createdCandidateId;
    public String cookieValue;
    String baseUrl = "https://opensource-demo.orangehrmlive.com";

    @BeforeClass
    public void loginAndCaptureSession() throws InterruptedException {


         RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", "Admin")
                .formParam("password", "admin123")
                .when()
                .post(baseUrl + "/web/index.php/auth/validate")
                .then()
                .statusCode(302) // should redirect on success
                .extract()
                .response();

        WebDriver driver = new ChromeDriver();
        driver.get("https://opensource-demo.orangehrmlive.com");

// Perform login
        Thread.sleep(6000);
        driver.findElement(By.name("username")).sendKeys("Admin");
        driver.findElement(By.name("password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

// Wait for login to complete
        Thread.sleep(5000);

// Extract the session cookie
         cookieValue = driver.manage().getCookieNamed("orangehrm").getValue();
        System.out.println("Extracted cookie: " + cookieValue);
        driver.quit();


    }
    @Test(priority = 1)
    public void testAddCandidate()  {
        String requestBody = "{\n" +
                "  \"firstName\": \"ali\",\n" +
                "  \"lastName\": \"saeed\",\n" +
                "  \"email\": \"test1@example.com\"\n" +
                "}";



        Response response = RestAssured.given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .cookie("orangehrm", cookieValue)   // attach the captured cookie
                .body(requestBody)
                .when()
                .post("/web/index.php/api/v2/recruitment/candidates")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .response();
        createdCandidateId = response.jsonPath().getString("data.id");
        System.out.println("Add Candidate Response: " + response.asString());
    }

    @Test(priority = 2, dependsOnMethods = "testAddCandidate")
    public void testDeleteCandidate() {
        String deletePayload = "{ \"ids\": [\"" + createdCandidateId + "\"] }";

        given()
                .baseUri(baseUrl)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .cookie("orangehrm", cookieValue)
                .body(deletePayload)
                .when()
                .delete("/web/index.php/api/v2/recruitment/candidates")
                .then()
                .statusCode(200);

        System.out.println("🗑 Candidate deleted with ID: " + createdCandidateId);
    }
}
