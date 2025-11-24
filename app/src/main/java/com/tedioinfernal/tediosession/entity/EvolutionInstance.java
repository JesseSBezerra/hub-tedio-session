package com.tedioinfernal.tediosession.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "evolution_instances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvolutionInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instance_name", nullable = false, length = 255)
    private String instanceName;

    @Column(name = "instance_id", length = 255)
    private String instanceId;

    @Column(nullable = false)
    private Boolean qrcode;

    @Column(name = "qrcode_base64", columnDefinition = "TEXT")
    private String qrcodeBase64;

    @Column(nullable = false, length = 100)
    private String integration;

    @Column(length = 50)
    private String status;

    @Column(length = 255)
    private String hash;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evolution_id", nullable = false)
    private Evolution evolution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
