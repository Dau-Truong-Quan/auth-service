package garder.quan.authservice.controller;

import garder.quan.authservice.client.AccountClient;
import garder.quan.authservice.dto.AccountInfo;
import garder.quan.authservice.dto.LoginRequest;
import garder.quan.authservice.dto.LoginResponse;
import garder.quan.authservice.dto.RefreshRequest;
import garder.quan.authservice.entity.RefreshToken;
import garder.quan.authservice.service.JwtService;
import garder.quan.authservice.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AccountClient accountClient;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AccountClient accountClient,
                          JwtService jwtService,
                          RefreshTokenService refreshTokenService) {
        this.accountClient = accountClient;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AccountInfo account = accountClient.verifyCredentials(request.getUsername(), request.getPassword());

        String accessToken = jwtService.generateToken(account.getId(), account.getUsername(), account.getRole());
        RefreshToken refreshToken = refreshTokenService.create(account.getId());

        return ResponseEntity.ok(new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                account.getId(),
                account.getUsername(),
                account.getRole()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        // Validate old token, delete it, issue a new one (rotation)
        RefreshToken newRefreshToken = refreshTokenService.validateAndRotate(request.getRefreshToken());

        // Fetch account info so we can re-embed username and role in the new access token
        AccountInfo account = accountClient.getAccountById(newRefreshToken.getAccountId());

        String accessToken = jwtService.generateToken(account.getId(), account.getUsername(), account.getRole());

        return ResponseEntity.ok(new LoginResponse(
                accessToken,
                newRefreshToken.getToken(),
                account.getId(),
                account.getUsername(),
                account.getRole()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        // Look up accountId from the token, then wipe all refresh tokens for that account
        refreshTokenService.validateAndGetAccountId(request.getRefreshToken())
                .ifPresent(refreshTokenService::deleteByAccountId);
        return ResponseEntity.noContent().build();
    }
}
