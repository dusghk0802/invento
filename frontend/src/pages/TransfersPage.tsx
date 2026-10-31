import { useState } from 'react'
import { useNavigate } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import type { StockLocation } from '../components/StockChannel'
import { Button, FormModal, Input, PageHeader, Select, Table, Toolbar, type Column } from '../components/ui'
import { api, queryClient, useGet } from '../lib/api'
import { useMe } from '../lib/auth'
import { dateTime, orderNo } from '../lib/format'
import { TRANSFER_STATUS, transferBadge, type Transfer } from '../lib/transfer'

type Product = {
  productId: number; productCode: string; productName: string; boxQty: number | null
  stocks: { locationId: number; onHandQty: number; availableQty: number; minimumQty: number }[]
}

const columns: Column<Transfer>[] = [
  { key: 'stockTransferId', title: '번호', render: (t) => orderNo(t.stockTransferId) },
  { key: 'fromLocationName', title: '출발' },
  { key: 'toLocationName', title: '도착' },
  { key: 'itemCount', title: '상품 수', align: 'right' },
  { key: 'totalQty', title: '총 수량', align: 'right' },
  { key: 'requestedAt', title: '요청일', render: (t) => dateTime(t.requestedAt) },
  { key: 'shippedAt', title: '출고일', render: (t) => dateTime(t.shippedAt) },
  { key: 'receivedAt', title: '도착일', render: (t) => dateTime(t.receivedAt) },
  { key: 'status', title: '상태', align: 'center', render: transferBadge },
]

// 매장출고현황: 매장 직원 요청 → 창고 직원 출고 → 매장 직원 도착 확인. 직원은 자기 위치가 들어간 건만 보임
export default function TransfersPage() {
  const navigate = useNavigate()
  const role = useMe().data?.role
  const [status, setStatus] = useState('')
  const [requesting, setRequesting] = useState(false)

  const { data, isPending } = useGet<Transfer[]>('/transfers', { status: status || undefined })

  return (
    <>
      <PageHeader title="매장 입·출고 내역"
                  actions={(role === 'OWNER' || role === 'STORE') && <Button onClick={() => setRequesting(true)}>+ 입고요청</Button>} />
      <Toolbar>
        <select className="control-plain" value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">전체 상태</option>
          {Object.entries(TRANSFER_STATUS).map(([value, s]) => <option key={value} value={value}>{s.label}</option>)}
        </select>
      </Toolbar>
      <Table columns={columns} rows={data} rowKey={(t) => t.stockTransferId} loading={isPending}
             onRowClick={(t) => navigate(`/admin/transfers/${t.stockTransferId}`)} emptyText="출고 요청이 없습니다." />
      {requesting && <RequestModal onClose={() => setRequesting(false)} />}
    </>
  )
}

type Line = { productId: string; boxCount: string; looseQty: string }
const emptyLine: Line = { productId: '', boxCount: '', looseQty: '' }

