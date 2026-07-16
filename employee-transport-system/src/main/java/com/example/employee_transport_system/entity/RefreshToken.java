package com.example.employee_transport_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Entity representing a persistent refresh token.
 */
@Entity
@Table(name = "refresh_tokens")
public final class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Instant expiryDate;

    public RefreshToken() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long pId) {
        this.id = pId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String pToken) {
        this.token = pToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String pEmail) {
        this.email = pEmail;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(final Instant pExpiryDate) {
        this.expiryDate = pExpiryDate;
    }
}
