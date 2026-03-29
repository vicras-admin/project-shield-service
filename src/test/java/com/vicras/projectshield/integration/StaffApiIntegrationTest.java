package com.vicras.projectshield.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class StaffApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAllStaff_asAdmin_returnsOk() {
        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/staff")
        .then()
                .statusCode(200)
                .body("$", is(notNullValue()));
    }

    @Test
    void getAllStaff_asViewer_returnsForbidden() {
        asViewer()
                .contentType(ContentType.JSON)
        .when()
                .get("/staff")
        .then()
                .statusCode(403);
    }

    @Test
    void createStaff_withValidData_returnsCreated() {
        // Create domains first
        String domainId1 = asAdmin()
                .contentType(ContentType.JSON)
                .body("""
                    {"name": "Claims Processing"}
                    """)
        .when()
                .post("/domains")
        .then()
                .statusCode(201)
                .extract().path("id");

        String domainId2 = asAdmin()
                .contentType(ContentType.JSON)
                .body("""
                    {"name": "Payment & Billing"}
                    """)
        .when()
                .post("/domains")
        .then()
                .statusCode(201)
                .extract().path("id");

        // Create skills first
        String skillId1 = asAdmin()
                .contentType(ContentType.JSON)
                .body("""
                    {"name": "Java"}
                    """)
        .when()
                .post("/skills")
        .then()
                .statusCode(201)
                .extract().path("id");

        String skillId2 = asAdmin()
                .contentType(ContentType.JSON)
                .body("""
                    {"name": "Spring Boot"}
                    """)
        .when()
                .post("/skills")
        .then()
                .statusCode(201)
                .extract().path("id");

        String requestBody = String.format("""
            {
                "firstName": "John",
                "lastName": "Doe",
                "email": "john.doe@example.com",
                "phone": "555-1234",
                "role": "member",
                "seniority": "senior",
                "hoursPerDay": 8,
                "domainIds": ["%s", "%s"],
                "skillIds": ["%s", "%s"]
            }
            """, domainId1, domainId2, skillId1, skillId2);

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/staff")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("firstName", equalTo("John"))
                .body("lastName", equalTo("Doe"))
                .body("role", equalTo("member"))
                .body("seniority", equalTo("senior"))
                .body("hoursPerDay", equalTo(8))
                .body("domains", hasSize(2))
                .body("skills", hasSize(2));
    }

    @Test
    void createStaff_asTeamLead_returnsForbidden() {
        String requestBody = """
            {
                "firstName": "Unauthorized",
                "lastName": "Staff",
                "role": "member",
                "seniority": "mid"
            }
            """;

        asTeamLead()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/staff")
        .then()
                .statusCode(403);
    }

    @Test
    void createStaff_withMinimalData_returnsCreated() {
        String requestBody = """
            {
                "firstName": "Jane",
                "lastName": "Smith",
                "role": "member",
                "seniority": "mid"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/staff")
        .then()
                .statusCode(201)
                .body("firstName", equalTo("Jane"))
                .body("lastName", equalTo("Smith"))
                .body("hoursPerDay", equalTo(8));
    }

    @Test
    void createStaff_withTeam_returnsCreatedWithTeam() {
        String teamBody = """
            {
                "name": "Dev Team",
                "description": "Development team"
            }
            """;

        String teamId = asAdmin()
                .contentType(ContentType.JSON)
                .body(teamBody)
        .when()
                .post("/teams")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        String staffBody = String.format("""
            {
                "firstName": "Team",
                "lastName": "Member",
                "role": "team_lead",
                "seniority": "lead",
                "teamId": "%s"
            }
            """, teamId);

        asAdmin()
                .contentType(ContentType.JSON)
                .body(staffBody)
        .when()
                .post("/staff")
        .then()
                .statusCode(201)
                .body("teamId", equalTo(teamId))
                .body("teamName", equalTo("Dev Team"));
    }

    @Test
    void createStaff_withMissingFirstName_returnsBadRequest() {
        String requestBody = """
            {
                "role": "member",
                "seniority": "mid"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/staff")
        .then()
                .statusCode(400);
    }

    @Test
    void createStaff_withInvalidHoursPerDay_returnsBadRequest() {
        String requestBody = """
            {
                "firstName": "Invalid",
                "lastName": "Hours",
                "role": "member",
                "seniority": "mid",
                "hoursPerDay": 10
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/staff")
        .then()
                .statusCode(400);
    }

    @Test
    void getStaffById_returnsStaff() {
        String createBody = """
            {
                "firstName": "Get",
                "lastName": "Test Staff",
                "role": "member",
                "seniority": "junior"
            }
            """;

        String staffId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/staff")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/staff/{id}", staffId)
        .then()
                .statusCode(200)
                .body("id", equalTo(staffId))
                .body("firstName", equalTo("Get"))
                .body("lastName", equalTo("Test Staff"));
    }

    @Test
    void updateStaff_withValidData_returnsUpdated() {
        String createBody = """
            {
                "firstName": "Original",
                "lastName": "Staff",
                "role": "member",
                "seniority": "junior"
            }
            """;

        String staffId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/staff")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        String updateBody = """
            {
                "firstName": "Promoted",
                "lastName": "Staff",
                "role": "team_lead",
                "seniority": "lead",
                "hoursPerDay": 6
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(updateBody)
        .when()
                .put("/staff/{id}", staffId)
        .then()
                .statusCode(200)
                .body("firstName", equalTo("Promoted"))
                .body("lastName", equalTo("Staff"))
                .body("role", equalTo("team_lead"))
                .body("seniority", equalTo("lead"))
                .body("hoursPerDay", equalTo(6));
    }

    @Test
    void deleteStaff_withValidId_returnsNoContent() {
        String createBody = """
            {
                "firstName": "Staff",
                "lastName": "to Delete",
                "role": "member",
                "seniority": "mid"
            }
            """;

        String staffId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/staff")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .delete("/staff/{id}", staffId)
        .then()
                .statusCode(204);

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/staff/{id}", staffId)
        .then()
                .statusCode(404);
    }

    @Test
    void createStaff_allSeniorities_areValid() {
        String[] seniorities = {"junior", "mid", "senior", "lead"};

        for (String seniority : seniorities) {
            String requestBody = String.format("""
                {
                    "firstName": "Staff",
                    "lastName": "%s",
                    "role": "member",
                    "seniority": "%s"
                }
                """, seniority, seniority);

            asAdmin()
                    .contentType(ContentType.JSON)
                    .body(requestBody)
            .when()
                    .post("/staff")
            .then()
                    .statusCode(201)
                    .body("seniority", equalTo(seniority));
        }
    }
}
