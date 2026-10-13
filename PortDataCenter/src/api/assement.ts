import request from '@/utils/request'
import type {
    AssessmentSafetyItem,
    AssessmentPilotageMonthlyItem,
    AssessmentPilotageSummaryItem,
    AssessmentLengthGt250Item,
    AssessmentHolidayItem,
    AssessmentWeekendItem,
    AssessmentNightItem,
    AssessmentSpecialItem,
    AssessmentRecordBreakItem,
    AssessmentPlanChangeItem,
    AssessmentNewWharfItem
} from '@/types/assement'
import type {ApiResponse} from "@/types/common.ts";

const normalizeParams = (params?: any) => {
    if (!params) return undefined
    const p: any = { ...params }
    if (p.dateRange && Array.isArray(p.dateRange) && p.dateRange.length === 2) {
        p.startDate = p.dateRange[0]
        p.endDate = p.dateRange[1]
        delete p.dateRange
    }
    return p
}

export const fetchAssessmentSafety = (params?: { dateRange?: string[] }) => {
    return request<ApiResponse<AssessmentSafetyItem[]>>({
        url: '/api/statistics/safetyPilotage/safetyMonthly',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentPilotageMonthly = (params?: { dateRange?: string[], shipName?: string, pilot?: string }) => {
    return request<ApiResponse<AssessmentPilotageMonthlyItem[]>>({
        url: '/api/statistics/safetyPilotage/pilotageMonthly',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentPilotageSummary = (params?: { dateRange?: string[] }) => {
    return request<ApiResponse<AssessmentPilotageSummaryItem[]>>({
        url: '/api/statistics/safetyPilotage/pilotageSummary',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentLengthGt250 = (params?: { dateRange?: string[], minLength?: number }) => {
    return request<ApiResponse<AssessmentLengthGt250Item[]>>({
        url: '/api/statistics/safetyPilotage/lengthGt250',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentHoliday = (params?: { dateRange?: string[] }) => {
    return request<ApiResponse<AssessmentHolidayItem[]>>({
        url: '/api/statistics/holidayWeekend/holiday',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentWeekend = (params?: { dateRange?: string[] }) => {
    return request<ApiResponse<AssessmentWeekendItem[]>>({
        url: '/api/statistics/holidayWeekend/weekend',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentNight = (params?: { dateRange?: string[] }) => {
    return request<ApiResponse<AssessmentNightItem[]>>({
        url: '/api/statistics/holidayWeekend/night',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentMeal = (params?: { dateRange?: string[] }) => {
    return request<ApiResponse<any[]>>({
        url: '/api/statistics/holidayWeekend/meal',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentSpecial = (params?: { dateRange?: string[], shipName?: string }) => {
    return request<ApiResponse<AssessmentSpecialItem[]>>({
        url: '/api/statistics/holidayWeekend/special',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentRecordBreak = (params?: { dateRange?: string[] }) => {
    return request<ApiResponse<AssessmentRecordBreakItem[]>>({
        url: '/api/statistics/specialRecords/recordBreak',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentNewWharf = (params?: { dateRange?: string[] }) => {
    return request<ApiResponse<AssessmentNewWharfItem[]>>({
        url: '/api/statistics/specialRecords/newWharf',
        method: 'get',
        params: normalizeParams(params)
    })
}

export const fetchAssessmentPlanChange = (params?: { dateRange?: string[], status?: string }) => {
    return request<ApiResponse<AssessmentPlanChangeItem[]>>({
        url: '/api/statistics/specialRecords/planChange',
        method: 'get',
        params: normalizeParams(params)
    })
}