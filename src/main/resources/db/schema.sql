
CREATE TABLE rental (
                        id UUID PRIMARY KEY,
                        business_key TEXT NOT NULL UNIQUE,
                        status TEXT NOT NULL,
                        title TEXT NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

                        CONSTRAINT rental_status_known
                            CHECK (status IN (
                                              'BOOKED',
                                              'RENTED',
                                              'RETURNED'
                                ))
);
