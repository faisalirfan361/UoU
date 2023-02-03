package com.UoU.infra.db;

import static com.UoU.infra.jooq.Tables.ACCOUNT;
import static com.UoU.infra.jooq.Tables.ACCOUNT_ERROR;
import static com.UoU.infra.jooq.Tables.CALENDAR;
import static com.UoU.infra.jooq.Tables.EVENT;
import static com.UoU.infra.jooq.Tables.PARTICIPANT;

import com.UoU.core.DataConfig;
import com.UoU.core.Fluent;
import com.UoU.core.OrgId;
import com.UoU.core.PageParams;
import com.UoU.core.PagedItems;
import com.UoU.core.SecretString;
import com.UoU.core.accounts.Account;
import com.UoU.core.accounts.AccountAccessInfo;
import com.UoU.core.accounts.AccountCreateRequest;
import com.UoU.core.accounts.AccountError;
import com.UoU.core.accounts.AccountId;
import com.UoU.core.accounts.AccountRepository;
import com.UoU.core.accounts.AccountUpdateRequest;
import com.UoU.core.accounts.ServiceAccountId;
import com.UoU.core.accounts.SyncState;
import com.UoU.infra.db.mapping.JooqAccountMapper;
import com.UoU.infra.encryption.Encryptor;
import com.UoU.infra.jooq.enums.AuthMethod;
import com.UoU.infra.jooq.enums.NylasAccountSyncState;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.Record9;
import org.jooq.SelectJoinStep;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JooqAccountRepository implements AccountRepository {
  private final DSLContext dsl;
  private final JooqAccountMapper mapper;
  private final Encryptor encryptor;
  private final ExceptionHelper exceptionHelper = new ExceptionHelper(Account.class);

  @Override
  public PagedItems<Account> list(OrgId orgId, PageParams page) {
    // Cursor paging fields: EMAIL
    // Use encryption because email is sensitive and will be passed around and potentially logged.
    val cursor = Cursor.decoder(encryptor::decryptToString).decodeOneToString(page.cursor());

    val records = Fluent.of(selectForModel()
            .where(Conditions.orgMatches(ACCOUNT, orgId)))
        .ifThenAlso(cursor, (query, cursorEmail) -> query
            .and(ACCOUNT.EMAIL.greaterThan(cursorEmail)))
        .get()
        .orderBy(ACCOUNT.EMAIL)
        .limit(Math.max(2, page.limit() + 1)) // fetch one extra so we know if there's a next page
        .fetch();

    val nextCursor = Optional
        .of(records)
        .filter(x -> x.size() > page.limit() && x.size() >= 2)
        .map(x -> x.get(x.size() - 2)) // last in page, accounting for one extra
        .map(x -> new Cursor(x.getValue(ACCOUNT.EMAIL)).encode(encryptor::encrypt));
    nextCursor.ifPresent(x -> records.remove(records.size() - 1)); // remove one extra

    return new PagedItems<>(
        records.map(mapper::toModel),
        nextCursor.orElse(null));
  }

  @Override
  public Stream<Account> listByServiceAccount(ServiceAccountId serviceAccountId) {
    return selectForModel()
        .where(ACCOUNT.SERVICE_ACCOUNT_ID.eq(serviceAccountId.value()))
        .orderBy(ACCOUNT.EMAIL)
        .fetch()
        .stream()
        .map(mapper::toModel);
  }

  @Override
  public PagedItems<Account> listByServiceAccount(
      OrgId orgId, ServiceAccountId serviceAccountId, PageParams page) {
    // Cursor paging fields: CREATED_AT and EMAIL
    // Using encryption because email is sensitive and will be passed around and potentially logged.
    val cursor = Cursor.decoder(encryptor::decryptToString).decodeTwoAndMap(
        page.cursor(),
        (createdAt, email) -> Pair.of(OffsetDateTime.parse(createdAt), email)
    );

    val records = Fluent
        .of(
            selectForModel()
                .where(Conditions.orgMatches(ACCOUNT, orgId))
                .and(ACCOUNT.SERVICE_ACCOUNT_ID.eq(serviceAccountId.value()))
        )
        .ifThenAlso(cursor, (query, cursorValue) -> query
            .and(ACCOUNT.CREATED_AT.gt(cursorValue.getLeft())
                .or(ACCOUNT.CREATED_AT.eq(cursorValue.getLeft())
                    .and(ACCOUNT.EMAIL.greaterThan(cursorValue.getRight())))))
        .get()
        .orderBy(ACCOUNT.CREATED_AT, ACCOUNT.EMAIL)
        .limit(Math.max(2, page.limit() + 1)) // fetch one extra so we know if there's a next page
        .fetch();

    val nextCursor = Optional
        .of(records)
        .filter(x -> x.size() > page.limit() && x.size() >= 2)
        .map(x -> x.get(x.size() - 2)) // last in page, accounting for one extra
        .map(x ->
            new Cursor(x.getValue(ACCOUNT.CREATED_AT), x.getValue(ACCOUNT.EMAIL))
                .encode(encryptor::encrypt));
    nextCursor.ifPresent(x -> records.remove(records.size() - 1)); // remove one extra

    return new PagedItems<>(
        records.map(mapper::toModel),
        nextCursor.orElse(null));
  }

  @Override
  public Stream<AccountError> listErrors(AccountId id, boolean includeDetails) {
    if (includeDetails) {
      return dsl
          .selectFrom(ACCOUNT_ERROR)
          .where(ACCOUNT_ERROR.ACCOUNT_ID.eq(id.value()))
          .orderBy(ACCOUNT_ERROR.CREATED_AT.desc())
          .fetch()
          .stream()
          .map(mapper::toModel);
    }

    return dsl
        .select(
            ACCOUNT_ERROR.ID,
            ACCOUNT_ERROR.ACCOUNT_ID,
            ACCOUNT_ERROR.CREATED_AT,
            ACCOUNT_ERROR.TYPE,
            ACCOUNT_ERROR.MESSAGE)
        .from(ACCOUNT_ERROR)
        .where(ACCOUNT_ERROR.ACCOUNT_ID.eq(id.value()))
        .orderBy(ACCOUNT_ERROR.CREATED_AT.desc())
        .fetch()
        .stream()
        .map(mapper::toModel);
  }

  @Override
  public Account get(AccountId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> mapper.toModel(
        selectForModel()
            .where(ACCOUNT.ID.eq(id.value()))
            .fetchSingle()));
  }

  @Override
  public Account get(String email) {
    return tryGet(email).orElseThrow(exceptionHelper::notFound);
  }

  @Override
  public Optional<Account> tryGet(String email) {
    return Optional.ofNullable(
            selectForModel()
                .where(ACCOUNT.EMAIL.eq(email))
                .fetchOne())
        .map(mapper::toModel);
  }

  @Override
  public AccountId getId(String email) {
    return exceptionHelper.throwNotFoundIfNoData(() -> new AccountId(dsl
        .select(ACCOUNT.ID)
        .from(ACCOUNT)
        .where(ACCOUNT.EMAIL.eq(email))
        .fetchSingle()
        .value1()));
  }

  @Override
  public SecretString getAccessToken(AccountId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> Fluent.of(dsl
            .select(ACCOUNT.ACCESS_TOKEN_ENCRYPTED)
            .from(ACCOUNT)
            .where(ACCOUNT.ID.eq(id.value()))
            .fetchSingle()
            .value1()))
        .map(x -> new SecretString(encryptor.decryptToString(x)))
        .get();
  }

  @Override
  public AccountAccessInfo getAccessInfo(AccountId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> Fluent.of(dsl
            .select(ACCOUNT.ORG_ID)
            .from(ACCOUNT)
            .where(ACCOUNT.ID.eq(id.value()))
            .fetchSingle()))
        .map(x -> new AccountAccessInfo(new OrgId(x.value1())))
        .get();
  }

  @Override
  public void create(AccountCreateRequest request) {
    val record = mapper.toRecord(request, encryptor.encrypt(request.accessToken().value()));
    dsl.executeInsert(record);
  }

  @Override
  public void createError(AccountError accountError) {
    val record = mapper.toRecord(accountError);
    val accountId = accountError.accountId().value();

    dsl.transaction(config -> {
      val txDsl = config.dsl();

      // Delete all but max-1 rows so that when new row is inserted we have <= max:
      txDsl
          .deleteFrom(ACCOUNT_ERROR)
          .where(ACCOUNT_ERROR.ACCOUNT_ID.eq(accountId))
          .and(ACCOUNT_ERROR.ID.notIn(txDsl
              .select(ACCOUNT_ERROR.ID)
              .from(ACCOUNT_ERROR)
              .where(ACCOUNT_ERROR.ACCOUNT_ID.eq(accountId))
              .orderBy(ACCOUNT_ERROR.CREATED_AT.desc())
              .limit(DataConfig.Accounts.MAX_ERRORS_PER_ACCOUNT - 1)))
          .execute();

      txDsl.executeInsert(record);
    });
  }

  public void update(AccountUpdateRequest request) {
    val record = mapper.toRecord(request, encryptor.encrypt(request.accessToken().value()));
    dsl.executeUpdate(record);
  }

  public void updateAccessToken(AccountId id, SecretString accessToken) {
    exceptionHelper.throwNotFoundIfNoRowsAffected(dsl
        .update(ACCOUNT)
        .set(ACCOUNT.ACCESS_TOKEN_ENCRYPTED, encryptor.encrypt(accessToken.value()))
        .set(ACCOUNT.UPDATED_AT, OffsetDateTime.now())
        .set(ACCOUNT.LINKED_AT, OffsetDateTime.now())
        .where(ACCOUNT.ID.eq(id.value()))
        .execute()
    );
  }

  public void updateSyncState(AccountId id, SyncState syncState) {
    exceptionHelper.throwNotFoundIfNoRowsAffected(dsl
        .update(ACCOUNT)
        .set(ACCOUNT.NYLAS_SYNC_STATE, mapper.toRecordEnum(syncState))
        .set(ACCOUNT.UPDATED_AT, OffsetDateTime.now())
        .where(ACCOUNT.ID.eq(id.value()))
        .execute());
  }

  @Override
  public void delete(AccountId id) {
    // DO-MAYBE: We maybe want to plug spring-tx into the jooq transaction provider rather than
    // using jooq's default.
    // See: https://www.jooq.org/doc/latest/manual/sql-execution/transaction-management/

    dsl.transaction(config -> {
      val txDsl = config.dsl();

      txDsl
          .deleteFrom(PARTICIPANT)
          .using(EVENT.join(CALENDAR).on(EVENT.CALENDAR_ID.eq(CALENDAR.ID)))
          .where(CALENDAR.ACCOUNT_ID.eq(id.value()))
          .execute();

      txDsl
          .deleteFrom(EVENT)
          .using(CALENDAR)
          .where(CALENDAR.ID.eq(EVENT.CALENDAR_ID))
          .and(CALENDAR.ACCOUNT_ID.eq(id.value()))
          .execute();

      txDsl
          .deleteFrom(CALENDAR)
          .where(CALENDAR.ACCOUNT_ID.eq(id.value()))
          .execute();

      txDsl.deleteFrom(ACCOUNT_ERROR)
          .where(ACCOUNT_ERROR.ACCOUNT_ID.eq(id.value()))
          .execute();

      exceptionHelper.throwNotFoundIfNoRowsAffected(txDsl
          .deleteFrom(ACCOUNT)
          .where(ACCOUNT.ID.eq(id.value()))
          .execute());
    });
  }

  @Override
  public void deleteErrors(AccountId id, AccountError.Type type) {
    dsl
        .deleteFrom(ACCOUNT_ERROR)
        .where(ACCOUNT_ERROR.ACCOUNT_ID.eq(id.value()))
        .and(ACCOUNT_ERROR.TYPE.eq(mapper.toRecordEnum(type)));
  }

  /**
   * Selects fields in the order needed for the Account ctor (via the AccountMapper).
   *
   * <p>Notably, this excludes the access token and other stuff we don't need for the Account model.
   */
  private SelectJoinStep<
      Record9<String, UUID, String, String, String, NylasAccountSyncState,
          AuthMethod, OffsetDateTime, OffsetDateTime>>
      selectForModel() {
    return dsl
        .select(
            ACCOUNT.ID,
            ACCOUNT.SERVICE_ACCOUNT_ID,
            ACCOUNT.ORG_ID,
            ACCOUNT.EMAIL,
            ACCOUNT.NAME,
            ACCOUNT.NYLAS_SYNC_STATE,
            ACCOUNT.AUTH_METHOD,
            ACCOUNT.CREATED_AT,
            ACCOUNT.UPDATED_AT)
        .from(ACCOUNT);
  }
}
