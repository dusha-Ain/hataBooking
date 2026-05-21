package com.rent.hatabooking.booking.dao;

import com.rent.hatabooking.booking.dto.IdfBooking;
import com.rent.hatabooking.booking.dto.enums.BookingStatus;
import com.rent.hatabooking.booking.entity.BookingEntity;
import com.rent.hatabooking.booking.entity.BookingsEntity;
import com.rent.hatabooking.booking.service.BookingValidator;
import com.rent.hatabooking.web.helpers.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BookingDao {
    private final NamedParameterJdbcTemplate template;
    private final BookingRowMapper rowMapper;

    public BookingDao(NamedParameterJdbcTemplate template,
                      BookingRowMapper rowMapper) {
        this.template = template;
        this.rowMapper = rowMapper;
    }

    public Optional<BookingEntity> getBookingById(UUID id) {
        String sql = "SELECT * FROM booking WHERE id=:id";
        List<BookingEntity> booking =
                template.query(sql, new MapSqlParameterSource("id", id), rowMapper);
        return BookingValidator.validateSearchResult(booking, id.toString());
    }

    public Optional<BookingEntity> getBookingByIdWithLock(UUID id) {
        String sql = "SELECT * FROM booking WHERE id=:id FOR UPDATE";
        List<BookingEntity> booking =
                template.query(sql, new MapSqlParameterSource("id", id), rowMapper);
        return BookingValidator.validateSearchResult(booking, id.toString());
    }

    public Optional<BookingEntity> getBookingByIdfBooking(IdfBooking id) {
        String sql = "SELECT * FROM booking WHERE id_profile=:idProfile AND id_room=:idRoom";
        var map = new MapSqlParameterSource()
                .addValue("idProfile", id.idProfile())
                .addValue("idRoom", id.idRoom());
        List<BookingEntity> booking =
                template.query(sql, map, rowMapper);
        return BookingValidator.validateSearchResult(booking, id.toString());
    }

    public Optional<BookingEntity> getBookingByIdfBookingWithLock(IdfBooking id) {
        String sql = "SELECT * FROM booking WHERE id_profile=:idProfile AND id_room=:idRoom FOR UPDATE";
        var map = new MapSqlParameterSource()
                .addValue("idProfile", id.idProfile())
                .addValue("idRoom", id.idRoom());
        List<BookingEntity> booking =
                template.query(sql, map, rowMapper);
        return BookingValidator.validateSearchResult(booking, id.toString());
    }

    public BookingsEntity getAllByFilter(Pageable filter) {
        String sql = "SELECT * FROM booking LIMIT :limit OFFSET :offset";
        var map = new MapSqlParameterSource()
                .addValue("limit", filter.getPageSize())
                .addValue("offset", filter.getPageSize() * (filter.getPageNumber() - 1));
        return new BookingsEntity(template.query(sql, map, rowMapper));
    }

    public Boolean createBooking(BookingEntity booking) {
        String sql = """
                INSERT INTO booking(id, id_profile, id_room, id_room_owner, start_date, description, status)
                VALUES(:id, :profile, :room, :owner, :startDate, :desc, :status)
                """;
        var map = new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("profile", booking.getIdProfile())
                .addValue("room", booking.getIdRoom())
                .addValue("owner", booking.getIdRoomOwner())
                .addValue("startDate", booking.getStartDate())
                .addValue("desc", booking.getDescription())
                .addValue("status", BookingStatus.PENDING.name());
        return 1 == template.update(sql, map);
    }

    public boolean updateBooking(BookingEntity booking) {
        String sql = """
                UPDATE booking
                SET start_date=:startDate, description=:desc
                WHERE id=:id;
                """;
        var map = new MapSqlParameterSource()
                .addValue("id", booking.getId())
                .addValue("startDate", booking.getStartDate())
                .addValue("desc", booking.getDescription());
        return 1 == template.update(sql, map);
    }

    public boolean delete(UUID id) {
        String sql = "DELETE FROM booking WHERE id=:id";
        return 1 == template.update(sql, new MapSqlParameterSource("id", id));
    }

    public boolean changeStatus(UUID id, BookingStatus status){
        String sql = """
                UPDATE booking
                SET status=:status
                WHERE id=:id;
                """;
        var map = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status.name());
        return 1 == template.update(sql, map);
    }
}
