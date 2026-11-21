package com.example.rental.handler;

import io.swagger.v3.oas.annotations.responses.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class MeController {
    public record MeResponse(String username, List<String> authorities) {}
    @ApiResponses({@ApiResponse(responseCode="200"),@ApiResponse(responseCode="401")})
    @GetMapping("/api/me")
    public MeResponse me(Authentication authentication) {
        return new MeResponse(authentication.getName(), authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }
}