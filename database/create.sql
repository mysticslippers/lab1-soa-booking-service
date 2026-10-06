CREATE TABLE BOOKINGS (
    id         BIGSERIAL PRIMARY KEY CONSTRAINT is_bookings_id_positive CHECK (id > 0),
    person_id  BIGINT NOT NULL CONSTRAINT is_person_id_positive CHECK (person_id > 0),
    ticket_id  BIGINT NOT NULL CONSTRAINT is_ticket_id_positive CHECK (ticket_id > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_bookings_person_ticket UNIQUE (person_id, ticket_id)
);

CREATE INDEX idx_bookings_ticket_id ON bookings (ticket_id);
