package com.clinic.physioclinic.dto;


import com.clinic.physioclinic.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class RegisterRequest {
    @NotBlank
    private String fullName;
    @Email @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotNull
    private Role role; // PATIENT or DOCTOR


    private String phone; // for PATIENT (optional)
    private String specialization; // for DOCTOR (optional)


    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}