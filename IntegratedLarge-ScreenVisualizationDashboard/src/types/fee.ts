export interface FeeRateItem {
  id: string;
  shipType: string;
  baseRate: number;
  holidayExtra: number;
  remark?: string;
}

export interface FeeCalculatePayload {
  visaNo: string;
  shipNetTonnage: number;
}

export interface FeeCalculateResult {
  visaNo: string;
  totalFee: number;
  breakdown: {
    base: number;
    holiday: number;
    discount: number;
  };
}

