package com.rent.hatabooking.booking.entity;

import com.rent.hatabooking.booking.dto.enums.BookingStatus;

import java.sql.Date;
import java.util.UUID;

public class BookingEntity {

    private UUID id;

    private UUID idProfile;

    private UUID idRoom;

    private UUID idRoomOwner;

    private Date startDate;

    private String description;

    private BookingStatus status;

    public BookingEntity() {
    }

    public BookingEntity(UUID id,
                         UUID idProfile,
                         UUID idRoom,
                         UUID idRoomOwner,
                         Date startDate,
                         String description,
                         BookingStatus status) {
        this.id = id;
        this.idProfile = idProfile;
        this.idRoom = idRoom;
        this.idRoomOwner = idRoomOwner;
        this.startDate = startDate;
        this.description = description;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getIdProfile() {
        return idProfile;
    }

    public void setIdProfile(UUID idProfile) {
        this.idProfile = idProfile;
    }

    public UUID getIdRoom() {
        return idRoom;
    }

    public void setIdRoom(UUID idRoom) {
        this.idRoom = idRoom;
    }

    public UUID getIdRoomOwner() {
        return idRoomOwner;
    }

    public void setIdRoomOwner(UUID idRoomOwner) {
        this.idRoomOwner = idRoomOwner;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
