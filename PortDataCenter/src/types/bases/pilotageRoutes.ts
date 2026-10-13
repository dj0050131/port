export interface PilotageRoutes {
  id: number                      // 主键ID
  routeCode: string               // 区间编号
  routeType?: string | null       // 区间类型
  routeName: string               // 区间名称
  routeNotes?: string | null      // 区间备注
  distanceNautical?: number | null         // 距离_海里
  defaultDuration?: number | null          // 默认航行时间（分钟）
  status?: string | null                  // 状态
  dangerLevel?: string | null             // 危险等级
  startLocationType?: string | null       // 起始位置类型
  startLocationId?: number | null         // 起始位置ID
  startLocationName?: string | null       // 起始位置名称（显示用）
  endLocationType?: string | null         // 结束位置类型
  endLocationId?: number | null           // 结束位置ID
  endLocationName?: string | null         // 结束位置名称（显示用）
  pilotageDirection?: string | null       // 引航方向
  applicableVesselMinNetTon?: number | null  // 适用船舶最小净吨
  applicableVesselMaxNetTon?: number | null  // 适用船舶最大净吨
  daylightOnly?: boolean | null           // 仅限白天
  tideDependent?: boolean | null          // 依赖潮汐
  createdAt?: string | null               // 创建时间
  updatedAt?: string | null               // 更新时间
}