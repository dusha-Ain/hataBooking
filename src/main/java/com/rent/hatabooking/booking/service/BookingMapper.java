package com.rent.hatabooking.booking.service;

import com.rent.hatabooking.booking.dto.Booking;
import com.rent.hatabooking.booking.entity.BookingEntity;
import com.rent.hatabooking.web.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BookingMapper {

    public BookingEntity toEntity(Booking booking) {
        if (booking == null){
            throw new ValidationException("booking must be not null");
        }
        return new BookingEntity(
                booking.id(),
                booking.idProfile(),
                booking.idRoom(),
                booking.idRoomOwner(),
                booking.startDate(),
                booking.description(),
                booking.status()
        );
    }

    public BookingEntity toEntity(BookingEntity bookingEntity, Booking booking){
        if (booking == null){
            return bookingEntity;
        }
        Optional.ofNullable(booking.startDate()).ifPresent(bookingEntity::setStartDate);
        Optional.ofNullable(booking.description()).ifPresent(bookingEntity::setDescription);
        return bookingEntity;
    }

    public Booking toDomainBooking(BookingEntity bookingEntity) {
        if (bookingEntity == null){
            throw new ValidationException("bookingEntity must be not null");
        }
        return new Booking(
                bookingEntity.getId(),
                bookingEntity.getIdProfile(),
                bookingEntity.getIdRoom(),
                bookingEntity.getIdRoomOwner(),
                bookingEntity.getStartDate(),
                bookingEntity.getDescription(),
                bookingEntity.getStatus()
        );
    }

}
