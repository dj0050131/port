export type CalculationMethod = 'PER_NET_TON' | 'PER_NAUTICAL_MILE' | 'PER_HOUR' | 'FIXED' | 'PERCENTAGE'

export interface RateRuleTypes {
  id?: number                    // 主键ID
  ruleTypeCode: string           // 规则类型编码
  ruleTypeName: string           // 规则类型名称
  calculationMethod: CalculationMethod // 计算方式
  chargeItemId?: number          // 关联的收费项目ID
  isSystem: number               // 是否系统内置
  sortOrder: number              // 排序
  description: string            // 描述
  createdAt?: string             // 创建时间
  updatedAt?: string             // 更新时间
}