package com.clinic.physioclinic.service;

import com.clinic.physioclinic.entity.IdempotencyKey;
import com.clinic.physioclinic.repository.IdempotencyKeyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
public class IdempotencyService {

    private final IdempotencyKeyRepository repo;
    public IdempotencyService(IdempotencyKeyRepository repo) { this.repo = repo; }

    private String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException("SHA-256 unavailable", e); }
    }

    @Transactional
    public Long checkOrReserve(String endpoint, String idemKey, String requestBodyJson) {
        var reqHash = sha256(requestBodyJson);
        var existing = repo.findByIdempotencyKeyAndEndpoint(idemKey, endpoint);
        if (existing.isPresent()) {
            var e = existing.get();
            // Same request? return existing response id (can be null if previous failed mid-flight)
            if (reqHash.equals(e.getRequestHash())) return e.getResponseId();
            // Different body with same key → reject
            throw new IllegalStateException("Idempotency key replay with different request");
        }
        // Reserve row (no response yet)
        var rec = new IdempotencyKey();
        rec.setIdempotencyKey(idemKey);
        rec.setEndpoint(endpoint);
        rec.setRequestHash(reqHash);
        try {
            repo.saveAndFlush(rec);
        } catch (DataIntegrityViolationException ex) {
            // Race: another thread saved it
            var again = repo.findByIdempotencyKeyAndEndpoint(idemKey, endpoint).orElseThrow();
            if (reqHash.equals(again.getRequestHash())) return again.getResponseId();
            throw new IllegalStateException("Idempotency key replay with different request");
        }
        return null; // reserved; no response yet
    }

    @Transactional
    public void attachResponse(String endpoint, String idemKey, Long responseId) {
        var rec = repo.findByIdempotencyKeyAndEndpoint(idemKey, endpoint).orElseThrow();
        if (rec.getResponseId() == null) {
            rec.setResponseId(responseId);
            repo.save(rec);
        }
    }
}
