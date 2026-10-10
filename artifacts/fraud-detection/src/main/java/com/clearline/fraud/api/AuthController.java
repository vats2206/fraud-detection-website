package com.clearline.fraud.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clearline.fraud.api.ApiDtos.LoginRequest;
import com.clearline.fraud.api.ApiDtos.RegisterRequest;
import com.clearline.fraud.api.ApiDtos.UserView;
import com.clearline.fraud.model.AppUser;
import com.clearline.fraud.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/risk-api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserView> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest) {
        AppUser user = authService.register(request);
        startSession(servletRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toView(user));
    }

    @PostMapping("/login")
    public UserView login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        AppUser user = authService.login(request);
        startSession(servletRequest, user);
        return toView(user);
    }

    @GetMapping("/me")
    public UserView me(HttpSession session) {
        return toView(authService.findById(requireUserId(session)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    static Long requireUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");
        if (userId instanceof Long id) {
            return id;
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in to continue.");
    }

    private void startSession(HttpServletRequest request, AppUser user) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getId());
    }

    private UserView toView(AppUser user) {
        return new UserView(user.getId(), user.getName(), user.getEmail());
    }
}
