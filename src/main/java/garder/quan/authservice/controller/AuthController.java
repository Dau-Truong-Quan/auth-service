package garder.quan.authservice.controller;

import garder.quan.authservice.client.AccountClient;
import garder.quan.authservice.dto.AccountInfo;
import garder.quan.authservice.dto.LoginRequest;
import garder.quan.authservice.dto.LoginResponse;
import garder.quan.authservice.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AccountClient accountClient;
    private final JwtService jwtService;

    public AuthController(AccountClient accountClient, JwtService jwtService) {
        this.accountClient = accountClient;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AccountInfo account = accountClient.verifyCredentials(request.getUsername(), request.getPassword());
        String token = jwtService.generateToken(account.getId(), account.getUsername(), account.getRole());
        return ResponseEntity.ok(new LoginResponse(token, account.getId(), account.getUsername(), account.getRole()));
    }
}
