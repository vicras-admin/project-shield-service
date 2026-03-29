package com.vicras.projectshield.security;

import com.vicras.projectshield.entity.Member;
import com.vicras.projectshield.repository.MemberRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserContext {

    private final MemberRepository memberRepository;

    public UserContext(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Optional<Member> getCurrentMember() {
        String clerkUserId = getCurrentClerkUserId();
        if (clerkUserId == null) {
            return Optional.empty();
        }
        return memberRepository.findByClerkUserId(clerkUserId);
    }

    public String getCurrentClerkUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return jwt.getSubject();
    }

    public String getCurrentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return jwt.getClaimAsString("email");
    }

    public boolean hasUserContext() {
        return getCurrentClerkUserId() != null;
    }
}
