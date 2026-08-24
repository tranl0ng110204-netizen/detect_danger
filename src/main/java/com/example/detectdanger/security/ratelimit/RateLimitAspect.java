package com.example.detectdanger.security.ratelimit;

import com.example.detectdanger.entity.User;
import com.example.detectdanger.exceptions.TooManyRequestsException;
import com.example.detectdanger.repository.UserRepository;
import com.example.detectdanger.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RateLimitService rateLimitService;
    private final UserRepository userRepository;

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(
            ProceedingJoinPoint joinPoint,
            RateLimit rateLimit
    ) throws Throwable {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return joinPoint.proceed();
        }

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow();

        Long userId = user.getId();

        ServletRequestAttributes attributes =
                (ServletRequestAttributes)
                        RequestContextHolder
                                .getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request =
                attributes.getRequest();

        String key =
                "user:"
                        + userId
                        + ":"
                        + request.getMethod()
                        + ":"
                        + request.getRequestURI();

        boolean allowed =
                rateLimitService.tryConsume(
                        key,
                        rateLimit.capacity(),
                        rateLimit.durationHours()
                );

        if (!allowed) {
            throw new TooManyRequestsException(
                    "Too many requests. Please try again later."
            );
        }

        return joinPoint.proceed();
    }
}
