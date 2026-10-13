<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules, type UploadProps } from 'element-plus'
import { User, Lock, Phone, Message, OfficeBuilding, Plus, Loading } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { uploadAvatar, updateAvatar } from '@/api/user'
import { updateProfile, changePassword } from '@/api/auth'
import type { UploadRequestOptions } from 'element-plus'
import { getToken } from '@/utils/storage'
import { resolveAvatarUrl } from '@/utils/url'

const auth = useAuthStore()
const router = useRouter()

const ROLE_LABELS: Record<string, string> = {
  admin: '站领导',
  dispatcher: '调度人员',
  billing: '费收人员',
  pilot: '引航员',
  agent: '船舶代理',
  ADMIN: '站领导',
  DISPATCHER: '调度人员',
  BILLING: '费收人员',
  PILOT: '引航员',
  AGENT: '船舶代理',
}

const profileFormRef = ref<FormInstance>()
const profileForm = reactive({
  username: auth.user?.username || '',
  account: auth.user?.account || '',
  userCode: auth.user?.userCode || '',
  userType: auth.user?.userType || '',
  phone: auth.user?.phone || '',
  email: auth.user?.email || '',
  department: auth.user?.department || '',
  position: auth.user?.position || '',
  avatar: auth.user?.avatar || '',
})

const profileRules: FormRules = {
  username: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
}

const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度6-20个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const profileLoading = ref(false)
const passwordLoading = ref(false)
const avatarLoading = ref(false)
const avatarUrl = ref(auth.user?.avatar || '')

const formatDateTime = (value?: string) => {
  if (!value) return '未知'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString()
}

const roleLabel = computed(() => {
  return auth.user?.userType ? ROLE_LABELS[auth.user.userType] || String(auth.user.userType) : '未知'
})

const syncProfileForm = () => {
  profileForm.username = auth.user?.username || ''
  profileForm.account = auth.user?.account || ''
  profileForm.userCode = auth.user?.userCode || ''
  profileForm.userType = auth.user?.userType || ''
  profileForm.phone = auth.user?.phone || ''
  profileForm.email = auth.user?.email || ''
  profileForm.department = auth.user?.department || ''
  profileForm.position = auth.user?.position || ''
  profileForm.avatar = auth.user?.avatar || ''
  avatarUrl.value = resolveAvatarUrl(auth.user?.avatar) || avatarUrl.value
}

const ensureUserLoaded = async () => {
  const token = auth.token || getToken()
  if (!token) return
  if (!auth.user?.userCode || !auth.user?.department || !auth.user?.position) {
    await auth.restoreSession(token)
  }
  syncProfileForm()
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(rawFile.type)) {
    ElMessage.error('只支持JPEG、PNG、GIF格式的图片')
    return false
  }
  if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error('图片大小不能超过2MB')
    return false
  }
  return true
}

const handleAvatarUpload = async (options: UploadRequestOptions) => {
  avatarLoading.value = true
  try {
    const file = options.file as File
    const res = await uploadAvatar(file)

    if (res.code === 200 && res.data) {
      avatarUrl.value = resolveAvatarUrl(res.data)
      profileForm.avatar = res.data

      await updateAvatar(res.data)

      if (auth.user) {
        auth.updateUser({ avatar: res.data })
      }

      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  } finally {
    avatarLoading.value = false
  }
}

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) {
    ElMessage.warning('表单未就绪，请刷新后重试')
    return
  }

  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请完善资料信息后再提交')
    return
  }

  try {
    profileLoading.value = true

    const res = await updateProfile({
      username: profileForm.username,
      phone: profileForm.phone,
      email: profileForm.email,
      department: profileForm.department,
      position: profileForm.position,
      avatar: profileForm.avatar,
    })

    if (auth.user) {
      auth.updateUser({
        username: res.data?.username || profileForm.username,
        name: res.data?.username || profileForm.username,
        phone: res.data?.phone || profileForm.phone,
        email: res.data?.email || profileForm.email,
        department: res.data?.department || profileForm.department,
        position: res.data?.position || profileForm.position,
        avatar: res.data?.avatar || profileForm.avatar,
      })
    }

    ElMessage.success('个人信息更新成功')
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    profileLoading.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) {
    ElMessage.warning('表单未就绪，请刷新后重试')
    return
  }

  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) {
    ElMessage.warning('请填写完整的密码信息')
    return
  }

  try {
    passwordLoading.value = true

    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })

    ElMessage.success('密码修改成功，请重新登录')

    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value.resetFields()

    setTimeout(async () => {
      await auth.logout()
      await router.replace('/login')
    }, 1500)
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  ensureUserLoaded()
})

watch(
  () => auth.user,
  () => {
    syncProfileForm()
  },
  { immediate: true },
)
</script>

