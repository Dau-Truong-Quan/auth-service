package garder.quan.authservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Subset of account data returned by the account-service after credential verification.
 */
@Getter
@Setter
public class AccountInfo {

    private UUID id;
    private String username;
    private String email;
    private String status;
    private String role;
}
