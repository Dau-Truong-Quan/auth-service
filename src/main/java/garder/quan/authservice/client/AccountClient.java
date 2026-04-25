package garder.quan.authservice.client;

import garder.quan.authservice.dto.AccountInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class AccountClient {

    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public AccountClient(RestTemplate restTemplate,
                         @Value("${account.service.url}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
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
}
