export interface AgentInvoiceInfo {
  id: number              // 主键ID
  agentId: number         // 代理公司ID
  voyageId: number        // 航次ID
  agentName: string       // 代理名称
  payingUnitName: string  // 付款单位名称
  invoiceType: string     // 发票类型
  taxpayerId: string      // 纳税人识别号
  payingUnitAddress: string  // 付款单位地址
  payingUnitPhone: string    // 付款单位电话
  payingUnitBank: string     // 付款单位开户银行
  payingUnitBankAccount: string  // 付款单位银行账号
  billingVoucherId: number   // 关联计费凭证ID
  createdAt: string          // 创建时间
  updatedAt: string          // 更新时间
}