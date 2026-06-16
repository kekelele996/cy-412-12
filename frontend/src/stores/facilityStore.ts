import { defineStore } from 'pinia';
import { ref } from 'vue';
import {
  listFacilities,
  getFacility,
  createFacility,
  updateFacility,
  deleteFacility,
  listFacilitySlots,
  createFacilitySlot,
  updateFacilitySlot,
  deleteFacilitySlot,
  listMyBookings,
  createBooking,
  cancelBooking,
  listBookings,
} from '../api/facility';
import type { Facility, FacilityTimeSlot, FacilityBooking, FacilityPayload, FacilitySlotPayload, FacilityBookingPayload } from '../types/facility';
import type { FacilityStatus, BookingStatus } from '../constants/facility';

export const useFacilityStore = defineStore('facility', () => {
  const facilities = ref<Facility[]>([]);
  const currentFacility = ref<Facility | null>(null);
  const slots = ref<FacilityTimeSlot[]>([]);
  const myBookings = ref<FacilityBooking[]>([]);
  const allBookings = ref<FacilityBooking[]>([]);
  const loading = ref(false);

  async function fetchFacilities(status?: FacilityStatus) {
    loading.value = true;
    try {
      facilities.value = await listFacilities(status);
    } finally {
      loading.value = false;
    }
  }

  async function fetchFacility(id: number) {
    loading.value = true;
    try {
      currentFacility.value = await getFacility(id);
    } finally {
      loading.value = false;
    }
  }

  async function addFacility(data: FacilityPayload) {
    const facility = await createFacility(data);
    facilities.value.unshift(facility);
    return facility;
  }

  async function editFacility(id: number, data: FacilityPayload) {
    const updated = await updateFacility(id, data);
    const index = facilities.value.findIndex((item) => item.id === id);
    if (index !== -1) {
      facilities.value[index] = updated;
    }
    if (currentFacility.value?.id === id) {
      currentFacility.value = updated;
    }
    return updated;
  }

  async function removeFacility(id: number) {
    await deleteFacility(id);
    facilities.value = facilities.value.filter((item) => item.id !== id);
  }

  async function fetchSlots(facilityId: number, bookingDate?: string) {
    loading.value = true;
    try {
      slots.value = await listFacilitySlots(facilityId, bookingDate);
    } finally {
      loading.value = false;
    }
  }

  async function addSlot(facilityId: number, data: FacilitySlotPayload) {
    const slot = await createFacilitySlot(facilityId, data);
    slots.value.push(slot);
    return slot;
  }

  async function editSlot(facilityId: number, id: number, data: FacilitySlotPayload) {
    const updated = await updateFacilitySlot(facilityId, id, data);
    const index = slots.value.findIndex((item) => item.id === id);
    if (index !== -1) {
      slots.value[index] = updated;
    }
    return updated;
  }

  async function removeSlot(facilityId: number, id: number) {
    await deleteFacilitySlot(facilityId, id);
    slots.value = slots.value.filter((item) => item.id !== id);
  }

  async function fetchMyBookings() {
    loading.value = true;
    try {
      myBookings.value = await listMyBookings();
    } finally {
      loading.value = false;
    }
  }

  async function fetchAllBookings(facilityId?: number, status?: BookingStatus) {
    loading.value = true;
    try {
      allBookings.value = await listBookings(facilityId, status);
    } finally {
      loading.value = false;
    }
  }

  async function bookFacility(data: FacilityBookingPayload) {
    const booking = await createBooking(data);
    myBookings.value.unshift(booking);
    return booking;
  }

  async function cancelBookingById(id: number) {
    const updated = await cancelBooking(id);
    const index = myBookings.value.findIndex((item) => item.id === id);
    if (index !== -1) {
      myBookings.value[index] = updated;
    }
    const allIndex = allBookings.value.findIndex((item) => item.id === id);
    if (allIndex !== -1) {
      allBookings.value[allIndex] = updated;
    }
    return updated;
  }

  return {
    facilities,
    currentFacility,
    slots,
    myBookings,
    allBookings,
    loading,
    fetchFacilities,
    fetchFacility,
    addFacility,
    editFacility,
    removeFacility,
    fetchSlots,
    addSlot,
    editSlot,
    removeSlot,
    fetchMyBookings,
    fetchAllBookings,
    bookFacility,
    cancelBookingById,
  };
});
