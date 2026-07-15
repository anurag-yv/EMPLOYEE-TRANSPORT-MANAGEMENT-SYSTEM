package com.example.employee_transport_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entity representing an Employee in the transport system.
 */
@Entity
@Table(name = "employees")
public final class Employee {

    /** Minimum password length. */
    private static final int MIN_PWD_LEN = 4;

    /** The unique identifier of the employee. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The full name of the employee. */
    @NotBlank(message = "Name is required")
    private String name;

    /** The unique email address of the employee. */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(unique = true)
    private String email;

    /** The encrypted password for authentication. */
    @NotBlank(message = "Password is required")
    @Size(min = MIN_PWD_LEN, message = "Password too short")
    private String password;

    /** The system role assigned to this user (e.g., EMPLOYEE, CITIZEN). */
    private String role = "Employee";

    public Long getId() {
        return id;
    }

    public void setId(final Long pId) {
        this.id = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String pName) {
        this.name = pName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String pEmail) {
        this.email = pEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String pPassword) {
        this.password = pPassword;
    }

    public String getRole() {
        return role;
    }

    public void setRole(final String pRole) {
        this.role = pRole;
    }

}
