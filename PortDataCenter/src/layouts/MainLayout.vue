<script setup lang="ts">
import type { Component } from 'vue'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as Icons from '@element-plus/icons-vue'
import { ArrowDown, User, SwitchButton } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useNow } from '@/utils/date-utils'
import { resolveAvatarUrl } from '@/utils/url'
import ParticleBackground from '@/components/common/ParticleBackground.vue'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const timeState = useNow()
const collapsed = ref(false)

const ArrowDownIcon = ArrowDown
const UserIcon = User
const SwitchButtonIcon = SwitchButton

interface MenuItem {
  path: string
  title: string
  icon?: Component
  group?: string
  role?: string[]
  perm?: string | string[]
  children?: MenuItem[]
}

const menuGroups = [
  { key: 'dashboard', label: '工作台', icon: 'HomeFilled' },
  { key: 'myTasks', label: '我的任务', icon: 'List' },
  { key: 'pilotage', label: '引航调度', icon: 'Guide' },
  { key: 'billing', label: '引航费收', icon: 'Coin' },
  { key: 'statistics', label: '费收统计', icon: 'DataLine' },
  { key: 'assessment', label: '考核统计', icon: 'TrendCharts' },
  { key: 'master', label: '基础数据', icon: 'Box' },
  { key: 'system', label: '系统管理', icon: 'Setting' },
]

const menuItems = computed(() => {
  const root = router.getRoutes().find((item) => item.path === '/' && item.children && item.children.length > 0)
  
  if (!root?.children) return []
  
  const items: MenuItem[] = root.children
    .filter((item) => !item.meta?.hidden)
    .map((item) => {
      let icon: Component | undefined = undefined
      const metaIcon = item.meta?.icon
      if (typeof metaIcon === 'string') {
        const iconComponent = Icons[metaIcon as keyof typeof Icons]
        icon = iconComponent ?? undefined
      } else if (metaIcon) {
        icon = metaIcon as Component
      }
      
      let path = item.path;
      if (!path.startsWith('/')) {
        path = '/' + path;
      }
      
      return {
        path,
        title: (item.meta?.title as string) ?? item.name ?? '',
        icon,
        group: (item.meta?.group as string) ?? 'other',
        role: item.meta?.role as string[] | undefined,
        perm: (item.meta as any)?.perm as string | string[] | undefined,
      }
    })
  
  const userRole = auth.user?.userType
  const userPerms = auth.user?.permissions || []
  return items.filter((item) => {
    if (item.path === '/dashboard') return true
    if (item.perm) {
      const required = Array.isArray(item.perm) ? item.perm : [item.perm]
      const has = userPerms.includes('*') || required.some((p) => userPerms.includes(p))
      if (!has) return false
    }
    // 没配 perm/role 的菜单项：默认仅管理员可见，避免出现“权限未配却到处可见”
    if (!item.perm && (!item.role || item.role.length === 0)) {
      return userPerms.includes('*')
    }
    if (!item.role || item.role.length === 0) return true
    return userRole && item.role.includes(userRole)
  })
})

const groupedMenus = computed(() => {
  return menuGroups.map((group) => ({
    ...group,
    items: menuItems.value.filter((item) => item.group === group.key),
  }))
})

const activePath = computed(() => route.path)

const handleSelect = (path: string) => {
  if (path !== route.path) {
    router.push(path)
  }
}

const handleLogout = async () => {
  await auth.logout()
  router.replace('/login')
}

const handleProfileClick = () => {
  router.push('/bases/profile')
}
</script>

