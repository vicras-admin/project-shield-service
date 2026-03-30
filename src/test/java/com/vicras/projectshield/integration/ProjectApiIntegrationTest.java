package com.vicras.projectshield.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class ProjectApiIntegrationTest extends BaseIntegrationTest {

    private String phaseId;

    @BeforeEach
    void setUpPhase() {
        String phaseBody = """
            {
                "name": "Test Phase",
                "startDate": "2025-01-01",
                "endDate": "2025-12-31",
                "type": "annual"
            }
            """;

        phaseId = asAdmin()
                .contentType(ContentType.JSON)
                .body(phaseBody)
        .when()
                .post("/phases")
        .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    void getProjectsByPhase_returnsEmptyList() {
        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    void getProjectsByPhase_asViewer_returnsOk() {
        asViewer()
                .contentType(ContentType.JSON)
        .when()
                .get("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(200);
    }

    @Test
    void createProject_withValidData_returnsCreated() {
        String requestBody = """
            {
                "name": "Claims Modernization",
                "description": "Modernize the claims processing system",
                "justification": "Reduce processing time by 50%",
                "sponsor": "Jane Smith",
                "estimatedBudget": 500000,
                "startDate": "2025-02-01",
                "endDate": "2025-06-30",
                "status": "strategic",
                "overallScore": 4.2,
                "stackRank": 1,
                "ratings": {
                    "strategicAlignment": 5,
                    "financialBenefit": 4,
                    "riskProfile": 3,
                    "feasibility": 4,
                    "regulatoryCompliance": 5
                },
                "staffingNeeds": [
                    {"role": "Backend Developer", "count": 2, "durationWeeks": 12},
                    {"role": "Frontend Developer", "count": 1, "durationWeeks": 8}
                ]
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Claims Modernization"))
                .body("status", equalTo("STRATEGIC"))
                .body("overallScore", equalTo(4.2f))
                .body("ratings.strategicAlignment", equalTo(5))
                .body("staffingNeeds", hasSize(2));
    }

    @Test
    void createProject_asViewer_returnsForbidden() {
        String requestBody = """
            {
                "name": "Unauthorized Project",
                "startDate": "2025-02-01",
                "endDate": "2025-06-30"
            }
            """;

        asViewer()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(403);
    }

    @Test
    void createProject_withDatesOutsidePhase_returnsBadRequest() {
        String requestBody = """
            {
                "name": "Invalid Project",
                "startDate": "2024-01-01",
                "endDate": "2025-06-30"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(400)
                .body("message", containsString("phase dates"));
    }

    @Test
    void createProject_withEndBeforeStart_returnsBadRequest() {
        String requestBody = """
            {
                "name": "Invalid Project",
                "startDate": "2025-06-30",
                "endDate": "2025-02-01"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(400);
    }

    @Test
    void getProjectById_returnsProject() {
        String createBody = """
            {
                "name": "Test Project",
                "startDate": "2025-03-01",
                "endDate": "2025-05-31"
            }
            """;

        String projectId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/projects/{id}", projectId)
        .then()
                .statusCode(200)
                .body("id", equalTo(projectId))
                .body("name", equalTo("Test Project"));
    }

    @Test
    void updateProject_withValidData_returnsUpdated() {
        String createBody = """
            {
                "name": "Original Project",
                "startDate": "2025-04-01",
                "endDate": "2025-07-31"
            }
            """;

        String projectId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        String updateBody = """
            {
                "name": "Updated Project",
                "description": "Now with description",
                "startDate": "2025-04-01",
                "endDate": "2025-08-31",
                "status": "accepted"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(updateBody)
        .when()
                .put("/projects/{id}", projectId)
        .then()
                .statusCode(200)
                .body("name", equalTo("Updated Project"))
                .body("description", equalTo("Now with description"));
    }

    @Test
    void updateStackRank_withValidData_returnsUpdated() {
        String createBody = """
            {
                "name": "Ranked Project",
                "startDate": "2025-05-01",
                "endDate": "2025-09-30",
                "stackRank": 5
            }
            """;

        String projectId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        String updateBody = """
            {
                "stackRank": 1
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(updateBody)
        .when()
                .put("/projects/{id}/stack-rank", projectId)
        .then()
                .statusCode(200)
                .body("stackRank", equalTo(1));
    }

    @Test
    void deleteProject_withValidId_returnsNoContent() {
        String createBody = """
            {
                "name": "Project to Delete",
                "startDate": "2025-06-01",
                "endDate": "2025-10-31"
            }
            """;

        String projectId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .delete("/projects/{id}", projectId)
        .then()
                .statusCode(204);

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/projects/{id}", projectId)
        .then()
                .statusCode(404);
    }

    @Test
    void createProject_allStatuses_areValid() {
        String[] statuses = {"ACCEPTED", "STRATEGIC", "REJECTED"};

        for (int i = 0; i < statuses.length; i++) {
            String requestBody = String.format("""
                {
                    "name": "Project Status %s",
                    "startDate": "2025-0%d-01",
                    "endDate": "2025-0%d-28",
                    "status": "%s"
                }
                """, statuses[i], i + 1, i + 2, statuses[i]);

            asAdmin()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
            .when()
                    .post("/phases/{phaseId}/projects", phaseId)
            .then()
                    .statusCode(201)
                    .body("status", equalTo(statuses[i]));
        }
    }
}
