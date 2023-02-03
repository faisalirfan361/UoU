package com.UoU.core.conferencing.teams;

import com.microsoft.graph.http.IHttpRequest;
import com.UoU.core.conferencing.ConferencingUserId;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import okhttp3.Headers;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Package-private helper for working with graph-related HTTP headers.
 */
class HttpHeaderManager {
  private static final String HEADER_CONFERENCING_USER_ID = "CalendarsApi-Auth-Conf-User-Id";

  public String getRequireAuthHeaderName() {
    return HEADER_CONFERENCING_USER_ID;
  }

  public Pair<String, String> createRequireAuthHeader(ConferencingUserId conferencingUserId) {
    return Pair.of(HEADER_CONFERENCING_USER_ID, conferencingUserId.value().toString());
  }

  public void addRequireAuthHeader(IHttpRequest request, ConferencingUserId conferencingUserId) {
    val header = createRequireAuthHeader(conferencingUserId);
    request.addHeader(header.getKey(), header.getValue());
  }

  public Optional<ConferencingUserId> parseRequireAuthHeader(Headers headers) {
    return Optional
        .ofNullable(headers.get(HEADER_CONFERENCING_USER_ID))
        .map(x -> new ConferencingUserId(UUID.fromString(x)));
  }
}
