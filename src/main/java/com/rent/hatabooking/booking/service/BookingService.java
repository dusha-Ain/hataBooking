package com.rent.hatabooking.booking.service;

import com.rent.hatabooking.booking.dao.BookingDao;
import com.rent.hatabooking.booking.dto.Booking;
import com.rent.hatabooking.booking.dto.Bookings;
import com.rent.hatabooking.booking.dto.IdfBooking;
import com.rent.hatabooking.booking.dto.enums.BookingStatus;
import com.rent.hatabooking.booking.entity.BookingEntity;
import com.rent.hatabooking.profile.service.ProfileService;
import com.rent.hatabooking.room.service.RoomService;
import com.rent.hatabooking.web.exception.EntityNotFoundException;
import com.rent.hatabooking.web.helpers.AppConfig;
import com.rent.hatabooking.web.helpers.Pageable;
import com.rent.hatabooking.web.kafka.dto.BookingEvent;
import com.rent.hatabooking.web.kafka.dto.enums.BookingEventType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingDao bookingDao;
    private final ProfileService profileService;
    private final RoomService roomService;
    private final BookingMapper mapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BookingService(BookingDao BookingDao,
                          ProfileService profileService,
                          RoomService roomService,
                          BookingMapper mapper,
                          KafkaTemplate<String, Object> kafkaTemplate) {
        this.bookingDao = BookingDao;
        this.profileService = profileService;
        this.roomService = roomService;
        this.mapper = mapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${kafka.topics.booking-events:booking-events}")
    private String bookingEventsTopic;

    @Transactional
    public Booking createBooking(@Valid Booking booking) {
        boolean userExists = profileService.userExists(booking.idProfile());
        if (!userExists) {
            throw new EntityNotFoundException("Пользователь с ID " + booking.idProfile() + " не найден");
        }
        boolean roomExists = roomService.roomExists(booking.idRoom(), booking.idRoomOwner());
        if (!roomExists) {
            throw new EntityNotFoundException("Квартира с ID " + booking.idRoom() + " не найдена");
        }
        var idf = new IdfBooking(booking.idProfile(), booking.idRoom());
        BookingValidator.validateBookingNotExists(bookingDao.getBookingByIdfBooking(idf), idf);
        BookingValidator.validateCreationResult(
                bookingDao.createBooking(mapper.toEntity(booking)), idf);
        var createdBooking = BookingValidator.validateBookingExistsAfterCreate(
                bookingDao.getBookingByIdfBooking(idf), idf);

        BookingEvent event = new BookingEvent(
                BookingEventType.BOOKING_CREATED.name(),
                createdBooking.getId(),
                createdBooking.getIdProfile(),
                createdBooking,
                LocalDateTime.now()
        );
        kafkaTemplate.send(bookingEventsTopic, event);
        return mapper.toDomainBooking(createdBooking);
    }

    public Booking getBookingById(UUID id) {
        var booking = BookingValidator.validateBookingCheckExists(
                bookingDao.getBookingById(id), id);
        return mapper.toDomainBooking(booking);
    }

    public Bookings getAllBookingsByFilter(Pageable pageable) {
        pageable.setPageSize(Objects.requireNonNullElse(pageable.getPageSize(), AppConfig.getDefaultPageSize()));
        pageable.setPageNumber(Objects.requireNonNullElse(pageable.getPageNumber(), AppConfig.getDefaultPageNumber()));
        var bookingEntity = bookingDao.getAllByFilter(pageable);
        return new Bookings(bookingEntity.list.stream()
                .map(mapper::toDomainBooking).toList());
    }

    @Transactional
    public Booking updateBookingById(UUID id, @Valid Booking booking) {
        var findBooking = BookingValidator.validateBookingCheckExists(
                bookingDao.getBookingByIdWithLock(id), id);

        var updateBooking = mapper.toEntity(findBooking, booking);
        BookingValidator.validateUpdateResult(bookingDao.updateBooking(updateBooking), id);
        BookingEntity savedBooking = BookingValidator.validateBookingExistsAfterUpdate(
                bookingDao.getBookingById(id), id);

        BookingEvent event = new BookingEvent(
                BookingEventType.BOOKING_UPDATED.name(),
                savedBooking.getId(),
                savedBooking.getIdProfile(),
                savedBooking,
                LocalDateTime.now()
        );
        kafkaTemplate.send(bookingEventsTopic, event);
        return mapper.toDomainBooking(savedBooking);
    }

    @Transactional
    public void deleteBookingById(UUID id) {
        BookingValidator.validateBookingCheckExists(bookingDao.getBookingByIdWithLock(id), id);
        BookingValidator.validateDeleteResult(bookingDao.delete(id), id);
        BookingEvent event = new BookingEvent(
                BookingEventType.BOOKING_DELETED.name(),
                id,
                null,
                null,
                LocalDateTime.now()
        );
        kafkaTemplate.send(bookingEventsTopic, event);
    }

    @Transactional
    public Booking updateBookingStatusById(UUID id, @Valid BookingStatus status) {
        BookingValidator.validateBookingCheckExists(bookingDao.getBookingByIdWithLock(id), id);
        BookingValidator.validateUpdateStatusResult(bookingDao.changeStatus(id, status), id);
        BookingEntity savedBooking = BookingValidator.validateBookingExistsAfterUpdate(
                bookingDao.getBookingById(id), id);
        BookingEventType bookingEvent = switch (status) {
            case BookingStatus.APPROVED -> BookingEventType.BOOKING_APPROVED;
            case BookingStatus.REJECTED -> BookingEventType.BOOKING_REJECTED;
            case BookingStatus.EXPIRED -> BookingEventType.BOOKING_EXPIRED;
            default -> throw new IllegalArgumentException("Unknown booking status: " + status);
        };
        BookingEvent event = new BookingEvent(
                bookingEvent.name(),
                id,
                savedBooking.getIdProfile(),
                savedBooking,
                LocalDateTime.now()
        );
        kafkaTemplate.send(bookingEventsTopic, event);
        return mapper.toDomainBooking(savedBooking);
    }
}
