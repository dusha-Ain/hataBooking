package com.rent.hatabooking.booking.dto;

import java.util.UUID;

public record IdfBooking(
        UUID idProfile,
        UUID idRoom
) {
}
