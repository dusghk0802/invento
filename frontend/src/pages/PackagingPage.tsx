import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useStockChannel, type StockLocation } from '../components/StockChannel'
import { AdjustModal, HistoryModal } from '../components/StockModals'
import { Badge, Button, FormModal, Input, PageHeader, Select, Table, Toolbar, type Column } from '../components/ui'
import { api, queryClient, useGet } from '../lib/api'
import { won } from '../lib/format'

type Usage = 'PRODUCT' | 'SHIPPING'
type Stock = { locationId: number; locationName: string; onHandQty: number; minimumQty: number; usedQty: number }
type Packaging = {
  packagingId: number
  packagingName: string
  usageType: Usage
  packagingType: string // 박스, 봉투, 완충재 등
  unitCost: number
  purchaseUrl: string | null
  stocks: Stock[]
}

const USAGE: Record<Usage, string> = { PRODUCT: '상품 포장재', SHIPPING: '택배 포장재' }

// 포장재 관리 (사장 + 출고 직원). 사장은 창고/매장 필터, 출고 직원은 창고만
// 사 오면 [입고], 주문 포장에 쓰면 자동으로 사용수량에 쌓임(6단계), 파손·분실은 [재고 조정]
export default function PackagingPage() {
  const { channel, channelLocations, adjustLocations, filter } = useStockChannel(['OWNER'])
  const [usage, setUsage] = useState('')
  const [editing, setEditing] = useState<Packaging | 'new' | null>(null)
  const [purchasing, setPurchasing] = useState<Packaging | null>(null)
  const [adjusting, setAdjusting] = useState<Packaging | null>(null)
  const [history, setHistory] = useState<Packaging | null>(null)

  const { data, isPending } = useGet<Packaging[]>('/packaging')
  const rows = data?.filter((p) => !usage || p.usageType === usage)

  const stockAt = (p: Packaging, locationId: number) => p.stocks.find((s) => s.locationId === locationId)
  const sumOf = (p: Packaging, key: 'onHandQty' | 'usedQty') => channelLocations.reduce((sum, l) => sum + (stockAt(p, l.locationId)?.[key] ?? 0), 0)
  const total = (p: Packaging) => sumOf(p, 'onHandQty')
  const isLow = (s?: Stock) => !!s && s.minimumQty > 0 && s.onHandQty <= s.minimumQty // 최소 재고 이하 → 빨간색
  const red = (low: boolean) => ({ color: low ? 'var(--color-primary)' : undefined })

  // 위치가 2개 이상이면 위치별 컬럼도 표시
  const locationColumns: Column<Packaging>[] = channelLocations.length < 2 ? [] : channelLocations.map((l) => ({
    key: `loc-${l.locationId}`, title: l.locationName, align: 'right',
    render: (p) => <span style={red(isLow(stockAt(p, l.locationId)))}>{stockAt(p, l.locationId)?.onHandQty ?? 0}</span>,
  }))

  const columns: Column<Packaging>[] = [
    { key: 'packagingName', title: '포장재명' },
    { key: 'usageType', title: '용도', render: (p) => <Badge tone={p.usageType === 'PRODUCT' ? 'primary' : 'neutral'}>{USAGE[p.usageType]}</Badge> },
    { key: 'packagingType', title: '종류' },
    ...locationColumns,
    {
      key: 'onHand', title: '재고수량', align: 'right',
      render: (p) => <span style={red(channelLocations.some((l) => isLow(stockAt(p, l.locationId))))}>{total(p)}</span>,
    },
    { key: 'used', title: '사용수량', align: 'right', render: (p) => sumOf(p, 'usedQty') },
    { key: 'unitCost', title: '단가', align: 'right', render: (p) => won(p.unitCost) },
    {
      key: 'remain', title: '남은 금액', align: 'right', render: (p) => won(p.unitCost * total(p)),
      summary: (list) => won(list.reduce((s, p) => s + p.unitCost * total(p), 0)),
    },
    { key: 'purchaseUrl', title: '구매처', render: (p) => p.purchaseUrl && <a href={p.purchaseUrl} target="_blank" rel="noreferrer">링크</a> },
    {
      key: 'status', title: '상태', align: 'center',
      render: (p) => (total(p) > 0 ? <Badge tone="success">재고 있음</Badge> : <Badge tone="danger">없음</Badge>),
    },
    {
      key: 'actions', title: '관리', align: 'center',
      render: (p) => (
        <div className="row-actions">
          {adjustLocations.length > 0 && <>
            <Button variant="ghost" size="sm" onClick={() => setPurchasing(p)}>입고</Button>
            <Button variant="ghost" size="sm" onClick={() => setAdjusting(p)}>재고 조정</Button>
          </>}
          <Button variant="ghost" size="sm" onClick={() => setHistory(p)}>이력</Button>
          <Button variant="secondary" size="sm" onClick={() => setEditing(p)}>수정</Button>
        </div>
      ),
    },
  ]

  return (
    <>
      <PageHeader title="포장재" actions={<Button onClick={() => setEditing('new')}>+ 추가</Button>} />
      <Toolbar>
        {filter}
        <select className="control-plain" value={usage} onChange={(e) => setUsage(e.target.value)}>
          <option value="">전체 용도</option>
          {Object.entries(USAGE).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>
      </Toolbar>
      <Table key={channel} columns={columns} rows={rows} rowKey={(p) => p.packagingId} loading={isPending}
             emptyText="등록된 포장재가 없습니다." />

      {editing && <PackagingModal packaging={editing === 'new' ? null : editing} locations={adjustLocations} onClose={() => setEditing(null)} />}
      {purchasing && <PurchaseModal packaging={purchasing} locations={adjustLocations} onClose={() => setPurchasing(null)} />}
      {adjusting && <AdjustModal name={adjusting.packagingName} stocks={adjusting.stocks} locations={adjustLocations}
                                 url="/stocks/packaging/adjust" target={{ packagingId: adjusting.packagingId }}
                                 invalidate="/packaging" onClose={() => setAdjusting(null)} />}
      {history && <HistoryModal name={history.packagingName} params={{ packagingId: history.packagingId }} onClose={() => setHistory(null)} />}
    </>
  )
}

const locationOptions = (locations: StockLocation[]) => locations.map((l) => ({ value: l.locationId, label: l.locationName }))

// 포장재 등록·수정. 등록할 때는 처음 사 온 수량을 바로 입고
function PackagingModal({ packaging, locations, onClose }: { packaging: Packaging | null; locations: StockLocation[]; onClose: () => void }) {
  const [form, setForm] = useState({
    packagingName: packaging?.packagingName ?? '',
    usageType: packaging?.usageType ?? 'PRODUCT',
    packagingType: packaging?.packagingType ?? '',
    unitCost: String(packaging?.unitCost ?? ''),
    purchaseUrl: packaging?.purchaseUrl ?? '',
    locationId: String(locations[0]?.locationId ?? ''),
    initialQty: '',
  })
  const set = (patch: Partial<typeof form>) => setForm({ ...form, ...patch })

  const save = useMutation({
    mutationFn: () => {
      const body = {
        ...form, unitCost: Number(form.unitCost), purchaseUrl: form.purchaseUrl || null,
        locationId: form.locationId ? Number(form.locationId) : null, initialQty: Number(form.initialQty || 0),
      }
      return packaging ? api.put(`/packaging/${packaging.packagingId}`, body) : api.post('/packaging', body)
    },
    onSuccess: done,
  })
  const remove = useMutation({ mutationFn: () => api.delete(`/packaging/${packaging!.packagingId}`), onSuccess: done })

  function done() {
    queryClient.invalidateQueries({ queryKey: ['/packaging'] })
    onClose()
  }

  return (
    <FormModal title={packaging ? '포장재 수정' : '포장재 등록'} onClose={onClose} onSubmit={() => save.mutate()}
               saving={save.isPending} error={save.error ?? remove.error}
               left={packaging && <Button variant="ghost" style={{ marginRight: 'auto' }} disabled={remove.isPending}
                                          onClick={() => window.confirm('이 포장재를 삭제할까요?') && remove.mutate()}>삭제</Button>}>
      <Input label="포장재명" required maxLength={200} value={form.packagingName} onChange={(e) => set({ packagingName: e.target.value })} />
      <Select label="용도" value={form.usageType} onChange={(e) => set({ usageType: e.target.value as Usage })}
              options={Object.entries(USAGE).map(([value, label]) => ({ value, label }))} />
      <Input label="종류" required maxLength={100} placeholder="예: 박스, 봉투, 완충재" value={form.packagingType}
             onChange={(e) => set({ packagingType: e.target.value })} />
      <Input label="단가" type="number" min={0} required value={form.unitCost} onChange={(e) => set({ unitCost: e.target.value })} />
      <Input label="구매처 URL" maxLength={1000} value={form.purchaseUrl} onChange={(e) => set({ purchaseUrl: e.target.value })} />
      {!packaging && locations.length > 0 && <>
        <Select label="보관 위치" value={form.locationId} onChange={(e) => set({ locationId: e.target.value })} options={locationOptions(locations)} />
        <Input label="수량 (처음 사 온 수량)" type="number" min={0} placeholder="0" value={form.initialQty}
               onChange={(e) => set({ initialQty: e.target.value })} />
      </>}
      {packaging && <p className="help-text">수량은 표의 [입고]·[재고 조정]으로 바꿔요.</p>}
    </FormModal>
  )
}

// 구매 입고: 포장재를 사 와서 재고에 더함 (원장 '구매 입고')
function PurchaseModal({ packaging, locations, onClose }: { packaging: Packaging; locations: StockLocation[]; onClose: () => void }) {
  const [form, setForm] = useState({ locationId: String(locations[0]?.locationId ?? ''), quantity: '', memo: '' })
  const set = (patch: Partial<typeof form>) => setForm({ ...form, ...patch })

  const save = useMutation({
    mutationFn: () => api.post('/stocks/packaging/purchase', {
      packagingId: packaging.packagingId, locationId: Number(form.locationId), quantity: Number(form.quantity), memo: form.memo || null,
    }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['/packaging'] }); onClose() },
  })

  return (
    <FormModal title={`입고 · ${packaging.packagingName}`} onClose={onClose} onSubmit={() => save.mutate()}
               saving={save.isPending} error={save.error} submitLabel="입고">
      <Select label="보관 위치" required value={form.locationId} onChange={(e) => set({ locationId: e.target.value })} options={locationOptions(locations)} />
      <Input label="수량" type="number" min={1} required value={form.quantity} onChange={(e) => set({ quantity: e.target.value })} />
      <Input label="메모" maxLength={500} placeholder="예: 쿠팡 구매" value={form.memo} onChange={(e) => set({ memo: e.target.value })} />
    </FormModal>
  )
}
