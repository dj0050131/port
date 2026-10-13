export interface PilotagePlan {
    id?: number
    planCode: string
    vesselName?: string
    shipName?: string
    englishName?: string
    nationality?: string
    planStatus: 'PLANNING' | 'PUBLISHED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED'
    planStartTime: string | Date
    planEndTime: string | Date
    startLocationName: string
    endLocationName: string
    pilotName?: string
    operationTypeFlag: 'INBOUND' | 'OUTBOUND' | 'SHIFT'
    priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'
}