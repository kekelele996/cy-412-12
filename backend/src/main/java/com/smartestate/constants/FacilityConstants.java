package com.smartestate.constants;

import java.util.Set;

public final class FacilityConstants {
    public static final String FACILITY_ACTIVE = "active";
    public static final String FACILITY_INACTIVE = "inactive";

    public static final String SLOT_ACTIVE = "active";
    public static final String SLOT_INACTIVE = "inactive";

    public static final String BOOKING_BOOKED = "booked";
    public static final String BOOKING_CANCELLED = "cancelled";
    public static final String BOOKING_COMPLETED = "completed";

    public static final Set<String> ALL_FACILITY_STATUSES = Set.of(FACILITY_ACTIVE, FACILITY_INACTIVE);
    public static final Set<String> ALL_BOOKING_STATUSES = Set.of(BOOKING_BOOKED, BOOKING_CANCELLED, BOOKING_COMPLETED);

    private FacilityConstants() {
    }
}
