export const FACILITY_STATUS = {
  ACTIVE: 'active',
  INACTIVE: 'inactive',
} as const;

export type FacilityStatus = (typeof FACILITY_STATUS)[keyof typeof FACILITY_STATUS];

export const FACILITY_STATUS_TEXT: Record<FacilityStatus, string> = {
  [FACILITY_STATUS.ACTIVE]: '启用',
  [FACILITY_STATUS.INACTIVE]: '停用',
};

export const FACILITY_STATUS_COLOR: Record<FacilityStatus, string> = {
  [FACILITY_STATUS.ACTIVE]: 'success',
  [FACILITY_STATUS.INACTIVE]: 'info',
};

export const BOOKING_STATUS = {
  BOOKED: 'booked',
  CANCELLED: 'cancelled',
  COMPLETED: 'completed',
} as const;

export type BookingStatus = (typeof BOOKING_STATUS)[keyof typeof BOOKING_STATUS];

export const BOOKING_STATUS_TEXT: Record<BookingStatus, string> = {
  [BOOKING_STATUS.BOOKED]: '已预约',
  [BOOKING_STATUS.CANCELLED]: '已取消',
  [BOOKING_STATUS.COMPLETED]: '已完成',
};

export const BOOKING_STATUS_COLOR: Record<BookingStatus, string> = {
  [BOOKING_STATUS.BOOKED]: 'success',
  [BOOKING_STATUS.CANCELLED]: 'info',
  [BOOKING_STATUS.COMPLETED]: 'primary',
};
