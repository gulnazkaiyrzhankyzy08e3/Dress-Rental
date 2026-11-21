package com.example.rental.handler;
import com.example.rental.security.TokenService;
import io.swagger.v3.oas.annotations.responses.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
    private final AuthenticationManager auth;
    private final TokenService tokens;
    public AuthController(AuthenticationManager auth, TokenService tokens) { this.auth=auth; this.tokens=tokens; }
    @ApiResponses({@ApiResponse(responseCode="200"),@ApiResponse(responseCode="400"),@ApiResponse(responseCode="401")})
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        var user = auth.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(request.username(),request.password()));
        return new TokenResponse(tokens.issue(user),"Bearer",TokenService.EXPIRES_IN);
    }
}