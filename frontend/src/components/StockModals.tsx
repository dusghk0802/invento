import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { FormModal, Input, Modal, Select, Table, type Column } from './ui'
import { api, queryClient, useGet } from '../lib/api'
import type { StockLocation } from './StockChannel'

const REASONS = { COUNT_DIFF: '실사 차이', DAMAGE: '파손', DEFECT: '불량', LOSS: '분실', TRANSFER_DIFF: '이동 수량 차이', ETC: '기타' }
const MOVEMENT = {
  PRODUCTION_IN: '공장 입고', PURCHASE_IN: '구매 입고', TRANSFER_OUT: '매장 출고', TRANSFER_IN: '매장 입고', SALE_OUT: '판매', GIFT_OUT: '증정',
  RETURN_IN: '반품 입고', DISPOSAL: '폐기', PACKAGING_OUT: '포장재 사용', ADJUST: '재고 조정',
}

type Movement = {
  stockMovementId: number; locationName: string | null; movementType: string; quantity: number
  adjustReason: string | null; memo: string | null; createdAt: string
}

type AdjustProps = {
  name: string
  stocks: { locationId: number; onHandQty: number; minimumQty: number }[]
  locations: StockLocation[]
  url: string // '/stocks/adjust' 또는 '/stocks/packaging/adjust' (최소 재고는 같은 경로의 /minimum)
  target: Record<string, number> // { productId: 1 } 또는 { packagingId: 1 }
  invalidate: string // 저장 후 다시 불러올 목록 (예: '/products')
  onClose: () => void
}

// 재고 조정 (상품·포장재 공통): 파손·분실·실사 차이 등을 사유와 함께 +/- 수량으로 기록 + 위치별 최소 재고 설정
// 최소 재고: 이하로 떨어지면 표에서 빨간색, 매장 출고 요청의 "부족 상품 불러오기" 대상
export function AdjustModal({ name, stocks, locations, url, target, invalidate, onClose }: AdjustProps) {
  const [form, setForm] = useState({ locationId: String(locations[0]?.locationId ?? ''), quantity: '', reason: 'COUNT_DIFF', memo: '' })
  const [minimum, setMinimum] = useState<string | null>(null) // null = 안 건드림 (위치를 바꾸면 초기화)
  const set = (patch: Partial<typeof form>) => setForm({ ...form, ...patch })
  const stock = stocks.find((s) => s.locationId === Number(form.locationId))
  const current = stock?.onHandQty ?? 0
  const currentMin = stock?.minimumQty ?? 0

  const save = useMutation({
    mutationFn: async () => {
      const locationId = Number(form.locationId)
      if (Number(form.quantity || 0) !== 0) {
        await api.post(url, { ...target, locationId, quantity: Number(form.quantity), reason: form.reason, memo: form.memo || null })
      }
      if (minimum !== null && Number(minimum || 0) !== currentMin) {
        await api.put(url.replace('/adjust', '/minimum'), { ...target, locationId, minimumQty: Number(minimum || 0) })
      }
    },
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: [invalidate] }); onClose() },
  })

  return (
    <FormModal title={`재고 조정 · ${name}`} onClose={onClose} onSubmit={() => save.mutate()} saving={save.isPending} error={save.error}>
      <Select label="위치" required value={form.locationId} onChange={(e) => { set({ locationId: e.target.value }); setMinimum(null) }}
              options={locations.map((l) => ({ value: l.locationId, label: l.locationName }))} />
      <Input label="증감 수량" type="number" placeholder="예: 파손 2개 → -2 (최소 재고만 바꾸면 비워두기)" value={form.quantity}
             onChange={(e) => set({ quantity: e.target.value })} />
      <p className="help-text">현재 보유 {current}개 → 조정 후 {current + Number(form.quantity || 0)}개</p>
      <Select label="사유" value={form.reason} onChange={(e) => set({ reason: e.target.value })}
              options={Object.entries(REASONS).map(([value, label]) => ({ value, label }))} />
      <Input label="메모" maxLength={500} value={form.memo} onChange={(e) => set({ memo: e.target.value })} />
      <Input label="최소 재고" type="number" min={0} placeholder="0이면 알림 없음" value={minimum ?? String(currentMin)}
             onChange={(e) => setMinimum(e.target.value)} />
    </FormModal>
  )
}

// 재고 변동 이력 (원장). params 예: { productId: 1 }, { packagingId: 3 }
export function HistoryModal({ name, params, onClose }: { name: string; params: Record<string, number>; onClose: () => void }) {
  const { data, isPending } = useGet<Movement[]>('/stocks/movements', params)

  const columns: Column<Movement>[] = [
    { key: 'createdAt', title: '일시', render: (m) => m.createdAt.replace('T', ' ').slice(0, 16) },
    { key: 'locationName', title: '위치' },
    { key: 'movementType', title: '구분', render: (m) => MOVEMENT[m.movementType as keyof typeof MOVEMENT] ?? m.movementType },
    { key: 'quantity', title: '수량', align: 'right', render: (m) => (m.quantity > 0 ? `+${m.quantity}` : m.quantity) },
    { key: 'memo', title: '사유·메모', render: (m) => [m.adjustReason && REASONS[m.adjustReason as keyof typeof REASONS], m.memo].filter(Boolean).join(' · ') },
  ]

  return (
    <Modal open width={720} title={`재고 이력 · ${name}`} onClose={onClose}>
      <Table columns={columns} rows={data} rowKey={(m) => m.stockMovementId} loading={isPending} emptyText="재고 변동 이력이 없습니다." />
    </Modal>
  )
}
