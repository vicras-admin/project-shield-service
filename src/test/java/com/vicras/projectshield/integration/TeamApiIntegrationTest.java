package com.vicras.projectshield.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class TeamApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAllTeams_returnsEmptyList() {
        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/teams")
        .then()
                .statusCode(200)
                .body("$", is(notNullValue()));
    }

    @Test
    void getAllTeams_asViewer_returnsOk() {
        asViewer()
                .contentType(ContentType.JSON)
        .when()
                .get("/teams")
        .then()
                .statusCode(200);
    }

    @Test
    void createTeam_withValidData_returnsCreated() {
        String requestBody = """
            {
                "name": "Platform Team",
                "description": "Core platform development"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/teams")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Platform Team"))
                .body("description", equalTo("Core platform development"));
    }

    @Test
    void createTeam_asViewer_returnsForbidden() {
        String requestBody = """
            {
                "name": "Unauthorized Team",
                "description": "Should fail"
            }
            """;

        asViewer()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/teams")
        .then()
                .statusCode(403);
    }

    @Test
    void createTeam_withMissingName_returnsBadRequest() {
        String requestBody = """
            {
                "description": "Missing name field"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/teams")
        .then()
                .statusCode(400);
    }

    @Test
    void createAndGetTeam_returnsTeamWithId() {
        String requestBody = """
            {
                "name": "API Team",
                "description": "API development"
            }
            """;

        String teamId = asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/teams")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/teams/{id}", teamId)
        .then()
                .statusCode(200)
                .body("id", equalTo(teamId))
                .body("name", equalTo("API Team"));
    }

    @Test
    void updateTeam_withValidData_returnsUpdated() {
        String createBody = """
            {
                "name": "Original Team",
                "description": "Original description"
            }
            """;

        String teamId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/teams")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        String updateBody = """
            {
                "name": "Updated Team",
                "description": "Updated description"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(updateBody)
        .when()
                .put("/teams/{id}", teamId)
        .then()
                .statusCode(200)
                .body("name", equalTo("Updated Team"))
                .body("description", equalTo("Updated description"));
    }

    @Test
    void deleteTeam_withValidId_returnsNoContent() {
        String createBody = """
            {
                "name": "Team to Delete",
                "description": "Will be deleted"
            }
            """;

        String teamId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/teams")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .delete("/teams/{id}", teamId)
        .then()
                .statusCode(204);

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/teams/{id}", teamId)
        .then()
                .statusCode(404);
    }

    @Test
    void getTeam_withInvalidId_returnsNotFound() {
        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/teams/{id}", "00000000-0000-0000-0000-000000000000")
        .then()
                .statusCode(404);
    }
}
