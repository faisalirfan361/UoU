package com.UoU.infra.db;

import static com.UoU.infra.jooq.tables.ConferencingUser.CONFERENCING_USER;

import com.UoU.core.OrgId;
import com.UoU.core.SecretString;
import com.UoU.core.conferencing.ConferencingAuthInfo;
import com.UoU.core.conferencing.ConferencingUser;
import com.UoU.core.conferencing.ConferencingUserCreateRequest;
import com.UoU.core.conferencing.ConferencingUserId;
import com.UoU.core.conferencing.ConferencingUserRepository;
import com.UoU.core.conferencing.ConferencingUserUpdateRequest;
import com.UoU.infra.db.mapping.JooqConferencingUserMapper;
import com.UoU.infra.encryption.Encryptor;
import com.UoU.infra.jooq.enums.ConferencingAuthMethod;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.val;
import org.jooq.DSLContext;
import org.jooq.Record8;
import org.jooq.SelectJoinStep;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class JooqConferencingUserRepository implements ConferencingUserRepository {

  private final DSLContext dsl;
  private final JooqConferencingUserMapper mapper;
  private final Encryptor encryptor;
  private final ExceptionHelper exceptionHelper = new ExceptionHelper(ConferencingUser.class);

  @Override
  public ConferencingUser get(ConferencingUserId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> selectForModel()
        .where(CONFERENCING_USER.ID.eq(id.value()))
        .fetchSingle(mapper::toModel));
  }

  @Override
  public Optional<ConferencingUser> tryGet(OrgId orgId, String email) {
    return selectForModel()
        .where(CONFERENCING_USER.ORG_ID.eq(orgId.value()))
        .and(CONFERENCING_USER.EMAIL.eq(email))
        .fetchOptional(mapper::toModel);
  }

  @Override
  public ConferencingAuthInfo getAuthInfo(ConferencingUserId id) {
    return exceptionHelper.throwNotFoundIfNoData(() -> dsl
            .select(
                CONFERENCING_USER.NAME,
                CONFERENCING_USER.REFRESH_TOKEN_ENCRYPTED,
                CONFERENCING_USER.ACCESS_TOKEN_ENCRYPTED,
                CONFERENCING_USER.EXPIRE_AT)
            .from(CONFERENCING_USER)
            .where(CONFERENCING_USER.ID.eq(id.value()))
            .fetchSingle())
        .map(x -> new ConferencingAuthInfo(
            x.get(CONFERENCING_USER.NAME),
            new SecretString(encryptor.decryptToString(
                x.getValue(CONFERENCING_USER.REFRESH_TOKEN_ENCRYPTED))),
            new SecretString(encryptor.decryptToString(
                x.getValue(CONFERENCING_USER.ACCESS_TOKEN_ENCRYPTED))),
            x.get(CONFERENCING_USER.EXPIRE_AT).toInstant()));
  }

  /**
   * Selects fields in the order needed for the ConferencingUser ctor (via the mapper).
   *
   * <p>Notably, this excludes tokens and stuff we don't need for the model.
   */
  private SelectJoinStep<Record8<
      UUID, String, String, String, ConferencingAuthMethod, OffsetDateTime, OffsetDateTime,
      OffsetDateTime>>
      selectForModel() {
    return dsl
        .select(
            CONFERENCING_USER.ID,
            CONFERENCING_USER.ORG_ID,
            CONFERENCING_USER.EMAIL,
            CONFERENCING_USER.NAME,
            CONFERENCING_USER.AUTH_METHOD,
            CONFERENCING_USER.EXPIRE_AT,
            CONFERENCING_USER.CREATED_AT,
            CONFERENCING_USER.UPDATED_AT)
        .from(CONFERENCING_USER);
  }

  @Override
  public void create(ConferencingUserCreateRequest request) {
    val record = mapper.toRecord(
        request,
        encryptor.encrypt(request.refreshToken().value()),
        encryptor.encrypt(request.accessToken().value()));

    dsl.executeInsert(record);
  }

  @Override
  public void update(ConferencingUserUpdateRequest request) {
    val record = mapper.toRecord(
        request,
        encryptor.encrypt(request.refreshToken().value()),
        encryptor.encrypt(request.accessToken().value()));

    exceptionHelper.throwNotFoundIfNoRowsAffected(
        dsl.executeUpdate(record));
  }
}
