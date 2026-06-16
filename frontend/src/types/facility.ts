import type { FacilityStatus, BookingStatus } from '../constants/facility';
import type { User } from './user';

export interface Facility {
  id: number;
  name: string;
  description?: string;
  image?: string;
  location?: string;
  status: FacilityStatus;
  createdAt: string;
  updatedAt: string;
}

export interface FacilityTimeSlot {
  id: number;
  facilityId: number;
  startTime: string;
  endTime: string;
  capacity: number;
  weekday?: number | null;
  status: string;
  createdAt: string;
  updatedAt: string;
  bookedCount?: number;
  available?: boolean;
}

export interface FacilityBooking {
  id: number;
  userId: number;
  facilityId: number;
  slotId: number;
  bookingDate: string;
  status: BookingStatus;
  remark?: string;
  createdAt: string;
  updatedAt: string;
  user?: User;
  facility?: Facility;
  slot?: FacilityTimeSlot;
}

export interface FacilityPayload {
  name: string;
  description?: string;
  image?: string;
  location?: string;
  status?: FacilityStatus;
}

export interface FacilitySlotPayload {
  facilityId?: number;
  startTime: string;
  endTime: string;
  capacity: number;
  weekday?: number | null;
  status?: string;
}

export interface FacilityBookingPayload {
  facilityId: number;
  slotId: number;
  bookingDate: string;
  remark?: string;
}
