package com.UoU._integration.core.nylas.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nylas.RequestFailedException;
import com.UoU._helpers.TestData;
import com.UoU.core.nylas.ExternalEtag;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

public class DeleteEventFromNylasTaskTests extends BaseNylasTaskTest {

  @SneakyThrows
  @Test
  void deleteEventFromNylas_shouldWork() {
    val accountId = dbHelper.createAccount(orgId);
    val externalId = TestData.eventExternalId();

    // Create etag to make sure it gets deleted too:
    redisHelper.getExternalEtagRepo().save(externalId, new ExternalEtag("test"));

    getNylasTaskRunnerSpy().deleteEventFromNylas(accountId, externalId);

    verify(getAccountClientMock().events()).delete(externalId.value(), true);
    assertThat(redisHelper.getExternalEtagRepo().get(externalId)).isEmpty();
    verifyEventPublisherMock()
        .noEventChangedOfAnyType();
  }

  @Test
  @SneakyThrows
  void deleteEventFromNylas_shouldWorkOnNylas404() {
    val accountId = dbHelper.createAccount(orgId);
    val externalId = TestData.eventExternalId();

    when(getAccountClientMock().events().delete(eq(externalId.value()), any(Boolean.class)))
        .thenThrow(new RequestFailedException(404, "message", "type"));

    assertThatCode(() -> getNylasTaskRunnerSpy().deleteEventFromNylas(accountId, externalId))
        .doesNotThrowAnyException();
  }
}
