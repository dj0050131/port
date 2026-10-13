import { defineStore } from 'pinia';

interface UserInfo {
  name: string;
  role: string;
  avatar?: string;
}

export const useUserStore = defineStore('user', {
  state: (): { user: UserInfo } => ({
    user: {
      name: '管理员',
      role: '港口调度管理员'
    }
  }),
  actions: {
    setUser(user: UserInfo) {
      this.user = user;
    }
  }
});

