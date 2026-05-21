package com.rent.hatabooking.booking.dao;

import com.rent.hatabooking.booking.dto.enums.BookingStatus;
import com.rent.hatabooking.booking.entity.BookingEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class BookingRowMapper implements RowMapper<BookingEntity> {
    @Override
    public BookingEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        var booking = new BookingEntity();
        booking.setId(UUID.fromString(rs.getString("id")));
        booking.setIdProfile(UUID.fromString(rs.getString("id_profile")));
        booking.setIdRoom(UUID.fromString(rs.getString("id_room")));
        booking.setIdRoomOwner(UUID.fromString(rs.getString("id_room_owner")));
        booking.setStartDate(rs.getDate("start_date"));
        booking.setDescription(rs.getString("description"));
        booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
        return booking;
    }
}
