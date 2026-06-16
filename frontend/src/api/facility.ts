import { request } from '../utils/request';
import type {
  Facility,
  FacilityTimeSlot,
  FacilityBooking,
  FacilityPayload,
  FacilitySlotPayload,
  FacilityBookingPayload,
} from '../types/facility';
import type { FacilityStatus, BookingStatus } from '../constants/facility';

export const listFacilities = (status?: FacilityStatus) =>
  request.get<never, Facility[]>('/facilities', { params: { status } });

export const getFacility = (id: number) =>
  request.get<never, Facility>(`/facilities/${id}`);

export const createFacility = (data: FacilityPayload) =>
  request.post<never, Facility>('/facilities', data);

export const updateFacility = (id: number, data: FacilityPayload) =>
  request.put<never, Facility>(`/facilities/${id}`, data);

export const deleteFacility = (id: number) =>
  request.delete<never, void>(`/facilities/${id}`);

export const listFacilitySlots = (facilityId: number, bookingDate?: string) =>
  request.get<never, FacilityTimeSlot[]>(`/facilities/${facilityId}/slots`, {
    params: { bookingDate },
  });

export const createFacilitySlot = (facilityId: number, data: FacilitySlotPayload) =>
  request.post<never, FacilityTimeSlot>(`/facilities/${facilityId}/slots`, data);

export const updateFacilitySlot = (facilityId: number, id: number, data: FacilitySlotPayload) =>
  request.put<never, FacilityTimeSlot>(`/facilities/${facilityId}/slots/${id}`, data);

export const deleteFacilitySlot = (facilityId: number, id: number) =>
  request.delete<never, void>(`/facilities/${facilityId}/slots/${id}`);

export const listBookings = (facilityId?: number, status?: BookingStatus) =>
  request.get<never, FacilityBooking[]>('/facility-bookings', {
    params: { facilityId, status },
  });

export const listMyBookings = () =>
  request.get<never, FacilityBooking[]>('/facility-bookings/my');

export const createBooking = (data: FacilityBookingPayload) =>
  request.post<never, FacilityBooking>('/facility-bookings', data);

export const cancelBooking = (id: number) =>
  request.put<never, FacilityBooking>(`/facility-bookings/${id}/cancel`);
