package com.rent.hatabooking.booking.service;

import com.rent.hatabooking.booking.dto.IdfBooking;
import com.rent.hatabooking.booking.entity.BookingEntity;
import com.rent.hatabooking.web.exception.EntityNotFoundException;
import com.rent.hatabooking.web.exception.EntityNotFoundInternalException;
import com.rent.hatabooking.web.exception.EntityToManyResultException;
import com.rent.hatabooking.web.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BookingValidator {

    private BookingValidator() {
    }

    public static Optional<BookingEntity> validateSearchResult(
            List<BookingEntity> booking, String idf) {
        if (booking.size() > 1) {
            throw new EntityToManyResultException(
                    String.format("More than one booking found by identification = %s", idf));
        }
        if (booking.isEmpty()) {
            return Optional.empty();
        }
        return booking.stream().findFirst();
    }

    public static BookingEntity validateBookingExistsAfterCreate(
            Optional<BookingEntity> booking, IdfBooking id) {
        return booking.orElseThrow(() -> new EntityNotFoundInternalException(
                String.format("Booking not found after create by IdfBooking = %s", id.toString())));
    }

    public static BookingEntity validateBookingExistsAfterUpdate(
            Optional<BookingEntity> booking, UUID id) {
        return booking.orElseThrow(() -> new EntityNotFoundInternalException(
                String.format("Booking not found after update by id = %s", id)));
    }

    public static BookingEntity validateBookingCheckExists(
            Optional<BookingEntity> booking, UUID id) {
        return booking.orElseThrow(() -> new EntityNotFoundException(
                String.format("Booking not found by id = %s", id)));
    }

    public static void validateCreationResult(boolean create, IdfBooking id) {
        if (!create) {
            throw new ValidationException(
                    String.format("Booking creation failed by IdfBooking = %s", id.toString()));
        }
    }

    public static void validateUpdateResult(boolean update, UUID id) {
        if (!update) {
            throw new ValidationException(
                    String.format("Booking update failed by id = %s", id));
        }
    }

    public static void validateDeleteResult(boolean delete, UUID id) {
        if (!delete) {
            throw new ValidationException(
                    String.format("Booking delete failed by id = %s", id));
        }
    }

    public static void validateUpdateStatusResult(boolean res, UUID id) {
        if (!res) {
            throw new ValidationException(
                    String.format("Booking change status failed by id = %s", id));
        }
    }

    public static void validateBookingNotExists(
            Optional<BookingEntity> existingBooking, IdfBooking id) {
        if (existingBooking.isPresent()) {
            throw new ValidationException(
                    String.format("Booking by idfBooking = %s already exist", id));
        }
    }
}
