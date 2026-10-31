import { useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Button, Input, Modal, Select, Table, type Column } from './ui'
import { api, getErrorMessage, queryClient } from '../lib/api'
import type { StockLocation } from './StockChannel'

const REASONS = { COUNT_DIFF: '실사 차이', DAMAGE: '파손', DEFECT: '불량', LOSS: '분실', TRANSFER_DIFF: '이동 수량 차이', ETC: '기타' }
const MOVEMENT = {
  PRODUCTION_IN: '공장 입고', TRANSFER_OUT: '매장 출고', TRANSFER_IN: '매장 입고', SALE_OUT: '판매', GIFT_OUT: '증정',
  RETURN_IN: '반품 입고', DISPOSAL: '폐기', PACKAGING_OUT: '포장재 사용', ADJUST: '재고 조정',
}

type Movement = {
  stockMovementId: number; locationName: string | null; movementType: string; quantity: number
  adjustReason: string | null; memo: string | null; createdAt: string
}

type AdjustProps = {
  name: string
  stocks: { locationId: number; onHandQty: number }[]
  locations: StockLocation[]
  url: string // '/stocks/adjust' 또는 '/stocks/packaging/adjust'
  target: Record<string, number> // { productId: 1 } 또는 { packagingId: 1 }
  invalidate: string // 저장 후 다시 불러올 목록 (예: '/products')
  onClose: () => void
}

// 재고 조정 (상품·포장재 공통): 파손·분실·실사 차이 등을 사유와 함께 +/- 수량으로 기록
export function AdjustModal({ name, stocks, locations, url, target, invalidate, onClose }: AdjustProps) {
  const [form, setForm] = useState({ locationId: String(locations[0]?.locationId ?? ''), quantity: '', reason: 'COUNT_DIFF', memo: '' })
  const set = (patch: Partial<typeof form>) => setForm({ ...form, ...patch })
  const current = stocks.find((s) => s.locationId === Number(form.locationId))?.onHandQty ?? 0

  const save = useMutation({
    mutationFn: () => api.post(url, {
      ...target, locationId: Number(form.locationId), quantity: Number(form.quantity),
      reason: form.reason, memo: form.memo || null,
    }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: [invalidate] }); onClose() },
  })

  return (
    <Modal open title={`재고 조정 · ${name}`} onClose={onClose}
           footer={
             <>
               <Button variant="secondary" onClick={onClose}>취소</Button>
               <Button type="submit" form="adjust-form" disabled={save.isPending}>저장</Button>
             </>
           }>
      <form id="adjust-form" className="form-stack" onSubmit={(e) => { e.preventDefault(); save.mutate() }}>
        <Select label="위치" required value={form.locationId} onChange={(e) => set({ locationId: e.target.value })}
                options={locations.map((l) => ({ value: l.locationId, label: l.locationName }))} />
        <Input label="증감 수량" type="number" required placeholder="예: 파손 2개 → -2" value={form.quantity}
               onChange={(e) => set({ quantity: e.target.value })} />
        <p className="help-text">현재 보유 {current}개 → 조정 후 {current + Number(form.quantity || 0)}개</p>
        <Select label="사유" value={form.reason} onChange={(e) => set({ reason: e.target.value })}
                options={Object.entries(REASONS).map(([value, label]) => ({ value, label }))} />
        <Input label="메모" maxLength={500} value={form.memo} onChange={(e) => set({ memo: e.target.value })} />
        {save.isError && <p className="form-error">{getErrorMessage(save.error)}</p>}
      </form>
    </Modal>
  )
}

// 재고 변동 이력 (원장). query 예: 'productId=1', 'packagingId=3'
export function HistoryModal({ name, query, onClose }: { name: string; query: string; onClose: () => void }) {
  const url = `/stocks/movements?${query}`
  const { data, isPending } = useQuery({ queryKey: [url], queryFn: () => api.get<Movement[]>(url).then((r) => r.data) })

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
