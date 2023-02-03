package com.UoU.core.nylas;

import com.UoU.core.events.EventExternalId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ExternalEtagRepository {
  Optional<ExternalEtag> get(EventExternalId externalId);

  Map<EventExternalId, ExternalEtag> get(Set<EventExternalId> externalIds);

  void save(EventExternalId externalId, ExternalEtag etag);

  void save(Map<EventExternalId, ExternalEtag> etags);

  void tryDelete(EventExternalId externalId);

  void tryDelete(Set<EventExternalId> externalIds);
}
