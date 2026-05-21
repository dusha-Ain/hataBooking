--liquibase formatted sql

--changeset create:1
CREATE TABLE IF NOT EXISTS booking
(
    id            UUID PRIMARY KEY,
    id_profile    UUID NOT NULL,
    id_room       UUID NOT NULL,
    id_room_owner UUID NOT NULL,
    start_date    Date NOT NULL,
    description   VARCHAR(250),
    status        VARCHAR(20) CHECK ( status IN ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED'))
);