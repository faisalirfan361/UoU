package com.UoU.infra.db;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.UoU._helpers.TestData;
import com.UoU.core.events.EventId;
import com.UoU.core.events.EventUpdateRequest;
import com.UoU.infra.db._helpers.Mappers;
import com.UoU.infra.jooq.tables.records.EventRecord;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import org.jooq.Batch;
import org.jooq.DSLContext;
import org.jooq.TransactionalRunnable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JooqEventRepositoryTests {
  private DSLContext dslMock;
  private JooqEventRepository repo;

  @BeforeEach
  void setUp() {
    dslMock = mock(DSLContext.class);
    when(dslMock.executeUpdate(any(EventRecord.class))).thenReturn(1);

    repo = new JooqEventRepository(dslMock, Mappers.EVENT_MAPPER, Mappers.PARTICIPANT_MAPPER);
  }

  @Test
  void update_shouldSkipIfNoChanges() {
    val request = updateBuilder().build();
    repo.update(request);
    verifyNoInteractions(dslMock);
  }

  @Test
  void update_shouldExecuteIfChanges() {
    val request = updateBuilder().title("test").build();
    repo.update(request);
    verify(dslMock).executeUpdate(any(EventRecord.class));
  }

  @Test
  void batchCreate_shouldSkipEmptyList() {
    repo.batchCreate(List.of());
    verifyNoInteractions(dslMock);
  }

  @Test
  void batchUpdate_shouldSkipEmptyList() {
    repo.batchUpdate(List.of());
    verifyNoInteractions(dslMock);
  }

  @Test
  void batchUpdate_shouldSkipIfNoChanges() {
    val requests = Stream
        .generate(() -> updateBuilder().build())
        .limit(2)
        .toList();
    repo.batchUpdate(requests);
    verifyNoInteractions(dslMock);
  }

  @Test
  @SuppressWarnings("unchecked")
  void batchUpdate_shouldExecuteWithNoTransactionIfOnlyEventChanges() {
    when(dslMock.batchUpdate(any(Collection.class))).thenReturn(mock(Batch.class));

    val requests = Stream
        .generate(() -> updateBuilder().description("test").build())
        .limit(2)
        .toList();
    repo.batchUpdate(requests);
    verify(dslMock).batchUpdate(any(Collection.class));
  }

  @Test
  void batchUpdate_shouldExecuteWithTransactionIfParticipantChanges() {
    val requests = Stream
        .generate(() -> updateBuilder()
            .participants(List.of(TestData.participantRequest()))
            .build())
        .limit(2)
        .toList();
    repo.batchUpdate(requests);
    verify(dslMock).transaction(any(TransactionalRunnable.class));
  }

  @Test
  void batchDelete_shouldSkipEmptyList() {
    repo.batchDelete(List.of());
    verifyNoInteractions(dslMock);
  }

  @Test
  void batchDelete_shouldExecute() {
    repo.batchDelete(List.of(EventId.create()));
    verify(dslMock).transaction(any(TransactionalRunnable.class));
  }

  private static EventUpdateRequest.Builder updateBuilder() {
    return EventUpdateRequest.builder().id(EventId.create());
  }
}
