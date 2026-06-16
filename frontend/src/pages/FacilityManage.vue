<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Edit, Delete, Plus, Setting } from '@element-plus/icons-vue';
import EmptyState from '../components/common/EmptyState.vue';
import PermissionButton from '../components/common/PermissionButton.vue';
import { useFacilityStore } from '../stores/facilityStore';
import { FACILITY_STATUS_TEXT, FACILITY_STATUS_COLOR } from '../constants/facility';
import type { Facility, FacilityTimeSlot, FacilityPayload, FacilitySlotPayload } from '../types/facility';

const facilityStore = useFacilityStore();

const facilityDialogVisible = ref(false);
const slotDialogVisible = ref(false);
const editingFacility = ref<Facility | null>(null);
const editingSlot = ref<FacilityTimeSlot | null>(null);
const currentFacility = ref<Facility | null>(null);

const facilityForm = reactive<FacilityPayload>({
  name: '',
  description: '',
  image: '',
  location: '',
  status: 'active',
});

const slotForm = reactive<FacilitySlotPayload>({
  startTime: '09:00',
  endTime: '11:00',
  capacity: 10,
  weekday: null,
  status: 'active',
});

const weekdayOptions = [
  { label: '每天', value: null },
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 },
];

function openFacilityDialog(facility?: Facility) {
  if (facility) {
    editingFacility.value = facility;
    Object.assign(facilityForm, {
      name: facility.name,
      description: facility.description || '',
      image: facility.image || '',
      location: facility.location || '',
      status: facility.status,
    });
  } else {
    editingFacility.value = null;
    Object.assign(facilityForm, {
      name: '',
      description: '',
      image: '',
      location: '',
      status: 'active',
    });
  }
  facilityDialogVisible.value = true;
}

async function saveFacility() {
  if (!facilityForm.name) {
    ElMessage.warning('请输入设施名称');
    return;
  }
  try {
    if (editingFacility.value) {
      await facilityStore.editFacility(editingFacility.value.id, facilityForm);
      ElMessage.success('设施已更新');
    } else {
      await facilityStore.addFacility(facilityForm);
      ElMessage.success('设施已添加');
    }
    facilityDialogVisible.value = false;
  } catch (e) {
    ElMessage.error('保存失败，请稍后重试');
  }
}

async function deleteFacility(facility: Facility) {
  try {
    await ElMessageBox.confirm(`确定要删除「${facility.name}」吗？`, '删除设施', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
    });
    await facilityStore.removeFacility(facility.id);
    if (currentFacility.value?.id === facility.id) {
      currentFacility.value = null;
    }
    ElMessage.success('设施已删除');
  } catch {
  }
}

async function selectFacility(facility: Facility) {
  currentFacility.value = facility;
  await facilityStore.fetchSlots(facility.id);
}

function openSlotDialog(slot?: FacilityTimeSlot) {
  if (slot) {
    editingSlot.value = slot;
    Object.assign(slotForm, {
      startTime: slot.startTime.substring(0, 5),
      endTime: slot.endTime.substring(0, 5),
      capacity: slot.capacity,
      weekday: slot.weekday ?? null,
      status: slot.status,
    });
  } else {
    editingSlot.value = null;
    Object.assign(slotForm, {
      startTime: '09:00',
      endTime: '11:00',
      capacity: 10,
      weekday: null,
      status: 'active',
    });
  }
  slotDialogVisible.value = true;
}

async function saveSlot() {
  if (!currentFacility.value) return;
  if (!slotForm.startTime || !slotForm.endTime) {
    ElMessage.warning('请选择开始和结束时间');
    return;
  }
  if (slotForm.capacity == null || slotForm.capacity < 1) {
    ElMessage.warning('容量必须大于 0');
    return;
  }
  try {
    if (editingSlot.value) {
      await facilityStore.editSlot(currentFacility.value.id, editingSlot.value.id, slotForm);
      ElMessage.success('时段已更新');
    } else {
      await facilityStore.addSlot(currentFacility.value.id, slotForm);
      ElMessage.success('时段已添加');
    }
    slotDialogVisible.value = false;
  } catch (e) {
    ElMessage.error('保存失败，请稍后重试');
  }
}

async function deleteSlot(slot: FacilityTimeSlot) {
  if (!currentFacility.value) return;
  try {
    await ElMessageBox.confirm(
      `确定要删除时段 ${slot.startTime.substring(0, 5)}-${slot.endTime.substring(0, 5)} 吗？`,
      '删除时段',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
      },
    );
    await facilityStore.removeSlot(currentFacility.value.id, slot.id);
    ElMessage.success('时段已删除');
  } catch {
  }
}

function formatTime(time: string) {
  return time.substring(0, 5);
}

function getWeekdayText(weekday: number | null | undefined) {
  if (weekday == null) return '每天';
  const map: Record<number, string> = {
    1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日',
  };
  return map[weekday] || '每天';
}

onMounted(async () => {
  await facilityStore.fetchFacilities();
});
</script>

