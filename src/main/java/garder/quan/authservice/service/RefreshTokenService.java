package garder.quan.authservice.service;

import garder.quan.authservice.entity.RefreshToken;
import garder.quan.authservice.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration; // milliseconds

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    /** Creates and persists a new refresh token for the given account. */
    @Transactional
    public RefreshToken create(UUID accountId) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setAccountId(accountId);
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        return repository.save(rt);
    }

    /**
     * Validates the token, deletes it (rotation), and returns a fresh one.
     * Throws 401 if the token is unknown or expired.
     */
    @Transactional
    public RefreshToken validateAndRotate(String tokenValue) {
        RefreshToken existing = repository.findByToken(tokenValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            repository.delete(existing);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token has expired — please log in again");
        }

        UUID accountId = existing.getAccountId();
        repository.delete(existing);   // rotate: old token is gone
        return create(accountId);
    }

    /**
     * Returns the accountId if the token exists (valid or expired).
     * Used by logout so we can wipe tokens even if they've expired.
     */
    public Optional<UUID> validateAndGetAccountId(String tokenValue) {
        return repository.findByToken(tokenValue).map(RefreshToken::getAccountId);
    }

    /** Removes all refresh tokens for the account (logout). */
    @Transactional
    public void deleteByAccountId(UUID accountId) {
        repository.deleteByAccountId(accountId);
    }
}
