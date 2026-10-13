export interface RateCondition {
  id?: number
  standardRateId: number
  conditionType: string
  conditionOperator?: string
  conditionValue: string
  description?: string
  createdAt?: string
  updatedAt?: string
}