// 출고 요청: 창고 → 매장. 박스 수 × 박스 입수량 + 낱개 = 수량. "부족 상품 불러오기"로 최소 재고 이하 상품을 한 번에 추가
function RequestModal({ onClose }: { onClose: () => void }) {
  const navigate = useNavigate()
  const me = useMe().data
  const myStore = me?.location?.locationType === 'STORE' ? String(me.location.locationId) : ''

  const locations = useGet<StockLocation[]>('/locations')
  const products = useGet<Product[]>('/products')
  const warehouses = locations.data?.filter((l) => l.active && l.locationType === 'WAREHOUSE') ?? []
  const stores = locations.data?.filter((l) => l.active && l.locationType === 'STORE' && (!myStore || String(l.locationId) === myStore)) ?? []

  const [fromId, setFromId] = useState('')
  const [toId, setToId] = useState(myStore)
  const [memo, setMemo] = useState('')
  const [lines, setLines] = useState<Line[]>([emptyLine])
  const from = fromId || String(warehouses[0]?.locationId ?? '')
  const to = toId || String(stores[0]?.locationId ?? '')

  const productOf = (id: string) => products.data?.find((p) => String(p.productId) === id)
  const stockOf = (p: Product | undefined, locationId: string) => p?.stocks.find((s) => String(s.locationId) === locationId)
  const qtyOf = (l: Line) => Number(l.boxCount || 0) * (productOf(l.productId)?.boxQty ?? 0) + Number(l.looseQty || 0)
  const setLine = (idx: number, patch: Partial<Line>) => setLines(lines.map((l, i) => (i === idx ? { ...l, ...patch } : l)))

  // 도착 매장에서 최소 재고 이하인 상품을 줄로 추가 (이미 있는 상품은 제외)
  function loadLowStock() {
    const low = (products.data ?? []).filter((p) => {
      const s = stockOf(p, to)
      return s && s.minimumQty > 0 && s.onHandQty <= s.minimumQty && !lines.some((l) => l.productId === String(p.productId))
    })
    setLines([...lines.filter((l) => l.productId), ...low.map((p) => ({ ...emptyLine, productId: String(p.productId) }))])
  }

  const save = useMutation({
    mutationFn: () => api.post<{ transfer: Transfer }>('/transfers', {
      fromLocationId: Number(from), toLocationId: Number(to), memo: memo || null,
      items: lines.map((l) => ({ productId: Number(l.productId), boxCount: Number(l.boxCount || 0), looseQty: Number(l.looseQty || 0) })),
    }),
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ['/transfers'] })
      onClose()
      navigate(`/admin/transfers/${res.data.transfer.stockTransferId}`)
    },
  })

  return (
    <FormModal title="출고 요청" width={680} onClose={onClose} onSubmit={() => save.mutate()}
               saving={save.isPending} error={save.error} submitLabel="요청">
      <Select label="출발 (창고)" required value={from} onChange={(e) => setFromId(e.target.value)}
              options={warehouses.map((l) => ({ value: l.locationId, label: l.locationName }))} />
      <Select label="도착 (매장)" required value={to} disabled={!!myStore} onChange={(e) => setToId(e.target.value)}
              options={stores.map((l) => ({ value: l.locationId, label: l.locationName }))} />
      <Input label="메모" maxLength={500} value={memo} onChange={(e) => setMemo(e.target.value)} />

      <Toolbar actions={<Button variant="secondary" size="sm" onClick={loadLowStock}>부족 상품 불러오기</Button>}>
        <h3 className="section-title">요청 상품</h3>
      </Toolbar>
      {lines.map((l, i) => {
        const p = productOf(l.productId)
        const store = stockOf(p, to)
        const warehouse = stockOf(p, from)
        return (
          <div key={i}>
            <div className="rule-row" style={{ gridTemplateColumns: '1fr 80px 80px 70px auto' }}>
              <select className="control-plain" required value={l.productId} onChange={(e) => setLine(i, { productId: e.target.value })}>
                <option value="">상품 선택</option>
                {products.data?.map((x) => <option key={x.productId} value={x.productId}>{x.productName} ({x.productCode})</option>)}
              </select>
              <input className="control-plain" type="number" min={0} placeholder="박스" aria-label="박스 수" disabled={!p?.boxQty}
                     value={l.boxCount} onChange={(e) => setLine(i, { boxCount: e.target.value })} />
              <input className="control-plain" type="number" min={0} placeholder="낱개" aria-label="낱개 수"
                     value={l.looseQty} onChange={(e) => setLine(i, { looseQty: e.target.value })} />
              <span style={{ textAlign: 'right' }}>= {qtyOf(l)}개</span>
              <Button variant="ghost" size="sm" disabled={lines.length === 1} onClick={() => setLines(lines.filter((_, idx) => idx !== i))}>삭제</Button>
            </div>
            {p && (
              <p className="help-text">
                {p.boxQty ? `1박스 ${p.boxQty}개` : '박스 입수량 없음 (낱개만)'}
                {' · '}창고 판매가능 {warehouse?.availableQty ?? 0}
                {' · '}매장 보유 {store?.onHandQty ?? 0}{store?.minimumQty ? ` / 최소 ${store.minimumQty}` : ''}
              </p>
            )}
          </div>
        )
      })}
      <Button variant="secondary" size="sm" onClick={() => setLines([...lines, emptyLine])}>+ 상품 추가</Button>
    </FormModal>
  )
}
