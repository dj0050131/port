import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Agents } from '@/types/base'
import {fetchAgentsList} from "@/api/bases/agents.ts";

export const useBaseStore = defineStore('base', () => {
    const agents = ref<Agents[]>([])
    const loading = ref(false)

    const fetchAgents = async () => {
        loading.value = true
        try {
            const res = await fetchAgentsList()
            agents.value = res.data || []
        } catch (e) {
            console.error('获取代理公司列表失败:', e)
        } finally {
            loading.value = false
        }
    }

    return {
        agents,
        loading,
        fetchAgents
    }
})