package com.vicras.projectshield.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class PhaseApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAllPhases_returnsOk() {
        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/phases")
        .then()
                .statusCode(200)
                .body("$", is(notNullValue()));
    }

    @Test
    void getAllPhases_asViewer_returnsOk() {
        asViewer()
                .contentType(ContentType.JSON)
        .when()
                .get("/phases")
        .then()
                .statusCode(200);
    }

    @Test
    void createPhase_withValidData_returnsCreated() {
        String requestBody = """
            {
                "name": "Q1 2025",
                "description": "First quarter planning",
                "startDate": "2025-01-01",
                "endDate": "2025-03-31",
                "type": "quarter"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/phases")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Q1 2025"))
                .body("type", equalTo("quarter"))
                .body("startDate", equalTo("2025-01-01"))
                .body("endDate", equalTo("2025-03-31"));
    }

    @Test
    void createPhase_asViewer_returnsForbidden() {
        String requestBody = """
            {
                "name": "Unauthorized Phase",
                "startDate": "2025-01-01",
                "endDate": "2025-03-31",
                "type": "quarter"
            }
            """;

        asViewer()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/phases")
        .then()
                .statusCode(403);
    }

    @Test
    void createPhase_withInvalidDateRange_returnsBadRequest() {
        String requestBody = """
            {
                "name": "Invalid Phase",
                "startDate": "2025-06-01",
                "endDate": "2025-03-31",
                "type": "quarter"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/phases")
        .then()
                .statusCode(400)
                .body("message", containsString("End date"));
    }

    @Test
    void createPhase_withMissingRequiredFields_returnsBadRequest() {
        String requestBody = """
            {
                "description": "Missing required fields"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/phases")
        .then()
                .statusCode(400);
    }

    @Test
    void getPhaseById_returnsPhaseWithProjects() {
        String createBody = """
            {
                "name": "Q2 2025",
                "description": "Second quarter",
                "startDate": "2025-04-01",
                "endDate": "2025-06-30",
                "type": "quarter"
            }
            """;

        String phaseId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/phases")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/phases/{id}", phaseId)
        .then()
                .statusCode(200)
                .body("id", equalTo(phaseId))
                .body("name", equalTo("Q2 2025"))
                .body("projects", notNullValue());
    }

    @Test
    void updatePhase_withValidData_returnsUpdated() {
        String createBody = """
            {
                "name": "Original Phase",
                "startDate": "2025-07-01",
                "endDate": "2025-09-30",
                "type": "quarter"
            }
            """;

        String phaseId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/phases")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        String updateBody = """
            {
                "name": "Updated Phase",
                "description": "Now with description",
                "startDate": "2025-07-01",
                "endDate": "2025-09-30",
                "type": "quarter"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(updateBody)
        .when()
                .put("/phases/{id}", phaseId)
        .then()
                .statusCode(200)
                .body("name", equalTo("Updated Phase"))
                .body("description", equalTo("Now with description"));
    }

    @Test
    void deletePhase_withValidId_returnsNoContent() {
        String createBody = """
            {
                "name": "Phase to Delete",
                "startDate": "2025-10-01",
                "endDate": "2025-12-31",
                "type": "quarter"
            }
            """;

        String phaseId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/phases")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .delete("/phases/{id}", phaseId)
        .then()
                .statusCode(204);
    }

    @Test
    void createPhase_allTypes_areValid() {
        String[] types = {"quarter", "half", "annual", "custom"};

        for (int i = 0; i < types.length; i++) {
            String requestBody = String.format("""
                {
                    "name": "Phase Type %s",
                    "startDate": "202%d-01-01",
                    "endDate": "202%d-12-31",
                    "type": "%s"
                }
                """, types[i], 6 + i, 6 + i, types[i]);

            asAdmin()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
            .when()
                    .post("/phases")
            .then()
                    .statusCode(201)
                    .body("type", equalTo(types[i]));
        }
    }
}
