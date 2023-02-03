package com.UoU._integration.db;

import static com.UoU._helpers.PagingAssertions.assertPagesContainValues;
import static com.UoU.infra.jooq.Tables.SERVICE_ACCOUNT;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.UoU._helpers.TestData;
import com.UoU._integration.BaseAppIntegrationTest;
import com.UoU.core.Fluent;
import com.UoU.core.PageParams;
import com.UoU.core.accounts.ServiceAccountCreateRequest;
import com.UoU.core.auth.AuthMethod;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

public class JooqServiceAccountRepositoryTests extends BaseAppIntegrationTest {

  @Test
  void list_shouldPageAndSortByEmail() {
    // Create 3 service accounts in the order we expect them returned: by email
    val ids = List.of(
        dbHelper.createServiceAccount(orgId, x -> x.email(TestData.emailStartingWith("a"))),
        dbHelper.createServiceAccount(orgId, x -> x.email(TestData.emailStartingWith("b"))),
        dbHelper.createServiceAccount(orgId, x -> x.email(TestData.emailStartingWith("c"))));

    // Get the 3 items: 3 pages with 1 per page.
    val limit = 1;
    val repo = dbHelper.getServiceAccountRepo();
    val page1 = repo.list(orgId, new PageParams(null, limit));
    val page2 = repo.list(orgId, new PageParams(page1.nextCursor(), limit));
    val page3 = repo.list(orgId, new PageParams(page2.nextCursor(), limit));

    assertPagesContainValues(
        x -> x.id(),
        Pair.of(page1, List.of(ids.get(0))),
        Pair.of(page2, List.of(ids.get(1))),
        Pair.of(page3, List.of(ids.get(2))));
  }

  @Test
  void list_shouldPageWithEncryptedEmail() {
    val email = TestData.email();
    dbHelper.createServiceAccount(orgId);
    dbHelper.createServiceAccount(orgId, x -> x.email(email));

    val page = dbHelper.getServiceAccountRepo().list(orgId, new PageParams(null, 1));
    val decodedCursor = new String(
        Base64.getUrlDecoder().decode(page.nextCursor()),
        StandardCharsets.UTF_8);

    assertThat(decodedCursor).doesNotContain(email);
  }

  @Test
  void listExpiredSettings_shouldWork() {
    // Delete any service accounts from previous tests because we need exact list results.
    dbHelper.resetServiceAccounts();

    val authMethods = Set.of(AuthMethod.MS_OAUTH_SA);

    // Create a few service accounts that should not be returned.
    dbHelper.createServiceAccount(orgId, x -> x
        .authMethod(AuthMethod.MS_OAUTH_SA));
    dbHelper.createServiceAccount(orgId, x -> x
        .authMethod(AuthMethod.MS_OAUTH_SA)
        .settingsExpireAt(Instant.now().plusSeconds(300)));
    dbHelper.createServiceAccount(orgId, x -> x
        .authMethod(AuthMethod.GOOGLE_OAUTH)
        .settingsExpireAt(Instant.now().minusSeconds(60)));

    // Create 3 service accounts that are expired and should be returned.
    val ids = Set.of(
        dbHelper.createServiceAccount(orgId, x -> x
            .authMethod(AuthMethod.MS_OAUTH_SA)
            .settingsExpireAt(Instant.now())),
        dbHelper.createServiceAccount(orgId, x -> x
            .authMethod(AuthMethod.MS_OAUTH_SA)
            .settingsExpireAt(Instant.now().minusSeconds(1))),
        dbHelper.createServiceAccount(orgId, x -> x
            .authMethod(AuthMethod.MS_OAUTH_SA)
            .settingsExpireAt(Instant.now().minusSeconds(2))));

    // Get batches with batchSize=2, so batch 1 will have 2 ids, and batch 2 will have 1 id.
    val batchSize = 2;
    val batches = dbHelper.getServiceAccountRepo()
        .listExpiredSettings(authMethods, batchSize)
        .toList();

    assertThat(batches.size()).as("Should return 2 batches").isEqualTo(2);
    assertThat(batches.get(0).size()).as("First batch should have 2 ids").isEqualTo(2);
    assertThat(batches.get(1).size()).as("Second batch should have 1 id").isEqualTo(1);
    assertThat(batches).as("Batch ids should be in set of expected ids").allSatisfy(
        batch -> assertThat(batch).allSatisfy(
            item -> assertThat(item).isIn(ids)));
  }

  @Test
  void create_getAuthInfo_shouldEncryptAndDecryptSettings() {
    val id = TestData.serviceAccountId();
    val settings = Map.<String, Object>of("refresh_token", "test", "other", 1);
    val createRequest = ServiceAccountCreateRequest.builder()
        .id(id)
        .authMethod(AuthMethod.MS_OAUTH_SA)
        .email(TestData.email())
        .orgId(TestData.orgId())
        .settings(settings)
        .build();

    dbHelper.getServiceAccountRepo().create(createRequest);
    val settingsResult = dbHelper.getServiceAccountRepo().getAuthInfo(id).settings();
    val rawSettingsResult = Fluent.of(dbHelper
            .getDsl()
            .select(SERVICE_ACCOUNT.SETTINGS_ENCRYPTED)
            .from(SERVICE_ACCOUNT)
            .where(SERVICE_ACCOUNT.ID.eq(id.value()))
            .fetchSingle()
            .value1())
        .map(x -> new String(x, StandardCharsets.UTF_8))
        .get();

    assertThat(settingsResult)
        .as("decrypted value matches input value we sent")
        .returns("test", x -> x.get("refresh_token"))
        .returns(1, x -> x.get("other"));
    assertThat(rawSettingsResult)
        .as("raw db value does not contain the unencrypted map values")
        .doesNotContain("refresh_token")
        .doesNotContain("test");
  }
}
