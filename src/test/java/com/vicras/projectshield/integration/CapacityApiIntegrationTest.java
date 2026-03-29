package com.vicras.projectshield.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class CapacityApiIntegrationTest extends BaseIntegrationTest {

    private String phaseId;

    @BeforeEach
    void setUpPhase() {
        String phaseBody = """
            {
                "name": "Capacity Test Phase",
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
    void getCapacityAllocation_withEmptyPhase_returnsEmptyAllocations() {
        asAdmin()
                .contentType(ContentType.JSON)
                .queryParam("phaseId", phaseId)
        .when()
                .get("/capacity/strategic")
        .then()
                .statusCode(200)
                .body("phaseId", equalTo(phaseId))
                .body("allocations", hasSize(0));
    }

    @Test
    void getCapacityAllocation_asViewer_returnsOk() {
        asViewer()
                .contentType(ContentType.JSON)
                .queryParam("phaseId", phaseId)
        .when()
                .get("/capacity/strategic")
        .then()
                .statusCode(200);
    }

    @Test
    void getCapacityAllocation_withProjectsAndStaff_returnsAllocations() {
        // Create a staff member with the "member" auth role
        String staffBody = """
            {
                "firstName": "Backend",
                "lastName": "Dev 1",
                "role": "member",
                "seniority": "senior",
                "hoursPerDay": 8
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(staffBody)
        .when()
                .post("/staff")
        .then()
                .statusCode(201);

        // Create a project with a staffing need matching the "member" role
        String projectBody = """
            {
                "name": "Test Project",
                "startDate": "2025-02-01",
                "endDate": "2025-06-30",
                "overallScore": 4.5,
                "staffingNeeds": [
                    {"role": "member", "count": 1, "durationWeeks": 12}
                ]
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(projectBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(201);

        asAdmin()
                .contentType(ContentType.JSON)
                .queryParam("phaseId", phaseId)
        .when()
                .get("/capacity/strategic")
        .then()
                .statusCode(200)
                .body("allocations", hasSize(1))
                .body("allocations[0].projectName", equalTo("Test Project"))
                .body("allocations[0].roleAllocations[0].role", equalTo("member"))
                .body("allocations[0].roleAllocations[0].assigned", equalTo(1));
    }

    @Test
    void getCapacityAllocation_withInvalidPhaseId_returnsNotFound() {
        asAdmin()
                .contentType(ContentType.JSON)
                .queryParam("phaseId", "00000000-0000-0000-0000-000000000000")
        .when()
                .get("/capacity/strategic")
        .then()
                .statusCode(404);
    }

    @Test
    void getGapAnalysis_withEmptyPhase_returnsNoGaps() {
        asAdmin()
                .contentType(ContentType.JSON)
                .queryParam("phaseId", phaseId)
        .when()
                .get("/capacity/gaps")
        .then()
                .statusCode(200)
                .body("phaseId", equalTo(phaseId))
                .body("gaps", hasSize(0))
                .body("totalGapCount", equalTo(0));
    }

    @Test
    void getGapAnalysis_asViewer_returnsOk() {
        asViewer()
                .contentType(ContentType.JSON)
                .queryParam("phaseId", phaseId)
        .when()
                .get("/capacity/gaps")
        .then()
                .statusCode(200);
    }

    @Test
    void getGapAnalysis_withUnfilledNeeds_returnsGaps() {
        String projectBody = """
            {
                "name": "Understaffed Project",
                "startDate": "2025-03-01",
                "endDate": "2025-08-31",
                "staffingNeeds": [
                    {"role": "Data Scientist", "count": 3, "durationWeeks": 16}
                ]
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(projectBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(201);

        asAdmin()
                .contentType(ContentType.JSON)
                .queryParam("phaseId", phaseId)
        .when()
                .get("/capacity/gaps")
        .then()
                .statusCode(200)
                .body("totalGapCount", equalTo(3))
                .body("gaps", hasSize(1))
                .body("gaps[0].role", equalTo("Data Scientist"))
                .body("gaps[0].required", equalTo(3))
                .body("gaps[0].available", equalTo(0))
                .body("gaps[0].gap", equalTo(3));
    }

    @Test
    void getGapAnalysis_withSufficientStaff_returnsNoGaps() {
        for (int i = 0; i < 3; i++) {
            String staffBody = String.format("""
                {
                    "firstName": "Dev",
                    "lastName": "%d",
                    "role": "member",
                    "seniority": "mid"
                }
                """, i);

            asAdmin()
                    .contentType(ContentType.JSON)
                    .body(staffBody)
            .when()
                    .post("/staff")
            .then()
                    .statusCode(201);
        }

        String projectBody = """
            {
                "name": "Well Staffed Project",
                "startDate": "2025-04-01",
                "endDate": "2025-09-30",
                "staffingNeeds": [
                    {"role": "member", "count": 2, "durationWeeks": 12}
                ]
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(projectBody)
        .when()
                .post("/phases/{phaseId}/projects", phaseId)
        .then()
                .statusCode(201);

        asAdmin()
                .contentType(ContentType.JSON)
                .queryParam("phaseId", phaseId)
        .when()
                .get("/capacity/gaps")
        .then()
                .statusCode(200)
                .body("gaps.findAll { it.role == 'member' }", hasSize(0));
    }

    @Test
    void getGapAnalysis_withInvalidPhaseId_returnsNotFound() {
        asAdmin()
                .contentType(ContentType.JSON)
                .queryParam("phaseId", "00000000-0000-0000-0000-000000000000")
        .when()
                .get("/capacity/gaps")
        .then()
                .statusCode(404);
    }
}
