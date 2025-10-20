package com.clinic.physioclinic.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "patient",
        indexes = {
                @Index(name = "ix_patient_user", columnList = "user_id"),
                @Index(name = "ix_patient_email", columnList = "email")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_patient_email", columnNames = {"email"})
                // if your model is strictly 1:1 with users, you can also make user_id unique:
                // @UniqueConstraint(name = "uq_patient_user", columnNames = {"user_id"})
        }
)
public class Patient implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the auth/user account that owns this patient profile
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_patient_user")
    )
    private User user;  // assumes com.clinic.physioclinic.model.User exists

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 25)
    private String phone;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    /* ===== Lifecycle ===== */

    @PrePersist
    protected void onCreate() {
        final Instant now = Instant.now();
        this.createdAt = (this.createdAt == null) ? now : this.createdAt;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /* ===== Getters / Setters ===== */

    public Long getId() {
        return id;
    }

    public Patient setId(Long id) {
        this.id = id;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Patient setUser(User user) {
        this.user = user;
        return this;
    }

    public String getName() {
        return name;
    }

    public Patient setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Patient setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public Patient setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Patient setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Patient setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /* ===== equals / hashCode / toString ===== */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient that)) return false;
        // Entity equality by id if present
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        // stable hash for persisted entities
        return 31;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
