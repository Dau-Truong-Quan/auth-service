package garder.quan.authservice.event;

import java.time.Instant;
import java.util.UUID;

public record AccountEvent(
        String type,
        UUID accountId,
        String username,
        String email,
        Instant occurredAt
) {}
