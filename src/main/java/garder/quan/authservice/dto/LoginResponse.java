package garder.quan.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private UUID userId;
    private String username;
    private String role;
}
