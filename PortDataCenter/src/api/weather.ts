import request from '@/utils/request'
import type { WeatherTidePayload } from '@/types/weather'

export const fetchWeatherTide = (portCode = 'WNZ') => {
  return request<WeatherTidePayload>({
    url: '/weather/tide',
    method: 'get',
    params: { portCode },
  })
}