<template>
  <el-container class="main-layout">
    <ParticleBackground :count="40" color="#00a8cc" line-color="rgba(0, 168, 204, 0.08)" />
    
    <el-aside :width="collapsed ? '64px' : '240px'" class="layout-aside">
      <div class="logo">
        <div class="logo-icon" v-if="collapsed">P</div>
        <div v-else class="logo__content">
          <strong>港口引航管理</strong>
          <small>Port Pilotage System</small>
        </div>
      </div>

      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activePath"
          class="menu"
          :collapse="collapsed"
          background-color="var(--tidal-bg-sidebar)"
          text-color="rgba(255,255,255,0.7)"
          active-text-color="#ffffff"
          @select="handleSelect"
        >
          <template v-for="group in groupedMenus" :key="group.key">
            <el-sub-menu v-if="group.items.length > 1 && !collapsed" :index="group.key">
              <template #title>
                <el-icon><component :is="Icons[group.icon as keyof typeof Icons]" /></el-icon>
                <span>{{ group.label }}</span>
              </template>
              <el-menu-item
                v-for="item in group.items"
                :index="item.path"
                :key="item.path"
              >
                <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            
            <template v-else-if="!collapsed">
              <el-menu-item
                v-for="item in group.items"
                :index="item.path"
                :key="item.path"
              >
                <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </el-menu-item>
              
              <el-menu-item
                v-if="group.key === 'dashboard' && group.items.length === 0"
                index="/dashboard"
              >
                <el-icon><component :is="Icons.HomeFilled" /></el-icon>
                <span>{{ group.label }}</span>
              </el-menu-item>
            </template>
            
            <el-menu-item
              v-if="collapsed && group.items.length >= 1"
              v-for="item in group.items"
              :index="item.path"
              :key="item.path + '_collapsed'"
            >
              <el-icon v-if="item.icon"><component :is="item.icon" /></el-icon>
              <template #title>
                <span>{{ item.title }}</span>
              </template>
            </el-menu-item>
            
            <!-- 折叠状态下特殊处理工作台 -->
            <el-menu-item
              v-if="collapsed && group.key === 'dashboard' && group.items.length === 0"
              index="/dashboard"
            >
              <el-icon><component :is="Icons.HomeFilled" /></el-icon>
              <template #title>
                <span>{{ group.label }}</span>
              </template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>

      <div class="collapse-toggle" @click="collapsed = !collapsed">
        <el-icon>
          <component :is="collapsed ? Icons.Expand : Icons.Fold" />
        </el-icon>
      </div>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <div class="header-breadcrumb">
            <span class="page-title">{{ route.meta.title || '工作台' }}</span>
          </div>
        </div>
        <div class="header-info">
          <div class="info-item">
            <span class="value digital-number">{{ timeState.time }}</span>
            <span class="label">{{ timeState.date }}</span>
          </div>
          <el-dropdown v-if="auth.user" @command="(cmd: string) => cmd === 'logout' ? handleLogout() : handleProfileClick()">
            <div class="info-user">
              <el-avatar :size="32" :src="resolveAvatarUrl(auth.user.avatar)" :style="{ backgroundColor: 'var(--tidal-primary)' }">
                {{ auth.user.name ? auth.user.name.slice(0, 1) : (auth.user.username ? auth.user.username.slice(0, 1) : 'U') }}
              </el-avatar>
              <div class="user-meta">
                <span class="name">{{ auth.user.name || auth.user.username }}</span>
              </div>
              <el-icon><ArrowDownIcon /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserIcon /></el-icon>
                  个人信息
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButtonIcon /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
  background-color: var(--tidal-bg-body);
}

.layout-aside {
  display: flex;
  flex-direction: column;
  background-color: var(--tidal-bg-sidebar);
  border-right: 1px solid var(--tidal-border);
  transition: width 0.3s ease;
  color: #fff;
  z-index: 10;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background-color: rgba(0, 0, 0, 0.1);

  &__content {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #fff;
    
    strong {
      font-size: 18px;
      font-weight: 600;
      letter-spacing: 1px;
    }
    
    small {
      font-size: 10px;
      opacity: 0.7;
      letter-spacing: 2px;
    }
  }
  
  .logo-icon {
    font-size: 24px;
    font-weight: bold;
    color: var(--tidal-accent);
  }
}

.menu-scrollbar {
  flex: 1;
}

.menu {
  border-right: none;
  
  :deep(.el-menu-item), :deep(.el-sub-menu__title) {
    &:hover {
      background-color: rgba(255, 255, 255, 0.05) !important;
    }
  }

  :deep(.el-menu-item.is-active) {
    background-color: var(--tidal-primary) !important;
    border-right: 4px solid var(--tidal-accent);
  }
}

.collapse-toggle {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.6);
  
  &:hover {
    color: #fff;
    background-color: rgba(255, 255, 255, 0.05);
  }
}

.layout-header {
  height: 64px;
  background-color: #fff;
  border-bottom: 1px solid var(--tidal-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  .page-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--tidal-text-main);
  }
}

.header-info {
  display: flex;
  align-items: center;
  gap: 24px;

  .info-item {
    text-align: right;
    display: flex;
    flex-direction: column;
    justify-content: center;
    
    .value {
      font-size: 16px;
      font-weight: 600;
      color: var(--tidal-text-main);
    }
    
    .label {
      font-size: 12px;
      color: var(--tidal-text-secondary);
    }
  }
}

.info-user {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px 8px;
  transition: background 0.2s;

  &:hover {
    background-color: var(--tidal-bg-body);
  }

  .user-meta {
    display: flex;
    flex-direction: column;
    
    .name {
      font-size: 14px;
      font-weight: 500;
      color: var(--tidal-text-main);
    }
  }
}

.layout-main {
  padding: 24px;
  overflow-y: auto;
  background-color: var(--tidal-bg-body);
}
</style>