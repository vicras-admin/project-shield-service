package com.vicras.projectshield.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class DomainApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAllDomains_asAdmin_returnsOk() {
        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/domains")
        .then()
                .statusCode(200)
                .body("$", is(notNullValue()));
    }

    @Test
    void getAllDomains_asViewer_returnsOk() {
        asViewer()
                .contentType(ContentType.JSON)
        .when()
                .get("/domains")
        .then()
                .statusCode(200);
    }

    @Test
    void createDomain_withValidData_returnsCreated() {
        String requestBody = """
            {
                "name": "Claims Processing"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/domains")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Claims Processing"))
                .body("createdAt", notNullValue());
    }

    @Test
    void createDomain_asTeamLead_returnsForbidden() {
        String requestBody = """
            {
                "name": "Unauthorized Domain"
            }
            """;

        asTeamLead()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/domains")
        .then()
                .statusCode(403);
    }

    @Test
    void createDomain_withMissingName_returnsBadRequest() {
        String requestBody = """
            {
                "name": ""
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/domains")
        .then()
                .statusCode(400);
    }

    @Test
    void getDomainById_returnsDomain() {
        String createBody = """
            {
                "name": "Payment & Billing"
            }
            """;

        String domainId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/domains")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/domains/{id}", domainId)
        .then()
                .statusCode(200)
                .body("id", equalTo(domainId))
                .body("name", equalTo("Payment & Billing"));
    }

    @Test
    void updateDomain_withValidData_returnsUpdated() {
        String createBody = """
            {
                "name": "Original Domain"
            }
            """;

        String domainId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/domains")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        String updateBody = """
            {
                "name": "Updated Domain"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(updateBody)
        .when()
                .put("/domains/{id}", domainId)
        .then()
                .statusCode(200)
                .body("name", equalTo("Updated Domain"));
    }

    @Test
    void deleteDomain_withValidId_returnsNoContent() {
        String createBody = """
            {
                "name": "Domain to Delete"
            }
            """;

        String domainId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/domains")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .delete("/domains/{id}", domainId)
        .then()
                .statusCode(204);

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/domains/{id}", domainId)
        .then()
                .statusCode(404);
    }

    @Test
    void deleteDomain_asMember_returnsForbidden() {
        String createBody = """
            {
                "name": "Protected Domain"
            }
            """;

        String domainId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/domains")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asMember()
                .contentType(ContentType.JSON)
        .when()
                .delete("/domains/{id}", domainId)
        .then()
                .statusCode(403);
    }
}
