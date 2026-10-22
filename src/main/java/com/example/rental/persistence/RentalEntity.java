package com.example.rental.persistence;
 
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "rental")
public class RentalEntity {

    @Id
    private UUID id;

    @Column(name = "business_key", nullable = false, unique = true)
    private String businessKey;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String title;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getBusinessKey() { return businessKey; }
    public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
