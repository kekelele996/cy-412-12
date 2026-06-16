<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Calendar, Location } from '@element-plus/icons-vue';
import EmptyState from '../components/common/EmptyState.vue';
import PermissionButton from '../components/common/PermissionButton.vue';
import { useFacilityStore } from '../stores/facilityStore';
import { BOOKING_STATUS, BOOKING_STATUS_TEXT, BOOKING_STATUS_COLOR } from '../constants/facility';
import type { Facility, FacilityTimeSlot } from '../types/facility';
import { useAuthStore } from '../stores/authStore';
import { USER_ROLE } from '../constants/user';

const facilityStore = useFacilityStore();
const authStore = useAuthStore();

const activeTab = ref<'browse' | 'my'>('browse');
const selectedFacility = ref<Facility | null>(null);
const selectedDate = ref<string>(new Date().toISOString().split('T')[0]);
const selectedSlot = ref<FacilityTimeSlot | null>(null);
const bookingDialogVisible = ref(false);
const remark = ref('');

const isStaff = computed(() =>
  authStore.role === USER_ROLE.STAFF || authStore.role === USER_ROLE.ADMIN,
);

const myActiveBookings = computed(() =>
  facilityStore.myBookings.filter((b) => b.status === BOOKING_STATUS.BOOKED),
);

async function selectFacility(facility: Facility) {
  selectedFacility.value = facility;
  selectedSlot.value = null;
  await facilityStore.fetchSlots(facility.id, selectedDate.value);
}

async function onDateChange(date: string) {
  if (selectedFacility.value) {
    selectedSlot.value = null;
    await facilityStore.fetchSlots(selectedFacility.value.id, date);
  }
}

function openBookingDialog(slot: FacilityTimeSlot) {
  if (!slot.available) {
    ElMessage.warning('该时段已约满，请选择其他时段');
    return;
  }
  selectedSlot.value = slot;
  remark.value = '';
  bookingDialogVisible.value = true;
}

async function confirmBooking() {
  if (!selectedFacility.value || !selectedSlot.value) return;
  try {
    await facilityStore.bookFacility({
      facilityId: selectedFacility.value.id,
      slotId: selectedSlot.value.id,
      bookingDate: selectedDate.value,
      remark: remark.value,
    });
    ElMessage.success('预约成功');
    bookingDialogVisible.value = false;
    await facilityStore.fetchSlots(selectedFacility.value.id, selectedDate.value);
    await facilityStore.fetchMyBookings();
  } catch (e) {
    ElMessage.error('预约失败，请稍后重试');
  }
}

async function cancelBooking(id: number) {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '取消预约', {
      type: 'warning',
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
    });
    await facilityStore.cancelBookingById(id);
    ElMessage.success('已取消预约');
    if (selectedFacility.value) {
      await facilityStore.fetchSlots(selectedFacility.value.id, selectedDate.value);
    }
  } catch {
  }
}

function formatTime(time: string) {
  return time.substring(0, 5);
}

function getWeekdayText(dateStr: string) {
  const date = new Date(dateStr);
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  return weekdays[date.getDay()];
}

onMounted(async () => {
  await Promise.all([
    facilityStore.fetchFacilities(),
    facilityStore.fetchMyBookings(),
  ]);
});
</script>

