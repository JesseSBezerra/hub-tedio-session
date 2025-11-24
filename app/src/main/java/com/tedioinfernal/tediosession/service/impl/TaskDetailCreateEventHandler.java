package com.tedioinfernal.tediosession.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tedioinfernal.tediosession.annotation.EventHandler;
import com.tedioinfernal.tediosession.dto.ImprovedTaskDTO;
import com.tedioinfernal.tediosession.dto.WhatsAppMessageDTO;
import com.tedioinfernal.tediosession.entity.EvolutionSession;
import com.tedioinfernal.tediosession.entity.MessageSession;
import com.tedioinfernal.tediosession.service.EventHandlerService;
import com.tedioinfernal.tediosession.service.EvolutionApiService;
import com.tedioinfernal.tediosession.service.MessageSessionService;
import com.tedioinfernal.tediosession.service.MondayService;
import com.tedioinfernal.tediosession.service.OpenAIService;
import com.tedioinfernal.tediosession.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;
import java.util.Optional;

@Slf4j
@EventHandler("TASK-DETAIL-CREATE")
@RequiredArgsConstructor
public class TaskDetailCreateEventHandler implements EventHandlerService {

    private final MessageSessionService messageSessionService;
    private final SessionService sessionService;
    private final EvolutionApiService evolutionApiService;
    private final MondayService mondayService;
    private final OpenAIService openAIService;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    public void handle(Map<String, Object> object) {
        log.info("Processing TASK-DETAIL-CREATE event");
        
        try {
            // Extrair messageSessionId
            Long messageSessionId = extractMessageSessionId(object);
            
            if (messageSessionId == null) {
                log.error("messageSessionId not found in object");
                return;
            }
            
            // Buscar MessageSession
            Optional<MessageSession> messageSessionOpt = messageSessionService.findById(messageSessionId);
            
            if (messageSessionOpt.isEmpty()) {
                log.error("MessageSession not found with ID: {}", messageSessionId);
                return;
            }
            
            MessageSession messageSession = messageSessionOpt.get();
            log.info("Creating task for MessageSession ID: {}", messageSession.getId());
            
            // Converter objeto para WhatsAppMessageDTO
            WhatsAppMessageDTO messageData = objectMapper.convertValue(object, WhatsAppMessageDTO.class);
            
            // Extrair informações
            String remoteJid = messageData.getData().getKey().getRemoteJid();
            String number = extractPhoneNumber(remoteJid);
            Long evolutionInstanceId = messageSession.getEvolutionSession().getInstance().getId();
            
            // Extrair mensagem do usuário (detalhamento da tarefa)
            String taskDetail = extractUserMessage(messageData, messageSession);
            log.info("Task detail from user: {}", taskDetail);
            
            // 1. Melhorar descrição com GPT
            log.info("Improving task description with OpenAI GPT...");
            ImprovedTaskDTO improvedTask = openAIService.improveTaskDescription(taskDetail);
            log.info("Improved task - Title: {}, Detail length: {}, Deadline: {}", 
                     improvedTask.getTitulo(), improvedTask.getDetalhe().length(), improvedTask.getPrazo());
            
            // 2. Criar item no Monday.com com título melhorado e prazo
            String itemId = mondayService.createTaskItem(improvedTask.getTitulo(), improvedTask.getPrazo());
            log.info("Monday.com item created with ID: {} and deadline: {}", itemId, improvedTask.getPrazo());
            
            // 3. Criar comentário com o detalhamento melhorado
            String updateId = mondayService.createTaskUpdate(itemId, improvedTask.getDetalhe());
            log.info("Monday.com update created with ID: {}", updateId);
            
            // 3. Enviar mensagem de agradecimento
            String thankYouMessage = "✅ *Tarefa criada com sucesso!*\n\n" +
                    "Obrigado por compartilhar os detalhes.\n\n" +
                    "Sua solicitação foi registrada e em breve entraremos em contato.\n\n" +
                    "Sua sessão foi encerrada. Até breve! 👋";
            
            evolutionApiService.sendMessage(number, thankYouMessage, evolutionInstanceId);
            
            log.info("Thank you message sent to number: {}", number);
            
            // 4. Inativar a sessão
            EvolutionSession evolutionSession = messageSession.getEvolutionSession();
            sessionService.deactivateSession(evolutionSession.getId());
            
            log.info("Evolution session {} deactivated", evolutionSession.getId());
            
            // 5. Atualizar status da mensagem
            messageSessionService.updateCurrentEvent(messageSession.getId(), "TASK-CREATED");
            
            log.info("TASK-DETAIL-CREATE event processed successfully");
            
        } catch (Exception e) {
            log.error("Error processing TASK-DETAIL-CREATE event", e);
            throw new RuntimeException("Failed to process TASK-DETAIL-CREATE event", e);
        }
    }
    
    private Long extractMessageSessionId(Map<String, Object> object) {
        Object idObj = object.get("messageSessionId");
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        }
        return null;
    }
    
    private String extractUserMessage(WhatsAppMessageDTO messageData, MessageSession messageSession) {
        Map<String, Object> message = messageData.getData().getMessage();
        
        // Verificar se é mensagem de texto
        if (message != null && message.containsKey("conversation")) {
            return message.get("conversation").toString();
        }
        
        // Verificar se é mensagem de áudio
        if (message != null && message.containsKey("audioMessage")) {
            log.info("Audio message detected, processing transcription...");
            return processAudioMessage(messageData, messageSession);
        }
        
        return "Sem detalhes fornecidos";
    }
    
    private String processAudioMessage(WhatsAppMessageDTO messageData, MessageSession messageSession) {
        try {
            // Extrair ID da mensagem
            String messageId = messageData.getData().getKey().getId();
            Long evolutionInstanceId = messageSession.getEvolutionSession().getInstance().getId();
            
            log.info("Getting audio media for messageId: {}", messageId);
            
            // 1. Buscar arquivo de áudio
            com.tedioinfernal.tediosession.dto.MediaResponseDTO mediaResponse = 
                    evolutionApiService.getMedia(messageId, evolutionInstanceId);
            
            log.info("Audio media retrieved: {} ({})", mediaResponse.getFileName(), mediaResponse.getMediaType());
            
            // 2. Decodificar base64
            byte[] audioData = java.util.Base64.getDecoder().decode(mediaResponse.getBase64());
            
            log.info("Audio data decoded, size: {} bytes", audioData.length);
            
            // 3. Converter nome do arquivo para formato suportado (.ogg ao invés de .oga)
            String fileName = mediaResponse.getFileName();
            if (fileName.endsWith(".oga")) {
                fileName = fileName.replace(".oga", ".ogg");
                log.info("Converted filename from .oga to .ogg: {}", fileName);
            }
            
            // 4. Transcrever áudio
            String transcription = openAIService.transcribeAudio(audioData, fileName);
            
            log.info("Audio transcribed successfully: {}", transcription);
            
            return transcription;
            
        } catch (Exception e) {
            log.error("Error processing audio message", e);
            return "Erro ao processar áudio. Por favor, envie uma mensagem de texto.";
        }
    }
    
    private String extractPhoneNumber(String remoteJid) {
        // Remove @s.whatsapp.net e outros sufixos
        if (remoteJid.contains("@")) {
            return remoteJid.substring(0, remoteJid.indexOf("@"));
        }
        return remoteJid;
    }
}