<template>
  <div class="page-grid two-col">
    <section class="section-panel">
      <div class="section-title">
        <h2>设施管理</h2>
        <PermissionButton permission="facility:manage" type="primary" :icon="Plus" @click="openFacility()">
          新增设施
        </PermissionButton>
      </div>

      <div v-if="facilityStore.facilities.length" class="facility-manage-list">
        <div
          v-for="facility in facilityStore.facilities"
          :key="facility.id"
          class="facility-manage-item"
          :class="{ active: currentFacility?.id === facility.id }"
          @click="selectFacility(facility)"
        >
          <div class="facility-manage-info">
            <h4>{{ facility.name }}</h4>
            <p class="facility-manage-meta">
              <el-tag :type="FACILITY_STATUS_COLOR[facility.status] as any" size="small">
                {{ FACILITY_STATUS_TEXT[facility.status] }}
              </el-tag>
              <span class="location">{{ facility.location || '位置待定' }}</span>
            </p>
          </div>
          <div class="facility-manage-actions">
            <el-button type="primary" link :icon="Edit" @click.stop="openFacilityDialog(facility)">
              编辑
            </el-button>
            <el-button type="danger" link :icon="Delete" @click.stop="deleteFacility(facility)">
              删除
            </el-button>
          </div>
        </div>
      </div>
      <EmptyState v-else title="暂无设施" description="点击右上角添加设施" />
    </section>

    <section class="section-panel">
      <div class="section-title">
        <h2>时段管理</h2>
        <PermissionButton
          permission="facility:slotManage"
          type="primary"
          :icon="Plus"
          :disabled="!currentFacility"
          @click="openSlotDialog()"
        >
          新增时段
        </PermissionButton>
      </div>

      <div v-if="currentFacility">
        <p class="current-facility-tip">当前设施：<strong>{{ currentFacility.name }}</strong></p>

        <div v-if="facilityStore.slots.length" class="slot-manage-list">
          <div v-for="slot in facilityStore.slots" :key="slot.id" class="slot-manage-item">
            <div class="slot-manage-info">
              <span class="slot-time">
                {{ formatTime(slot.startTime) }} - {{ formatTime(slot.endTime) }}
              </span>
              <span class="slot-weekday">{{ getWeekdayText(slot.weekday) }}</span>
              <span class="slot-capacity">容量：{{ slot.capacity }} 人</span>
              <el-tag size="small" :type="slot.status === 'active' ? 'success' : 'info'">
                {{ slot.status === 'active' ? '启用' : '停用' }}
              </el-tag>
            </div>
            <div class="slot-manage-actions">
              <el-button type="primary" link :icon="Edit" @click="openSlotDialog(slot)">
                编辑
              </el-button>
              <el-button type="danger" link :icon="Delete" @click="deleteSlot(slot)">
                删除
              </el-button>
            </div>
          </div>
        </div>
        <EmptyState v-else title="暂无时段" description="点击右上角添加时段" />
      </div>
      <div v-else class="slot-empty-tip">
        <el-icon :size="40" color="#b9c5b3"><Setting /></el-icon>
        <p>请从左侧选择一个设施管理时段</p>
      </div>
    </section>
  </div>

  <el-dialog v-model="facilityDialogVisible" :title="editingFacility ? '编辑设施' : '新增设施'" width="480px">
    <el-form label-position="top">
      <el-form-item label="设施名称">
        <el-input v-model="facilityForm.name" maxlength="80" placeholder="请输入设施名称" />
      </el-form-item>
      <el-form-item label="位置">
        <el-input v-model="facilityForm.location" maxlength="160" placeholder="请输入位置信息" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input
          v-model="facilityForm.description"
          type="textarea"
          :rows="3"
          maxlength="500"
          placeholder="请输入设施描述"
        />
      </el-form-item>
      <el-form-item label="图片链接">
        <el-input v-model="facilityForm.image" placeholder="请输入图片URL（选填）" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="facilityForm.status">
          <el-radio value="active">启用</el-radio>
          <el-radio value="inactive">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="facilityDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveFacility">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="slotDialogVisible" :title="editingSlot ? '编辑时段' : '新增时段'" width="420px">
    <el-form label-position="top">
      <el-form-item label="适用日期">
        <el-select v-model="slotForm.weekday" style="width: 100%">
          <el-option
            v-for="opt in weekdayOptions"
            :key="opt.value ?? 'all'"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间">
        <el-time-picker
          v-model="slotForm.startTime"
          format="HH:mm"
          value-format="HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-time-picker
          v-model="slotForm.endTime"
          format="HH:mm"
          value-format="HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="可容纳人数">
        <el-input-number v-model="slotForm.capacity" :min="1" :max="999" style="width: 100%" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="slotForm.status">
          <el-radio value="active">启用</el-radio>
          <el-radio value="inactive">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="slotDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveSlot">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.facility-manage-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.facility-manage-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border: 1px solid #dfe7d8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.facility-manage-item:hover {
  border-color: #45624f;
}

.facility-manage-item.active {
  border-color: #45624f;
  background: #f5f9f0;
}

.facility-manage-info h4 {
  margin: 0 0 6px 0;
  font-size: 15px;
  color: #2c3e35;
}

.facility-manage-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #6e7b68;
  margin: 0;
}

.facility-manage-actions {
  display: flex;
  gap: 4px;
}

.current-facility-tip {
  font-size: 13px;
  color: #6e7b68;
  padding: 8px 12px;
  background: #eef3e8;
  border-radius: 6px;
  margin-bottom: 14px;
}

.current-facility-tip strong {
  color: #2c3e35;
}

.slot-manage-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.slot-manage-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border: 1px solid #dfe7d8;
  border-radius: 8px;
  background: #fff;
}

.slot-manage-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.slot-time {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e35;
}

.slot-weekday {
  font-size: 12px;
  color: #6e7b68;
  padding: 2px 8px;
  background: #f0f4ec;
  border-radius: 4px;
}

.slot-capacity {
  font-size: 12px;
  color: #8a9584;
}

.slot-manage-actions {
  display: flex;
  gap: 4px;
}

.slot-empty-tip {
  height: 200px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: #95a08d;
}
</style>
