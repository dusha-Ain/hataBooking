package com.rent.hatabooking.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rent.hatabooking.booking.dto.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.sql.Date;
import java.util.UUID;

public record Booking(
        @Null
        UUID id,

        UUID idProfile,
        UUID idRoom,
        UUID idRoomOwner,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2026-03-20")
        Date startDate,

        @Size(max = 250)
        String description,
        @Null
        BookingStatus status
) {
}
