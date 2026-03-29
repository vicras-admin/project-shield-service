package com.vicras.projectshield.integration;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class SkillApiIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAllSkills_asAdmin_returnsOk() {
        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/skills")
        .then()
                .statusCode(200)
                .body("$", is(notNullValue()));
    }

    @Test
    void getAllSkills_asViewer_returnsOk() {
        asViewer()
                .contentType(ContentType.JSON)
        .when()
                .get("/skills")
        .then()
                .statusCode(200);
    }

    @Test
    void createSkill_withValidData_returnsCreated() {
        String requestBody = """
            {
                "name": "Java"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/skills")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Java"))
                .body("createdAt", notNullValue());
    }

    @Test
    void createSkill_asTeamLead_returnsForbidden() {
        String requestBody = """
            {
                "name": "Unauthorized Skill"
            }
            """;

        asTeamLead()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/skills")
        .then()
                .statusCode(403);
    }

    @Test
    void createSkill_withMissingName_returnsBadRequest() {
        String requestBody = """
            {
                "name": ""
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/skills")
        .then()
                .statusCode(400);
    }

    @Test
    void getSkillById_returnsSkill() {
        String createBody = """
            {
                "name": "Spring Boot"
            }
            """;

        String skillId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/skills")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/skills/{id}", skillId)
        .then()
                .statusCode(200)
                .body("id", equalTo(skillId))
                .body("name", equalTo("Spring Boot"));
    }

    @Test
    void updateSkill_withValidData_returnsUpdated() {
        String createBody = """
            {
                "name": "Original Skill"
            }
            """;

        String skillId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/skills")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        String updateBody = """
            {
                "name": "Updated Skill"
            }
            """;

        asAdmin()
                .contentType(ContentType.JSON)
                .body(updateBody)
        .when()
                .put("/skills/{id}", skillId)
        .then()
                .statusCode(200)
                .body("name", equalTo("Updated Skill"));
    }

    @Test
    void deleteSkill_withValidId_returnsNoContent() {
        String createBody = """
            {
                "name": "Skill to Delete"
            }
            """;

        String skillId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/skills")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .delete("/skills/{id}", skillId)
        .then()
                .statusCode(204);

        asAdmin()
                .contentType(ContentType.JSON)
        .when()
                .get("/skills/{id}", skillId)
        .then()
                .statusCode(404);
    }

    @Test
    void deleteSkill_asMember_returnsForbidden() {
        String createBody = """
            {
                "name": "Protected Skill"
            }
            """;

        String skillId = asAdmin()
                .contentType(ContentType.JSON)
                .body(createBody)
        .when()
                .post("/skills")
        .then()
                .statusCode(201)
                .extract()
                .path("id");

        asMember()
                .contentType(ContentType.JSON)
        .when()
                .delete("/skills/{id}", skillId)
        .then()
                .statusCode(403);
    }
}
