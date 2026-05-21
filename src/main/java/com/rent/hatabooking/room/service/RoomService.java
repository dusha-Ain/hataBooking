package com.rent.hatabooking.room.service;

import com.rent.hatabooking.room.dto.Room;
import com.rent.hatabooking.web.kafka.dto.enums.ProfileAction;
import com.rent.hatabooking.web.kafka.dto.enums.RoomAction;
import com.rent.hatabooking.web.kafka.dto.room.RoomValidationRequest;
import com.rent.hatabooking.web.kafka.dto.room.RoomValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public RoomService(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Хранилище ожидающих ответов
    private final Map<String, CompletableFuture<RoomValidationResponse>> pendingRequests =
            new ConcurrentHashMap<>();

    /**
     * Проверить, существует ли квартира
     */
    public boolean roomExists(UUID idRoom, UUID idRoomOwner) {
        log.debug("Проверка существования квартиры: {}, {}", idRoom, idRoomOwner);
        RoomValidationResponse response = sendRequest(idRoom, idRoomOwner, RoomAction.CHECK_EXISTS.name());
        boolean exists = response != null && response.isExists();
        log.debug("Квартира {} существует: {}", idRoom, exists);
        return exists;
    }

    /**
     * Получить квартиру по ID
     */
    public Room getRoom(UUID idRoom, UUID idRoomOwner) {
        log.debug("Получение квартиры по ID: {}", idRoom);
        RoomValidationResponse response = sendRequest(idRoom, idRoomOwner, RoomAction.GET_ROOM.name());
        if (response != null && response.isExists() && response.getRoom() != null) {
            var room = new Room();
            room.setIdRoom(response.getRoom().getIdRoom());
            room.setIdRoomOwner(response.getRoom().getIdRoomOwner());
            room.setPrice(response.getRoom().getPrice());
            room.setRoomStatus(response.getRoom().getRoomStatus());
            return room;
        }
        return null;
    }

    /**
     * Отправить запрос в основное приложение
     */
    private RoomValidationResponse sendRequest(UUID idRoom, UUID idRoomOwner, String action) {
        String requestId = UUID.randomUUID().toString();
        CompletableFuture<RoomValidationResponse> future = new CompletableFuture<>();
        pendingRequests.put(requestId, future);

        // Создаем объект, а не строку!
        var request = new RoomValidationRequest();
        request.setIdRequest(requestId);
        request.setIdRoom(idRoom);
        request.setIdRoomOwner(idRoomOwner);
        request.setAction(action);

        log.info("Отправлен запрос {} для пользователя {} с действием {}", requestId, idRoom, action);

        // Отправляем объект, Kafka сама сериализует в JSON
        kafkaTemplate.send("room-validation-requests", request);

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Таймаут ожидания ответа для запроса {}: {}", requestId, e.getMessage());
            return null;
        }
    }

    /**
     * Слушаем ответы от основного приложения (принимаем как String)
     */
    @KafkaListener(topics = "room-validation-responses", groupId = "booking-microservice")
    public void handleValidationResponse(String message) {
        log.debug("📨 Получен ответ от основного приложения: {}", message);

        try {
            // Десериализуем JSON в объект
            RoomValidationResponse response = objectMapper.readValue(message, RoomValidationResponse.class);
            log.debug("✅ Десериализован ответ для запроса {}: exists={}",
                    response.getIdRequest(), response.isExists());

            CompletableFuture<RoomValidationResponse> future = pendingRequests.remove(response.getIdRequest());
            if (future != null) {
                future.complete(response);
            } else {
                log.warn("⚠️ Получен ответ для неизвестного запроса: {}", response.getIdRequest());
            }

        } catch (Exception e) {
            log.error("❌ Ошибка десериализации ответа: {}", e.getMessage(), e);
        }
    }

}
