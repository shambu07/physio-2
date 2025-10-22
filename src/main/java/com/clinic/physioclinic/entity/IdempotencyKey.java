package com.clinic.physioclinic.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "idempotency_keys", uniqueConstraints =
@UniqueConstraint(name = "uq_idem", columnNames = {"idempotency_key","endpoint"}))
public class IdempotencyKey {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "endpoint", nullable = false, length = 128)
    private String endpoint;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_id")
    private Long responseId;

    // getters/setters
    public Long getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String k) { this.idempotencyKey = k; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String e) { this.endpoint = e; }
    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String h) { this.requestHash = h; }
    public Long getResponseId() { return responseId; }
    public void setResponseId(Long r) { this.responseId = r; }
}
