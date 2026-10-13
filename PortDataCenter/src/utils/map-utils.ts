import type { GeoCoordinate } from '@/types/common'

export const computeBoundingBox = (points: GeoCoordinate[]) => {
  const lngs = points.map((p) => p.lng)
  const lats = points.map((p) => p.lat)
  return {
    minLng: Math.min(...lngs),
    maxLng: Math.max(...lngs),
    minLat: Math.min(...lats),
    maxLat: Math.max(...lats),
  }
}

export const toPath = (points: GeoCoordinate[]) => points.map((item) => [item.lng, item.lat])

export const interpolatePath = (from: GeoCoordinate, to: GeoCoordinate, steps = 12): GeoCoordinate[] => {
  const result: GeoCoordinate[] = []
  for (let i = 0; i <= steps; i += 1) {
    const ratio = i / steps
    result.push({
      lng: from.lng + (to.lng - from.lng) * ratio,
      lat: from.lat + (to.lat - from.lat) * ratio,
    })
  }
  return result
}

export const offsetCoordinate = (coord: GeoCoordinate, lngOffset = 0, latOffset = 0): GeoCoordinate => ({
  lng: parseFloat((coord.lng + lngOffset).toFixed(6)),
  lat: parseFloat((coord.lat + latOffset).toFixed(6)),
})

