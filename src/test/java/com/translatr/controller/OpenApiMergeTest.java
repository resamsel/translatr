package com.translatr.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * Guards the merged /api/openapi document against two regression classes discovered
 * migrating LocaleResource: (1) excluding a class from mp.openapi.scan.exclude.classes
 * while it still holds un-migrated hand-written methods makes those methods vanish from
 * this document entirely, and (2) forgetting to exclude a fully-migrated resource's class
 * lets smallrye's annotation scan silently overwrite its static contract descriptions with
 * generic scanned defaults. Both failure modes are silent — no compile error, no runtime
 * error, just a wrong or incomplete document — so this is the only thing that would catch
 * either regression short of a manual /api/openapi inspection.
 */
@QuarkusTest
class OpenApiMergeTest {

    @Test
    void mergedDocument_includesEveryMigratedResourcesPaths_andTheHandWrittenTransferEndpoints() {
        given()
            .when().get("/api/openapi?format=json")
            .then()
            .statusCode(200)
            .body("paths", org.hamcrest.Matchers.hasKey("/api/oidc-providers"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/accesstokens"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/projects"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/messages"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/project/{projectId}/locales"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/locale/{id}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/{username}/{projectName}/locales/{localeName}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/locale"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/locale/{localeId}/import/{fileType}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/locale/{localeId}/export/{fileType}"))
            .body("paths['/api/locale/{id}'].get.responses.200.description", is("The locale."))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/project/{projectId}/keys"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/key/{id}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/{username}/{projectName}/keys/{keyName}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/key"))
            .body("paths['/api/key/{id}'].get.responses.200.description", is("The key."))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/project/{projectId}/members"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/members/{projectId}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/member/{id}"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/member"))
            .body("paths['/api/member/{id}'].get.responses.200.description", is("The member."))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/featureflags/resolved"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/featureflags/global"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/featureflag/global"))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/featureflag/global/{id}"))
            .body("paths['/api/featureflags/global'].get.responses.200.description", is("All global feature flag overrides."))
            .body("paths['/api/member/{id}'].get.responses.200.description", is("The member."))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/health"))
            .body("paths['/api/health'].get.responses.200.description", is("The service is healthy."))
            .body("paths['/api/member/{id}'].get.responses.200.description", is("The member."))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/statistics"))
            .body("paths['/api/statistics'].get.responses.200.description", is("Aggregate counts across the whole instance."))
            .body("paths['/api/member/{id}'].get.responses.200.description", is("The member."))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/authclients"))
            .body("paths['/api/authclients'].get.responses.200.description", is("Auth providers a visitor can actually use."))
            .body("paths['/api/member/{id}'].get.responses.200.description", is("The member."))
            .body("paths", org.hamcrest.Matchers.hasKey("/api/notifications"));
    }
}
