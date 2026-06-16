<script setup lang="ts">
import { computed } from 'vue';
import { House, Bell, CreditCard, DataAnalysis, User, Tools, Calendar, Setting } from '@element-plus/icons-vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from './stores/authStore';
import { USER_ROLE } from './constants/user';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const navItems = computed(() => {
  const items = [
    { path: '/dashboard', label: '工作台', icon: DataAnalysis },
    { path: '/repairs', label: '报修', icon: Tools },
    { path: '/payments', label: '缴费', icon: CreditCard },
    { path: '/announcements', label: '公告', icon: Bell },
  ];
  if (authStore.can('facility:view')) {
    items.push({ path: '/facilities', label: '设施预约', icon: Calendar });
  }
  if (authStore.can('facility:manage')) {
    items.push({ path: '/facility-manage', label: '设施管理', icon: Setting });
  }
  items.push({ path: '/profile', label: '我的', icon: User });
  return items;
});

const activePath = computed(() => route.path);
</script>

<template>
  <div class="app-shell">
    <aside class="side-nav">
      <div class="brand">
        <el-icon><House /></el-icon>
        <div>
          <strong>SmartEstate</strong>
          <span>智慧社区物业</span>
        </div>
      </div>
      <el-menu :default-active="activePath" router class="nav-menu">
        <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
      <div class="account-chip">
        <img :src="authStore.currentUser?.avatar" alt="" />
        <div>
          <strong>{{ authStore.currentUser?.nickname }}</strong>
          <span>{{ authStore.roleText }}</span>
        </div>
      </div>
    </aside>

    <main class="main-pane">
      <header class="topbar">
        <div>
          <span class="eyebrow">Community Operations</span>
          <h1>{{ route.meta.title || 'SmartEstate' }}</h1>
        </div>
        <el-button type="primary" plain @click="router.push('/profile')">账户与房产</el-button>
      </header>
      <router-view />
    </main>
  </div>
</template>
