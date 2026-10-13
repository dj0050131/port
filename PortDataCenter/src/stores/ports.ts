import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Port, PortArea, Wharves, Anchorages, Berths } from '@/types/bases/ports.ts'
import {
    fetchAnchoragesByPortAreaId, fetchBerthsByWharvesId,
    fetchPortAreasByPortId,
    fetchWharvesByPortAreaId,
    getPortsList
} from "@/api/bases/ports.ts";

export const usePortsStore = defineStore('ports', () => {
    const ports = ref<Port[]>([])
    const portAreas = ref<PortArea[]>([])
    const wharves = ref<Wharves[]>([])
    const anchorages = ref<Anchorages[]>([])
    const berths = ref<Berths[]>([])

    const loading = ref(false)

    const fetchPorts = async () => {
        loading.value = true
        try {
            const res = await getPortsList()
            ports.value = res.data.records || []
        } catch (e) {
            console.error('获取港口列表失败:', e)
        } finally {
            loading.value = false
        }
    }

    const fetchPortAreas = async (portId: number) => {
        loading.value = true
        try {
            const res = await fetchPortAreasByPortId(portId)
            portAreas.value = res.data || []
        } catch (e) {
            console.error('获取港区列表失败:', e)
            portAreas.value = []
        } finally {
            loading.value = false
        }
    }

    const fetchWharves = async (portAreaId: number) => {
        loading.value = true
        try {
            const res = await fetchWharvesByPortAreaId(portAreaId)
            wharves.value = res.data || []
        } catch (e) {
            console.error('获取码头列表失败:', e)
            wharves.value = []
        } finally {
            loading.value = false
        }
    }

    const fetchAnchorages = async (portAreaId: number) => {
        loading.value = true
        try {
            const res = await fetchAnchoragesByPortAreaId(portAreaId)
            anchorages.value = res.data || []
        } catch (e) {
            console.error('获取锚地列表失败:', e)
            anchorages.value = []
        } finally {
            loading.value = false
        }
    }

    const fetchBerths = async (wharvesId: number) => {
        loading.value = true
        try {
            const res = await fetchBerthsByWharvesId(wharvesId)
            berths.value = res.data || []
        } catch (e) {
            console.error('获取泊位列表失败:', e)
            berths.value = []
        } finally {
            loading.value = false
        }
    }

    return {
        ports,
        portAreas,
        wharves,
        anchorages,
        berths,
        loading,
        fetchPorts,
        fetchPortAreas,
        fetchWharves,
        fetchAnchorages,
        fetchBerths
    }
})