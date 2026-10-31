import { useState } from 'react'
import { useNavigate } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import { Button, FormModal, Input, PageHeader, Select, Table, Toolbar, type Column } from '../components/ui'
import { api, queryClient, useGet } from '../lib/api'
import { useIsOwner } from '../lib/auth'
import { orderNo, today, won } from '../lib/format'
import { STATUS, statusBadge, type ProductionItem, type ProductionOrder } from '../lib/production'

type Supplier = { supplierId: number; supplierName: string; active: boolean }
type Product = { productId: number; productCode: string; productName: string }

// 공장 발주·입고 (사장 + 창고 직원). 행을 누르면 상세(입고 처리)
export default function ProductionOrdersPage() {
  const navigate = useNavigate()
  const isOwner = useIsOwner()
  const [status, setStatus] = useState('')
  const [creating, setCreating] = useState(false)

  const { data, isPending } = useGet<ProductionOrder[]>('/production-orders', { status: status || undefined })

  const columns: Column<ProductionOrder>[] = [
    { key: 'productionOrderId', title: '발주번호', render: (o) => orderNo(o.productionOrderId) },
    { key: 'supplierName', title: '공급처' },
    { key: 'orderDate', title: '발주일' },
    { key: 'expectedDate', title: '입고 예정일' },
    { key: 'itemCount', title: '상품 수', align: 'right' },
    { key: 'orderedQty', title: '발주수량', align: 'right' },
    { key: 'receivedQty', title: '입고수량', align: 'right' },
    { key: 'defectiveQty', title: '불량', align: 'right' },
    ...(isOwner ? [{ key: 'totalCost', title: '발주 금액', align: 'right' as const, render: (o: ProductionOrder) => won(o.totalCost) }] : []),
    { key: 'status', title: '상태', align: 'center', render: statusBadge },
  ]

  return (
    <>
      <PageHeader title="발주·입고" actions={<Button onClick={() => setCreating(true)}>+ 추가</Button>} />
      <Toolbar>
        <select className="control-plain" value={status} onChange={(e) => setStatus(e.target.value)}>
          <option value="">전체 상태</option>
          {Object.entries(STATUS).map(([value, s]) => <option key={value} value={value}>{s.label}</option>)}
        </select>
      </Toolbar>
      <Table columns={columns} rows={data} rowKey={(o) => o.productionOrderId} loading={isPending}
             onRowClick={(o) => navigate(`/admin/production-orders/${o.productionOrderId}`)}
             emptyText="등록된 발주가 없습니다." />
      {creating && <ProductionOrderModal onClose={() => setCreating(false)} />}
    </>
  )
}

type Line = { productId: string; orderedQty: string }

// 발주 등록·수정 (수정은 입고 전에만). 상품 줄을 추가해서 여러 상품을 한 번에 발주
export function ProductionOrderModal({ order, items, onClose }: { order?: ProductionOrder; items?: ProductionItem[]; onClose: () => void }) {
  const navigate = useNavigate()
  const [form, setForm] = useState({
    supplierId: order ? String(order.supplierId) : '',
    orderDate: order?.orderDate ?? today(),
    expectedDate: order?.expectedDate ?? '',
    memo: order?.memo ?? '',
  })
  const [lines, setLines] = useState<Line[]>(
    items?.map((i) => ({ productId: String(i.productId), orderedQty: String(i.orderedQty) })) ?? [{ productId: '', orderedQty: '' }])
  const set = (patch: Partial<typeof form>) => setForm({ ...form, ...patch })
  const setLine = (idx: number, patch: Partial<Line>) => setLines(lines.map((l, i) => (i === idx ? { ...l, ...patch } : l)))

  const suppliers = useGet<Supplier[]>('/suppliers')
  const products = useGet<Product[]>('/products')

  const save = useMutation({
    mutationFn: () => {
      const body = {
        supplierId: Number(form.supplierId), orderDate: form.orderDate, expectedDate: form.expectedDate || null,
        memo: form.memo || null, items: lines.map((l) => ({ productId: Number(l.productId), orderedQty: Number(l.orderedQty) })),
      }
      return order ? api.put(`/production-orders/${order.productionOrderId}`, body) : api.post('/production-orders', body)
    },
    onSuccess: (res) => {
      queryClient.invalidateQueries({ queryKey: ['/production-orders'] })
      onClose()
      if (!order) navigate(`/admin/production-orders/${res.data.order.productionOrderId}`)
    },
  })

  return (
    <FormModal title={order ? '발주 수정' : '발주 등록'} width={600} onClose={onClose} onSubmit={() => save.mutate()}
               saving={save.isPending} error={save.error}>
      <Select label="공급처" required value={form.supplierId} placeholder="선택" onChange={(e) => set({ supplierId: e.target.value })}
              options={(suppliers.data ?? []).filter((s) => s.active || String(s.supplierId) === form.supplierId)
                .map((s) => ({ value: s.supplierId, label: s.supplierName }))} />
      <Input label="발주일" type="date" required value={form.orderDate} onChange={(e) => set({ orderDate: e.target.value })} />
      <Input label="입고 예정일" type="date" value={form.expectedDate} onChange={(e) => set({ expectedDate: e.target.value })} />
      <Input label="메모" maxLength={500} value={form.memo} onChange={(e) => set({ memo: e.target.value })} />

      <h3 className="section-title">발주 상품</h3>
      {lines.map((l, i) => (
        <div key={i} className="rule-row" style={{ gridTemplateColumns: '1fr 110px auto' }}>
          <select className="control-plain" required value={l.productId} onChange={(e) => setLine(i, { productId: e.target.value })}>
            <option value="">상품 선택</option>
            {products.data?.map((p) => <option key={p.productId} value={p.productId}>{p.productName} ({p.productCode})</option>)}
          </select>
          <input className="control-plain" type="number" min={1} required placeholder="수량" value={l.orderedQty}
                 onChange={(e) => setLine(i, { orderedQty: e.target.value })} />
          <Button variant="ghost" size="sm" disabled={lines.length === 1} onClick={() => setLines(lines.filter((_, idx) => idx !== i))}>삭제</Button>
        </div>
      ))}
      <Button variant="secondary" size="sm" onClick={() => setLines([...lines, { productId: '', orderedQty: '' }])}>+ 상품 추가</Button>
    </FormModal>
  )
}
