package com.rent.hatabooking.booking.controller;

import com.rent.hatabooking.booking.dto.Booking;
import com.rent.hatabooking.booking.dto.Bookings;
import com.rent.hatabooking.booking.dto.enums.BookingStatus;
import com.rent.hatabooking.booking.service.BookingService;
import com.rent.hatabooking.web.helpers.Pageable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/bookings/")
@Tag(name = "Бронирование", description = "Управление бронированием квартир")
public class BookingController {

    private final Logger logger = LoggerFactory.getLogger(BookingController.class);
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @Operation(summary = "Создать новое бронирование")
    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Parameter(description = "Данные о бронировании")
            @Valid @RequestBody Booking booking) {
        logger.info("Called createBooking");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createBooking(booking));
    }

    @Operation(summary = "Показать бронирование")
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(
            @Parameter(description = "Идентификатор удобства")
            @PathVariable UUID id) {
        logger.info("Called getBookingById id = {}", id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.getBookingById(id));
    }

    @Operation(summary = "Показать все бронирования")
    @GetMapping("/")
    public ResponseEntity<Bookings> getAllBookingsByFilter(
            @Parameter(description = "Количество бронирований для вывода")
            @RequestParam(name = "pageSize", required = false) Integer pageSize,

            @Parameter(description = "Номер страницы поиска")
            @RequestParam(name = "pageNumber", required = false) Integer pageNumber
    ) {
        logger.info("Called getAllBookingsByFilter");
        var pageable = new Pageable();
        pageable.setPageSize(pageSize);
        pageable.setPageNumber(pageNumber);
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.getAllBookingsByFilter(pageable));
    }

    @Operation(summary = "Обновить информацию о бронированиии")
    @PutMapping("/update/{id}")
    public ResponseEntity<Booking> updateBooking(
            @Parameter(description = "Идентификатор бронирования")
            @PathVariable UUID id,
            @Parameter(description = "Информация о бронировании")
            @Valid @RequestBody Booking booking) {
        logger.info("Called updateBooking id = {}", id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.updateBookingById(id, booking));
    }

    @Operation(summary = "Удалить бронирование")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteBooking(
            @Parameter(description = "Идентификатор бронирования")
            @PathVariable UUID id) {
        logger.info("Called deleteBooking by id = {}", id);
        service.deleteBookingById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Изменить статус бронирования")
    @PutMapping("/update-status/{id}")
    public ResponseEntity<Booking> updateStatusBooking(
            @Parameter(description = "Идентификатор бронирования")
            @PathVariable UUID id,
            @Parameter(description = "Статус бронирования")
            @Valid @RequestBody BookingStatus status) {
        logger.info("Called updateBookingStatus id = {}", id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.updateBookingStatusById(id, status));
    }

}
