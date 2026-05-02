package garder.quan.authservice.client;

import garder.quan.authservice.dto.AccountInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Component
public class AccountClient {

    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public AccountClient(RestTemplate restTemplate,
                         @Value("${account.service.url}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
    }

    /** Fetches account info by ID — used when refreshing a token. */
    public AccountInfo getAccountById(UUID accountId) {
        try {
            return restTemplate.getForObject(
                    accountServiceUrl + "/api/accounts/" + accountId,
                    AccountInfo.class
            );
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found");
        }
    }

    /**
     * Calls account-service to verify credentials.
     * Returns account info on success, throws 401 on bad credentials.
     */
    public AccountInfo verifyCredentials(String username, String password) {
        try {
            return restTemplate.postForObject(
                    accountServiceUrl + "/api/accounts/verify-credentials",
                    Map.of("username", username, "password", password),
                    AccountInfo.class
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
            }
            throw e;
        }
    }

    /**
     * Find-or-create an account from a Google identity. Caller is responsible for
     * having verified the Google ID token before invoking this.
     */
    public AccountInfo findOrCreateGoogleAccount(String email, String displayName, String googleSub) {
        try {
            return restTemplate.postForObject(
                    accountServiceUrl + "/api/accounts/google-upsert",
                    Map.of(
                            "email", email,
                            "displayName", displayName == null ? "" : displayName,
                            "googleSub", googleSub
                    ),
                    AccountInfo.class
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account is not active");
            }
            throw e;
        }
    }

    /**
     * Calls account-service to create a new account. The plain password is sent in
     * the {@code passwordHash} field (the account-service BCrypt-encodes it before persisting).
     * Returns the created account info; surfaces 409 on duplicate username/email.
     */
    public AccountInfo register(String username, String email, String password) {
        try {
            return restTemplate.postForObject(
                    accountServiceUrl + "/api/accounts",
                    Map.of(
                            "username", username,
                            "email", email,
                            "passwordHash", password,
                            "status", "ACTIVE",
                            "role", "USER"
                    ),
                    AccountInfo.class
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username or email already exists");
            }
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid registration data");
            }
            throw e;
        }
    }
}