<template>
  <div class="profile-page page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">个人信息</h2>
        <p class="page-desc">查看和编辑个人账号信息</p>
      </div>
    </div>

    <div class="profile-content">
      <div class="profile-summary glass-card">
        <div class="summary-header">
          <div class="avatar-wrapper">
            <el-avatar
                v-if="avatarUrl"
                :src="avatarUrl"
                :size="86"
                class="avatar-image"
            />
            <el-avatar
                v-else
                :size="86"
                :style="{ backgroundColor: '#1890ff' }"
                class="avatar-placeholder"
            >
              {{ auth.user?.username?.slice(0, 1) }}
            </el-avatar>
          </div>
          <div class="summary-title">
            <h3>{{ auth.user?.username || '未命名用户' }}</h3>
            <el-tag type="primary">{{ roleLabel }}</el-tag>
          </div>
        </div>
        <div class="summary-list">
          <div class="info-item">
            <span class="label">账号</span>
            <span class="value">{{ auth.user?.account || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">用户编号</span>
            <span class="value">{{ auth.user?.userCode || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">部门</span>
            <span class="value">{{ auth.user?.department || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">岗位</span>
            <span class="value">{{ auth.user?.position || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号</span>
            <span class="value">{{ auth.user?.phone || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ auth.user?.email || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">最后登录</span>
            <span class="value">{{ formatDateTime(auth.user?.lastLoginAt) }}</span>
          </div>
          <div class="info-item">
            <span class="label">创建时间</span>
            <span class="value">{{ formatDateTime(auth.user?.createdAt) }}</span>
          </div>
        </div>
      </div>

      <div class="profile-panel glass-card">
        <el-tabs class="profile-tabs">
          <el-tab-pane label="编辑资料">
            <el-form
                ref="profileFormRef"
                :model="profileForm"
                :rules="profileRules"
                label-width="100px"
                class="profile-form"
            >
              <el-form-item label="头像">
                <el-upload
                    class="avatar-uploader"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="handleAvatarUpload"
                    accept="image/jpeg,image/png,image/gif"
                >
                  <el-avatar
                      v-if="avatarUrl"
                      :src="avatarUrl"
                      :size="64"
                      class="avatar-image"
                  />
                  <el-avatar
                      v-else
                      :size="64"
                      :style="{ backgroundColor: '#1890ff' }"
                      class="avatar-placeholder"
                  >
                    {{ auth.user?.username?.slice(0, 1) }}
                  </el-avatar>
                  <div class="avatar-overlay">
                    <el-icon v-if="avatarLoading"><Loading /></el-icon>
                    <el-icon v-else><Plus /></el-icon>
                    <span>{{ avatarLoading ? '上传中...' : '更换头像' }}</span>
                  </div>
                </el-upload>
              </el-form-item>
              <el-form-item label="姓名" prop="username">
                <el-input v-model="profileForm.username" :prefix-icon="User" placeholder="请输入姓名" />
              </el-form-item>
              <el-form-item label="账号">
                <el-input v-model="profileForm.account" disabled />
              </el-form-item>
              <el-form-item label="用户编号">
                <el-input v-model="profileForm.userCode" disabled />
              </el-form-item>
              <el-form-item label="部门" prop="department">
                <el-input v-model="profileForm.department" :prefix-icon="OfficeBuilding" placeholder="请输入部门" />
              </el-form-item>
              <el-form-item label="岗位" prop="position">
                <el-input v-model="profileForm.position" placeholder="请输入岗位" />
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" :prefix-icon="Phone" placeholder="请输入手机号" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" :prefix-icon="Message" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleUpdateProfile" :loading="profileLoading">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="修改密码">
            <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
                class="profile-form"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    :prefix-icon="Lock"
                    placeholder="请输入当前密码"
                    show-password
                    autocomplete="current-password"
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    :prefix-icon="Lock"
                    placeholder="请输入新密码（6-20位）"
                    show-password
                    autocomplete="new-password"
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    :prefix-icon="Lock"
                    placeholder="请再次输入新密码"
                    show-password
                    autocomplete="new-password"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
                  修改密码
                </el-button>
                <el-button @click="passwordFormRef?.resetFields()">重置</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  color: #1f2d3d;
}

.page-header {
  margin-bottom: 10px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2d3d;
}

.page-desc {
  margin: 8px 0 0;
  font-size: 14px;
  color: #64748b;
}

.profile-content {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 20px;

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
  }
}

.profile-summary,
.profile-panel {
  padding: 24px;
  border-radius: 18px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(13, 38, 76, 0.08);
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.summary-title {
  display: flex;
  flex-direction: column;
  gap: 8px;

  h3 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: #1f2d3d;
  }
}

.summary-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;

  .label {
    font-size: 13px;
    color: #6b7280;
    min-width: 80px;
  }

  .value {
    font-size: 14px;
    color: #1f2d3d;
    text-align: right;
    max-width: 200px;
    word-break: break-all;
  }
}

.avatar-wrapper {
  position: relative;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;

  .avatar-image,
  .avatar-placeholder {
    display: block;
    border: 2px solid rgba(143, 214, 255, 0.3);
    transition: all 0.3s ease;
  }

  .avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    opacity: 0;
    transition: opacity 0.3s ease;
    color: #ffffff;
    font-size: 12px;

    .el-icon {
      font-size: 22px;
      margin-bottom: 4px;
    }
  }

  &:hover {
    .avatar-image,
    .avatar-placeholder {
      border-color: #8fd6ff;
    }

    .avatar-overlay {
      opacity: 1;
    }
  }
}

.profile-form {
  max-width: 560px;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(16, 24, 40, 0.12);
}

.profile-tabs :deep(.el-tabs__item) {
  color: #334155;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: #0ea5e9;
}

.profile-panel :deep(.el-form-item__label) {
  color: #475569;
}

.profile-panel :deep(.el-input__inner) {
  color: #1f2d3d;
}

</style>