<template>
  <section class="section-panel">
    <div class="section-title">
      <h2>公共设施预约</h2>
    </div>

    <el-tabs v-model="activeTab" class="facility-tabs">
      <el-tab-pane label="预约场地" name="browse">
        <div class="facility-layout">
          <div class="facility-list">
            <div v-if="facilityStore.facilities.length" class="facility-cards">
              <div
                v-for="facility in facilityStore.facilities"
                :key="facility.id"
                class="facility-card"
                :class="{ active: selectedFacility?.id === facility.id }"
                @click="selectFacility(facility)"
              >
                <div class="facility-image">
                  <img v-if="facility.image" :src="facility.image" :alt="facility.name" />
                  <div v-else class="facility-placeholder">
                    <el-icon :size="32"><Calendar /></el-icon>
                  </div>
                </div>
                <div class="facility-info">
                  <h3>{{ facility.name }}</h3>
                  <p class="facility-location">
                    <el-icon><Location /></el-icon>
                    <span>{{ facility.location || '位置待定' }}</span>
                  </p>
                  <p v-if="facility.description" class="facility-desc">
                    {{ facility.description }}
                  </p>
                </div>
              </div>
            </div>
            <EmptyState v-else title="暂无可用设施" description="请联系物业添加设施" />
          </div>

          <div class="slot-panel">
            <div v-if="selectedFacility" class="slot-content">
              <div class="slot-header">
                <h3>{{ selectedFacility.name }} - 选择时段</h3>
                <el-date-picker
                  v-model="selectedDate"
                  type="date"
                  :disabled-date="(d: Date) => d.getTime() < Date.now() - 86400000"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  @change="onDateChange"
                />
              </div>
              <div class="slot-date-info">
                {{ selectedDate }} {{ getWeekdayText(selectedDate) }}
              </div>
              <div v-if="facilityStore.slots.length" class="slot-grid">
                <div
                  v-for="slot in facilityStore.slots"
                  :key="slot.id"
                  class="slot-item"
                  :class="{
                    available: slot.available,
                    full: !slot.available,
                    selected: selectedSlot?.id === slot.id,
                  }"
                  @click="openBookingDialog(slot)"
                >
                  <div class="slot-time">
                    {{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}
                  </div>
                  <div class="slot-capacity">
                    <span v-if="slot.available" class="available-text">
                      剩余 {{ (slot.capacity || 0) - (slot.bookedCount || 0) }} / {{ slot.capacity }}
                    </span>
                    <span v-else class="full-text">已约满</span>
                  </div>
                </div>
              </div>
              <EmptyState v-else title="暂无可用时段" description="请选择其他日期或设施" />
            </div>
            <div v-else class="slot-empty">
              <el-icon :size="48" color="#b9c5b3"><Calendar /></el-icon>
              <p>请从左侧选择一个设施</p>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane :label="`我的预约 (${myActiveBookings.length})`" name="my">
        <div v-if="facilityStore.myBookings.length" class="booking-list">
          <div
            v-for="booking in facilityStore.myBookings"
            :key="booking.id"
            class="booking-card"
          >
            <div class="booking-info">
              <h4>{{ booking.facility?.name || '设施' }}</h4>
              <p class="booking-date">
                <el-icon><Calendar /></el-icon>
                <span>
                  {{ booking.bookingDate }}
                  <template v-if="booking.slot">
                    {{ formatTime(booking.slot.startTime) }} - {{ formatTime(booking.slot.endTime) }}
                  </template>
                </span>
              </p>
              <p v-if="booking.remark" class="booking-remark">备注：{{ booking.remark }}</p>
            </div>
            <div class="booking-action">
              <el-tag :type="BOOKING_STATUS_COLOR[booking.status] as any" size="small">
                {{ BOOKING_STATUS_TEXT[booking.status] }}
              </el-tag>
              <el-button
                v-if="booking.status === 'booked'"
                type="danger"
                size="small"
                text
                @click="cancelBooking(booking.id)"
              >
                取消预约
              </el-button>
            </div>
          </div>
        </div>
        <EmptyState v-else title="暂无预约记录" description="去预约场地吧" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="bookingDialogVisible" title="确认预约" width="400px">
      <div v-if="selectedFacility && selectedSlot" class="booking-confirm">
        <div class="confirm-row">
          <span class="label">设施名称</span>
          <span class="value">{{ selectedFacility.name }}</span>
        </div>
        <div class="confirm-row">
          <span class="label">预约日期</span>
          <span class="value">{{ selectedDate }} {{ getWeekdayText(selectedDate) }}</span>
        </div>
        <div class="confirm-row">
          <span class="label">时段</span>
          <span class="value">
            {{ formatTime(selectedSlot.startTime) }} - {{ formatTime(selectedSlot.endTime) }}
          </span>
        </div>
        <div class="confirm-row">
          <span class="label">可容纳</span>
          <span class="value">{{ selectedSlot.capacity }} 人</span>
        </div>
        <el-form label-position="top">
          <el-form-item label="备注（选填）">
            <el-input v-model="remark" type="textarea" :rows="2" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="bookingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBooking">确认预约</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.facility-tabs {
  margin-top: 16px;
}

.facility-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  min-height: 500px;
}

.facility-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.facility-card {
  display: flex;
  gap: 14px;
  padding: 14px;
  border: 1px solid #dfe7d8;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.facility-card:hover {
  border-color: #45624f;
  box-shadow: 0 4px 12px rgba(69, 98, 79, 0.1);
}

.facility-card.active {
  border-color: #45624f;
  background: #f5f9f0;
}

.facility-image {
  width: 100px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #eef3e8;
}

.facility-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.facility-placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: #95a08d;
}

.facility-info {
  flex: 1;
  min-width: 0;
}

.facility-info h3 {
  margin: 0 0 6px 0;
  font-size: 15px;
  color: #2c3e35;
}

.facility-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6e7b68;
  margin: 0 0 4px 0;
}

.facility-desc {
  font-size: 12px;
  color: #8a9584;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.slot-panel {
  border: 1px solid #dfe7d8;
  border-radius: 10px;
  padding: 18px;
  background: #fafbf8;
}

.slot-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.slot-header h3 {
  margin: 0;
  font-size: 16px;
  color: #2c3e35;
}

.slot-date-info {
  font-size: 13px;
  color: #6e7b68;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #dfe7d8;
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.slot-item {
  padding: 12px;
  border: 1px solid #dfe7d8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  text-align: center;
}

.slot-item.available:hover {
  border-color: #45624f;
  background: #f5f9f0;
}

.slot-item.full {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f5f5f5;
}

.slot-item.selected {
  border-color: #45624f;
  background: #eef8de;
}

.slot-time {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e35;
  margin-bottom: 4px;
}

.slot-capacity {
  font-size: 12px;
}

.available-text {
  color: #45624f;
}

.full-text {
  color: #999;
}

.slot-empty {
  height: 100%;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 12px;
  color: #95a08d;
  min-height: 300px;
}

.booking-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.booking-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px;
  border: 1px solid #dfe7d8;
  border-radius: 8px;
  background: #fff;
}

.booking-info h4 {
  margin: 0 0 6px 0;
  font-size: 15px;
  color: #2c3e35;
}

.booking-date {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6e7b68;
  margin: 0 0 4px 0;
}

.booking-remark {
  font-size: 12px;
  color: #8a9584;
  margin: 0;
}

.booking-action {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.booking-confirm .confirm-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.booking-confirm .label {
  color: #6e7b68;
}

.booking-confirm .value {
  color: #2c3e35;
  font-weight: 500;
}
</style>
