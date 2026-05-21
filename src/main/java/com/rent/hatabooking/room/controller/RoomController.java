package com.rent.hatabooking.room.controller;

import com.rent.hatabooking.profile.dto.Profile;
import com.rent.hatabooking.room.dto.Room;
import com.rent.hatabooking.room.service.RoomService;
import com.rent.hatabooking.web.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Квартиры", description = "Прокси для работы с квартирами из главного приложения")
public class RoomController {

    Logger logger = LoggerFactory.getLogger(RoomController.class);
    private final RoomService service;

    public RoomController(RoomService service) {
        this.service = service;
    }

    @GetMapping("/{idRoom}/{idRoomOwner}")
    public Room getRoom(
            @PathVariable UUID idRoom,
            @PathVariable UUID idRoomOwner) {
        logger.info("Called getRoom from microservice");
        Room room = service.getRoom(idRoom, idRoomOwner);
        if (room == null) {
            throw new EntityNotFoundException("Квартира не найдена");
        }
        return room;
    }
}
