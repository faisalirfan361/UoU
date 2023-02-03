package com.UoU.infra.redis;

import com.UoU.core.Fluent;
import com.UoU.core.calendars.CalendarId;
import com.UoU.core.diagnostics.Config;
import com.UoU.core.diagnostics.DiagnosticRepository;
import com.UoU.core.diagnostics.Results;
import com.UoU.core.diagnostics.RunId;
import com.UoU.core.diagnostics.RunIdInfo;
import com.UoU.core.diagnostics.SaveRequest;
import com.UoU.core.diagnostics.Status;
import com.UoU.core.diagnostics.events.DiagnosticEvent;
import com.UoU.core.exceptions.NotFoundException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RedisDiagnosticRepository implements DiagnosticRepository {
  private final Config config;
  private final RedisTemplate<String, Object> redisTemplate;
  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public RunIdInfo getOrSaveCurrentRun(CalendarId calendarId) {
    val newId = UUID.randomUUID();

    // Note that setIfAbsent() and get() are not atomic because the benefits of a transaction
    // would be minimal for this use case, and transactions complicate things for cluster mode.
    return Fluent
        .of(stringRedisTemplate.boundValueOps(Keys.currentRunId(calendarId)))
        .also(ops -> ops.setIfAbsent(newId.toString(), config.currentRunDuration()))
        .map(ops -> ops.get())
        .map(UUID::fromString)
        .map(x -> new RunIdInfo(new RunId(calendarId, x), x.equals(newId)))
        .ifThenAlso(
            // If new, set status to pending:
            x -> x.isNew(),
            x -> save(SaveRequest.builder().runId(x.runId()).status(Status.PENDING).build()))
        .get();
  }

  @Override
  public Status getStatus(RunId runId) {
    return Optional
        .ofNullable(stringRedisTemplate.opsForHash().get(Keys.runInfo(runId), Properties.STATUS))
        .map(x -> Status.valueOf((String) x))
        .orElseThrow(() -> NotFoundException.ofName("Run"));
  }

  @Override
  public Results getResults(RunId runId) {
    val infoKey = Keys.runInfo(runId);
    val info = stringRedisTemplate.opsForHash().entries(infoKey);
    if (info.isEmpty()) {
      throw NotFoundException.ofName("Run");
    }

    val expireSeconds = stringRedisTemplate.getExpire(infoKey, TimeUnit.SECONDS);
    val events = redisTemplate.opsForList().range(Keys.runEvents(runId), 0, -1)
        .stream()
        .map(x -> (DiagnosticEvent) x)
        .toList();

    val status = Optional
        .ofNullable((String) info.get(Properties.STATUS))
        .map(Status::valueOf)
        .orElse(Status.PENDING);
    val startedAt = Optional
        .ofNullable((String) info.get(Properties.STARTED_AT))
        .map(Instant::parse)
        .orElse(null);
    val finishedAt = Optional
        .ofNullable((String) info.get(Properties.FINISHED_AT))
        .map(Instant::parse)
        .orElse(null);
    val expiresAt = Optional
        .ofNullable(expireSeconds)
        .map(x -> Instant.now().plusSeconds(expireSeconds))
        .orElse(Instant.now()) // something's wrong, expiration not found, just display as now
        .truncatedTo(ChronoUnit.MINUTES); // truncate so expiration is optimistic and stable

    return new Results(runId, status, startedAt, finishedAt, expiresAt, events);
  }

  @Override
  public void save(SaveRequest request) {
    // Collect info properties that are set (non-null).
    val info = Stream.of(
            Pair.of(
                Properties.STATUS,
                Optional.ofNullable(request.status()).map(x -> x.name())),
            Pair.of(
                Properties.STARTED_AT,
                Optional.ofNullable(request.startedAt()).map(x -> x.toString())),
            Pair.of(
                Properties.FINISHED_AT,
                Optional.ofNullable(request.finishedAt()).map(x -> x.toString())))
        .filter(x -> x.getValue().isPresent())
        .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().orElseThrow()));

    if (info.isEmpty() && request.newEvents().isEmpty()) {
      return; // nothing to save
    }

    Fluent
        .of(stringRedisTemplate.boundHashOps(Keys.runInfo(request.runId())))
        .ifThenAlso(!info.isEmpty(), ops -> ops.putAll(info))
        .also(ops -> ops.expire(config.resultsExpiration()));

    Fluent
        .of(redisTemplate.boundListOps(Keys.runEvents(request.runId())))
        .ifThenAlso(
            !request.newEvents().isEmpty(),
            ops -> ops.rightPushAll(request.newEvents().toArray()))
        .also(ops -> ops.expire(config.resultsExpiration()));
  }

  /**
   * Redis key creation helpers.
   */
  private static class Keys {
    // Use {calendarId} hash slot so all keys go to the same redis node in case we use clustering.
    private static final String PREFIX = "diagnostics-calendar-sync-{%s}-"; // %s = calendarId
    private static final String CURRENT_RUN_ID_FORMAT = PREFIX + "current";
    private static final String RUN_INFO_FORMAT = PREFIX + "%s-info"; // %s = runId
    private static final String RUN_EVENTS_FORMAT = PREFIX + "%s-events";  // %s = runId

    public static String currentRunId(CalendarId calendarId) {
      return String.format(CURRENT_RUN_ID_FORMAT, calendarId.value());
    }

    public static String runInfo(RunId runId) {
      return String.format(RUN_INFO_FORMAT, runId.calendarId().value(), runId);
    }

    public static String runEvents(RunId runId) {
      return String.format(RUN_EVENTS_FORMAT, runId.calendarId().value(), runId);
    }
  }

  /**
   * Redis hash property names.
   */
  private static class Properties {
    public static final String STATUS = "status";
    public static final String STARTED_AT = "startedAt";
    public static final String FINISHED_AT = "finishedAt";
  }
}
