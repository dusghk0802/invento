import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { useStockChannel } from '../components/StockChannel'
import { AdjustModal, HistoryModal } from '../components/StockModals'
import { Badge, Button, FormModal, Input, PageHeader, Select, Table, Toolbar, type Column } from '../components/ui'
import { api, queryClient, useGet } from '../lib/api'
import { won } from '../lib/format'

type Usage = 'PRODUCT' | 'SHIPPING'
type Stock = { locationId: number; locationName: string; onHandQty: number; minimumQty: number }
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
export default function PackagingPage() {
  const { channel, channelLocations, adjustLocations, filter } = useStockChannel(['OWNER'])
  const [usage, setUsage] = useState('')
  const [editing, setEditing] = useState<Packaging | 'new' | null>(null)
  const [adjusting, setAdjusting] = useState<Packaging | null>(null)
  const [history, setHistory] = useState<Packaging | null>(null)

  const { data, isPending } = useGet<Packaging[]>('/packaging')
  const rows = data?.filter((p) => !usage || p.usageType === usage)

  const stockAt = (p: Packaging, locationId: number) => p.stocks.find((s) => s.locationId === locationId)
  const total = (p: Packaging) => channelLocations.reduce((sum, l) => sum + (stockAt(p, l.locationId)?.onHandQty ?? 0), 0)

  // 위치마다 재고 컬럼 1개. 최소 재고 이하이면 빨간색
  const locationColumns: Column<Packaging>[] = channelLocations.map((l) => ({
    key: `loc-${l.locationId}`, title: l.locationName, align: 'right',
    render: (p) => {
      const s = stockAt(p, l.locationId)
      const low = !!s && s.minimumQty > 0 && s.onHandQty <= s.minimumQty
      return <span style={{ color: low ? 'var(--color-primary)' : undefined }}>{s?.onHandQty ?? 0}</span>
    },
  }))

  const columns: Column<Packaging>[] = [
    { key: 'packagingName', title: '포장재명' },
    { key: 'usageType', title: '용도', render: (p) => <Badge tone={p.usageType === 'PRODUCT' ? 'primary' : 'neutral'}>{USAGE[p.usageType]}</Badge> },
    { key: 'packagingType', title: '종류' },
    ...locationColumns,
    // 위치가 2개 이상일 때만 합계
    ...(channelLocations.length > 1 ? [{ key: 'total', title: '합계', align: 'right' as const, render: total }] : []),
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
          {adjustLocations.length > 0 && <Button variant="ghost" size="sm" onClick={() => setAdjusting(p)}>재고 조정</Button>}
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

      {editing && <PackagingModal packaging={editing === 'new' ? null : editing} onClose={() => setEditing(null)} />}
      {adjusting && <AdjustModal name={adjusting.packagingName} stocks={adjusting.stocks} locations={adjustLocations}
                                 url="/stocks/packaging/adjust" target={{ packagingId: adjusting.packagingId }}
                                 invalidate="/packaging" onClose={() => setAdjusting(null)} />}
      {history && <HistoryModal name={history.packagingName} params={{ packagingId: history.packagingId }} onClose={() => setHistory(null)} />}
    </>
  )
}

function PackagingModal({ packaging, onClose }: { packaging: Packaging | null; onClose: () => void }) {
  const [form, setForm] = useState({
    packagingName: packaging?.packagingName ?? '',
    usageType: packaging?.usageType ?? 'PRODUCT',
    packagingType: packaging?.packagingType ?? '',
    unitCost: String(packaging?.unitCost ?? ''),
    purchaseUrl: packaging?.purchaseUrl ?? '',
  })
  const set = (patch: Partial<typeof form>) => setForm({ ...form, ...patch })

  const save = useMutation({
    mutationFn: () => {
      const body = { ...form, unitCost: Number(form.unitCost), purchaseUrl: form.purchaseUrl || null }
      return packaging ? api.put(`/packaging/${packaging.packagingId}`, body) : api.post('/packaging', body)
    },
    onSuccess: done,
  })
  const remove = useMutation({ mutationFn: () => api.delete(`/packaging/${packaging!.packagingId}`), onSuccess: done })

  function done() {
    queryClient.invalidateQueries({ queryKey: ['/packaging'] })
    onClose()
  }

  const error = save.error ?? remove.error

  return (
    <FormModal title={packaging ? '포장재 수정' : '포장재 등록'} onClose={onClose} onSubmit={() => save.mutate()}
               saving={save.isPending} error={error}
               left={packaging && <Button variant="ghost" style={{ marginRight: 'auto' }} disabled={remove.isPending}
                                          onClick={() => window.confirm('이 포장재를 삭제할까요?') && remove.mutate()}>삭제</Button>}>
      <Input label="포장재명" required maxLength={200} value={form.packagingName} onChange={(e) => set({ packagingName: e.target.value })} />
      <Select label="용도" value={form.usageType} onChange={(e) => set({ usageType: e.target.value as Usage })}
              options={Object.entries(USAGE).map(([value, label]) => ({ value, label }))} />
      <Input label="종류" required maxLength={100} placeholder="예: 박스, 봉투, 완충재" value={form.packagingType}
             onChange={(e) => set({ packagingType: e.target.value })} />
      <Input label="단가" type="number" min={0} required value={form.unitCost} onChange={(e) => set({ unitCost: e.target.value })} />
      <Input label="구매처 URL" maxLength={1000} value={form.purchaseUrl} onChange={(e) => set({ purchaseUrl: e.target.value })} />
    </FormModal>
  )
}
