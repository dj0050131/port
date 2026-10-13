export interface MonthlyStatisticsDTO {
  /**
   * 月份
   */
  month: number
  
  /**
   * 计费凭证数
   */
  billingVoucherCount: number
  
  /**
   * 月度应收总额
   */
  totalAmount: number
  
  /**
   * 月度实收总额
   */
  actualReceivedAmount: number
  
  /**
   * 月度欠款额
   */
  arrears: number
  
  /**
   * 年度累计应收
   */
  cumulativeTotalAmount: number
  
  /**
   * 年度累计实收
   */
  cumulativeActualReceivedAmount: number
  
  /**
   * 年度累计欠款
   */
  cumulativeArrears: number
}

export interface AnnualReportDTO {
  /**
   * 年份
   */
  year: number
  
  /**
   * 统计开始日期
   */
  startDate: string
  
  /**
   * 统计结束日期
   */
  endDate: string
  
  /**
   * 每月统计数据
   */
  monthlyStatistics: MonthlyStatisticsDTO[]
  
  /**
   * 年度应收总额
   */
  annualTotalAmount: number
  
  /**
   * 年度实收总额
   */
  annualActualReceivedAmount: number
  
  /**
   * 年度欠款总额
   */
  annualArrears: number
}

export interface MonthlyReportDTO {
  /**
   * 年份
   */
  year: number
  
  /**
   * 月份
   */
  month: number
  
  /**
   * 统计开始日期
   */
  startDate: string
  
  /**
   * 统计结束日期
   */
  endDate: string
  
  /**
   * 计费凭证数
   */
  billingVoucherCount: number
  
  /**
   * 船舶数
   */
  vesselCount: number
  
  /**
   * 代理公司数
   */
  agentCount: number
  
  /**
   * 应收总额
   */
  totalAmount: number
  
  /**
   * 实收总额
   */
  actualReceivedAmount: number
  
  /**
   * 折扣总额
   */
  discountAmount: number
  
  /**
   * 滞纳金总额
   */
  lateFee: number
  
  /**
   * 未付款单数
   */
  unpaidCount: number
  
  /**
   * 部分付款单数
   */
  partialPaidCount: number
  
  /**
   * 已付款单数
   */
  fullyPaidCount: number
  
  /**
   * 国际航线金额
   */
  internationalRouteAmount: number
  
  /**
   * 国内航线金额
   */
  domesticRouteAmount: number
}