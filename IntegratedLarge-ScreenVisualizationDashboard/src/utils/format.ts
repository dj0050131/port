export const formatCurrency = (value: number, digits = 2) => {
  return `￥${value.toFixed(digits).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
};

export const formatNumber = (value: number) => {
  if (value > 10000) {
    return `${(value / 10000).toFixed(1)} 万`;
  }
  return value.toLocaleString();
};

export const formatTime = (value: string) => {
  return new Date(value).toLocaleString();
};

