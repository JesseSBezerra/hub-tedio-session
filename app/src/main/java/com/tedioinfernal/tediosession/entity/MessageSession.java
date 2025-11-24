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
@Table(name = "message_session")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evolution_session_id", nullable = false)
    private EvolutionSession evolutionSession;

    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String messageContent;

    @Column(name = "current_event", nullable = false, length = 100)
    private String currentEvent;

    @Column(name = "message_timestamp", nullable = false)
    private Long messageTimestamp;

    @Column(name = "remote_jid", nullable = false, length = 255)
    private String remoteJid;

    @Column(name = "from_me", nullable = false)
    private Boolean fromMe;

    @Column(name = "message_type", length = 50)
    private String messageType;

    @Column(name = "push_name", length = 255)
    private String pushName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
