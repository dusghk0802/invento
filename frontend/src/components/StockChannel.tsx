import { useState } from 'react'
import { Badge } from './ui'
import { useGet } from '../lib/api'
import { useMe, type Role } from '../lib/auth'

export type Channel = 'ONLINE' | 'OFFLINE' // 온라인 = 창고 재고(온라인 주문 출고), 오프라인 = 매장 재고
export type StockLocation = { locationId: number; locationName: string; locationType: 'WAREHOUSE' | 'STORE'; active: boolean }

// 상품·증정품·포장재 화면 공통: 창고/매장 필터와 표에 나올 위치
// switchRoles = 필터로 창고/매장을 바꿀 수 있는 역할. 나머지는 매장 직원=매장, 그 외=창고 고정
export function useStockChannel(switchRoles: Role[]) {
  const me = useMe().data
  const isOwner = me?.role === 'OWNER'
  const canSwitch = !!me && switchRoles.includes(me.role)
  const [picked, setPicked] = useState<Channel | null>(null)
  const fixed: Channel = me?.role === 'STORE' ? 'OFFLINE' : 'ONLINE'
  const channel: Channel = canSwitch ? picked ?? fixed : fixed

  const locations = useGet<StockLocation[]>('/locations')

  // 같은 종류의 담당 위치가 있으면 그곳만 (매장 직원의 창고 표 = 창고 전체)
  const channelType = channel === 'ONLINE' ? 'WAREHOUSE' : 'STORE'
  const channelLocations = (locations.data ?? []).filter((l) =>
    l.active && l.locationType === channelType
    && (me?.location?.locationType !== channelType || me.location.locationId === l.locationId))
  // 재고 조정: 사장=전체, 출고 직원=창고, 매장·창고 직원=담당 위치만
  const adjustLocations = channelLocations.filter((l) =>
    isOwner || (me?.role === 'FULFILLMENT' ? l.locationType === 'WAREHOUSE' : me?.location?.locationId === l.locationId))

  const filter = canSwitch ? (
    <select className="control-plain" value={channel} onChange={(e) => setPicked(e.target.value as Channel)}>
      <option value="ONLINE">창고</option>
      <option value="OFFLINE">매장</option>
    </select>
  ) : (
    <Badge tone="primary">{channel === 'ONLINE' ? '창고' : '매장'}</Badge>
  )

  return { me, isOwner, channel, channelLocations, adjustLocations, filter }
}
