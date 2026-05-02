package garder.quan.authservice.event;

import garder.quan.authservice.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AccountEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AccountEventConsumer.class);

    private final RefreshTokenService refreshTokenService;

    public AccountEventConsumer(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @KafkaListener(topics = "account-events", groupId = "auth-service")
    public void handle(AccountEvent event) {
        if ("ACCOUNT_DELETED".equals(event.type())) {
            log.info("Account deleted — purging refresh tokens for accountId={}", event.accountId());
            refreshTokenService.deleteByAccountId(event.accountId());
        }
    }
}
