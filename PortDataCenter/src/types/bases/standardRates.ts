export interface StandardRates {
  id: number                     // 主键ID
  rateCode: string               // 标准费率编号
  categoryId: number             // 分类ID
  typeId: string                 // 类型编号
  effectiveDate: string          // 生效日期
  expiryDate: string             // 失效日期
  routeId: number                // 区间ID
  nauticalMiles: number          // 海里数
  notes: string                  // 备注
  applicableVesselMinNetTon: number  // 适用船舶最小净吨
  applicableVesselMaxNetTon: number  // 适用船舶最大净吨
  rateStatus: string             // 状态
  currencyUnit: string           // 货币单位
  createdBy: number             // 创建用户ID
  updatedBy: number             // 更新用户ID
  createdAt: string             // 创建时间
  updatedAt: string             // 更新时间
  ruleCount?: number            // 关联的费率规则数量
  conditionCount?: number        // 关联的条件数量
}

// 费率规则类型
export interface RateRuleTypes {
  id: number                    // 主键ID
  ruleTypeCode: string          // 规则类型编码
  ruleTypeName: string          // 规则类型名称
  calculationMethod: string      // 计算方式
  chargeItemId: number          // 关联的收费项目ID
  isSystem: number              // 是否系统内置
  sortOrder: number             // 排序
  description: string           // 描述
  createdAt: string             // 创建时间
  updatedAt: string             // 更新时间
}

// 费率规则
export interface RateRules {
  id: number                    // 主键ID
  standardRateId: number        // 关联原standard_rates表的ID
  ruleTypeId: number            // 费率规则类型ID
  rateValue: number             // 费率值
  applicableVesselMinNetTon: number  // 适用船舶最小净吨
  applicableVesselMaxNetTon: number  // 适用船舶最大净吨
  notes: string                 // 备注
  isActive: number              // 是否启用
  version: number               // 版本号
  createdAt: string             // 创建时间
  updatedAt: string             // 更新时间
}

export interface RateConditions {
  id: number                    // 主键ID
  standardRateId: number        // 关联原standard_rates表的ID
  conditionType: string         // 条件类型
  conditionOperator: string      // 条件运算符
  conditionValue: string        // 条件值
  description: string           // 条件描述
  createdAt: string             // 创建时间
  updatedAt: string             // 更新时间
}

// 标准费率信息及费率规则DTO
export interface StandardRatesWithRulesDTO {
  standardRate: StandardRates   // 标准费率信息
  rateRules: RateRules[]        // 费率规则列表
  ruleTypeMap: Record<number, RateRuleTypes>  // 费率规则类型映射
}