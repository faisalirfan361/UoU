package com.UoU.infra.db;

import static com.UoU.infra.jooq.Tables.ACCOUNT;
import static com.UoU.infra.jooq.Tables.SERVICE_ACCOUNT;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import com.UoU.core.PageParams;
import com.UoU.core.PagedItems;
import com.UoU.core.accounts.ServiceAccount;
import com.UoU.core.accounts.ServiceAccountAccessInfo;
import com.UoU.core.accounts.ServiceAccountAuthInfo;
import com.UoU.core.accounts.ServiceAccountCreateRequest;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.accounts.ServiceAccountRepository;
import com.UoU.core.accounts.ServiceAccountUpdateRequest;
import com.UoU.infra.db.mapping.JooqServiceAccountMapper;
import com.UoU.infra.encryption.Encryptor;
import com.UoU.infra.jooq.enums.AuthMethod;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record6;
import org.jooq.SelectJoinStep;
import org.jooq.SelectSeekStep1;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JooqServiceAccountRepository implements ServiceAccountRepository {
  private final DSLContext dsl;
  private final Encryptor encryptor;
  private final JooqServiceAccountMapper mapper;
  private final ObjectMapper objectMapper;
  private final ExceptionHelper exceptionHelper = new ExceptionHelper(ServiceAccount.class);

  @Override
  public PagedItems<ServiceAccount> list(OrgId orgId, PageParams page) {
    // Cursor paging fields: EMAIL
    // Use encryption because email is sensitive and will be passed around and potentially logged.
    val cursor = Cursor.decoder(encryptor::decryptToString).decodeOneToString(page.cursor());

    val records = Fluent.of(selectForModel()
            .where(Conditions.orgMatches(SERVICE_ACCOUNT, orgId)))
        .ifThenAlso(cursor, (query, cursorEmail) -> query
            .and(SERVICE_ACCOUNT.EMAIL.greaterThan(cursorEmail)))
        .get()
        .orderBy(SERVICE_ACCOUNT.EMAIL)
        .limit(Math.max(2, page.limit() + 1)) // fetch one extra so we know if there's a next page
        .fetch();

    val nextCursor = Optional
        .of(records)
        .filter(x -> x.size() > page.limit() && x.size() >= 2)
        .map(x -> x.get(x.size() - 2)) // last in page, accounting for one extra
        .map(x -> new Cursor(x.getValue(SERVICE_ACCOUNT.EMAIL)).encode(encryptor::encrypt));
    nextCursor.ifPresent(x -> records.remove(records.size() - 1)); // remove one extra

    return new PagedItems<>(
        records.map(mapper::toModel),
        nextCursor.orElse(null));
  }

  /**
   * Returns lazy-executed batches of service account ids with expired settings.
   */
  @Override
  public Stream<List<ServiceAccountId>> listExpiredSettings(
      Set<com.UoU.core.auth.AuthMethod> authMethods, int batchSize) {

    if (batchSize <= 0) {
      throw new IllegalArgumentException("Batch size must be greater than 0.");
    }

    val dbAuthMethods = authMethods.stream().map(mapper::mapAuthMethod).collect(Collectors.toSet());

    final Supplier<SelectSeekStep1<Record1<UUID>, UUID>> query = () -> dsl
        .select(SERVICE_ACCOUNT.ID)
        .from(SERVICE_ACCOUNT)
        .where(SERVICE_ACCOUNT.AUTH_METHOD.in(dbAuthMethods))
        .and(SERVICE_ACCOUNT.SETTINGS_EXPIRE_AT.lessOrEqual(DSL.currentOffsetDateTime()))
        .orderBy(SERVICE_ACCOUNT.ID);

    // Return a lazy stream of batches by executing the query for each next batch on iteration.
    // This continues while the returned rows >= batch size, which means there could be more.
    // Each batch query is created by using the last id from the prev batch as a cursor.
    return Stream
        .iterate(
            query.get().limit(batchSize).fetch(), // seed
            batch -> batch != null && batch.isNotEmpty(), // has next?
            batch -> batch.size() < batchSize ? null : query.get() // create next
                .seekAfter(batch.get(batch.size() - 1).value1())
                .limit(batchSize)
                .fetch())
        .filter(batch -> batch != null && batch.isNotEmpty())
        .map(batch -> batch.map(record -> new ServiceAccountId(record.value1())));
  }

  @Override
  public ServiceAccountAccessInfo getAccessInfo(ServiceAccountId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> Fluent.of(dsl
            .select(SERVICE_ACCOUNT.ORG_ID)
            .from(SERVICE_ACCOUNT)
            .where(SERVICE_ACCOUNT.ID.eq(id.value()))
            .fetchSingle()))
        .map(x -> new ServiceAccountAccessInfo(new OrgId(x.value1())))
        .get();
  }

  @Override
  public ServiceAccount get(ServiceAccountId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> mapper.toModel(
        selectForModel()
            .where(SERVICE_ACCOUNT.ID.eq(id.value()))
            .fetchSingle()));
  }

  @Override
  public Optional<ServiceAccount> tryGet(String email) {
    return Optional.ofNullable(selectForModel()
            .where(SERVICE_ACCOUNT.EMAIL.eq(email))
            .fetchOne())
        .map(mapper::toModel);
  }

  @Override
  public ServiceAccountAuthInfo getAuthInfo(ServiceAccountId id) {
    val record = exceptionHelper.throwNotFoundIfNoData(() -> dsl
        .select(
            SERVICE_ACCOUNT.ORG_ID,
            SERVICE_ACCOUNT.AUTH_METHOD,
            SERVICE_ACCOUNT.SETTINGS_ENCRYPTED)
        .from(SERVICE_ACCOUNT)
        .where(SERVICE_ACCOUNT.ID.eq(id.value()))
        .fetchSingle());

    byte[] jsonBytes;
    try {
      jsonBytes = encryptor.decrypt(record.getValue(SERVICE_ACCOUNT.SETTINGS_ENCRYPTED));
    } catch (Exception ex) {
      throw new DataAccessException("Service account settings could not be decrypted", ex);
    }

    Map<String, Object> settings;
    try {
      settings = objectMapper.readValue(jsonBytes, new TypeReference<HashMap<String, Object>>() {});
    } catch (IOException ex) {
      throw new DataAccessException("Service account settings JSON could not parsed", ex);
    }

    return new ServiceAccountAuthInfo(
        id,
        new OrgId(record.getValue(SERVICE_ACCOUNT.ORG_ID)),
        mapper.mapAuthMethod(record.getValue(SERVICE_ACCOUNT.AUTH_METHOD)),
        settings);
  }

  @Override
  public boolean hasAccounts(ServiceAccountId id) {
    return dsl.fetchExists(
        dsl.selectOne()
            .from(ACCOUNT)
            .where(ACCOUNT.SERVICE_ACCOUNT_ID.eq(id.value())));
  }

  @Override
  @SneakyThrows
  public void create(ServiceAccountCreateRequest request) {
    val jsonBytes = objectMapper.writeValueAsBytes(request.settings());
    val encryptedJsonBytes = encryptor.encrypt(jsonBytes);
    val record = mapper.toRecord(request).setSettingsEncrypted(encryptedJsonBytes);
    dsl.executeInsert(record);
  }

  @Override
  @SneakyThrows
  public void update(ServiceAccountUpdateRequest request) {
    val jsonBytes = objectMapper.writeValueAsBytes(request.settings());
    val record = mapper.toRecord(request).setSettingsEncrypted(encryptor.encrypt(jsonBytes));

    exceptionHelper.throwNotFoundIfNoRowsAffected(dsl
        .executeUpdate(record));
  }

  @Override
  public void delete(ServiceAccountId id) {
    exceptionHelper.throwNotFoundIfNoRowsAffected(dsl
        .deleteFrom(SERVICE_ACCOUNT)
        .where(SERVICE_ACCOUNT.ID.eq(id.value()))
        .execute());
  }

  /**
   * Selects fields in the order needed for the ServiceAccount ctor (via the ServiceAccountMapper).
   *
   * <p>Notably, this excludes the settings parameter and others that are unnecessary for the
   * Service Account model.
   */
  private SelectJoinStep<
      Record6<UUID, String, String, AuthMethod, OffsetDateTime, OffsetDateTime>>
      selectForModel() {
    return dsl
        .select(
            SERVICE_ACCOUNT.ID,
            SERVICE_ACCOUNT.ORG_ID,
            SERVICE_ACCOUNT.EMAIL,
            SERVICE_ACCOUNT.AUTH_METHOD,
            SERVICE_ACCOUNT.CREATED_AT,
            SERVICE_ACCOUNT.UPDATED_AT)
        .from(SERVICE_ACCOUNT);
  }
}
