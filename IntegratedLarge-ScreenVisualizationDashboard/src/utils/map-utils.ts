import type { GeoCoordinate } from '@/types/dashboard'

const BASE_COORDINATE: GeoCoordinate = {
  lng: 113.881,
  lat: 22.483,
}

export const randomBetween = (min: number, max: number) => {
  return Math.random() * (max - min) + min
}

export const generateGeoPoint = (spread = 0.08): GeoCoordinate => {
  return {
    lng: Number((BASE_COORDINATE.lng + randomBetween(-spread, spread)).toFixed(4)),
    lat: Number((BASE_COORDINATE.lat + randomBetween(-spread, spread)).toFixed(4)),
  }
}

export const interpolateRoute = (start: GeoCoordinate, end: GeoCoordinate, segments = 4) => {
  const points: GeoCoordinate[] = []
  for (let i = 1; i < segments; i += 1) {
    const ratio = i / segments
    points.push({
      lng: Number((start.lng + (end.lng - start.lng) * ratio).toFixed(4)),
      lat: Number((start.lat + (end.lat - start.lat) * ratio).toFixed(4)),
    })
  }
  return points
}

export const getPilotRoute = (destination: GeoCoordinate): GeoCoordinate[] => {
  const entryPoint = {
    lng: BASE_COORDINATE.lng + randomBetween(-0.25, 0.25),
    lat: BASE_COORDINATE.lat + randomBetween(-0.25, 0.25),
  }

  return [entryPoint, ...interpolateRoute(entryPoint, destination, 5), destination]
}

export const withGlow = (color: string, blur = 12) => {
  return {
    shadowBlur: blur,
    shadowColor: color,
  }
}

export const randomEnumValue = <T extends readonly unknown[]>(values: T): T[number] => {
  const index = Math.floor(Math.random() * values.length)
  return values[index]
}

export const formatGeoLabel = (coord: GeoCoordinate) => `${coord.lng.toFixed(3)}, ${coord.lat.toFixed(3)}`
