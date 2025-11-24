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
@Table(name = "evolution_sessions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"instance_id", "remote_jid"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvolutionSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instance_id", nullable = false)
    private EvolutionInstance instance;

    @Column(name = "remote_jid", nullable = false, length = 255)
    private String remoteJid;

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, INACTIVE

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
