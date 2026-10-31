import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { useMutation, useQuery } from '@tanstack/react-query'
import { useStockChannel, type StockLocation } from '../components/StockChannel'
import { Button, Card, Input, InfoGrid, Modal, PageHeader, Select, Table, type Column } from '../components/ui'
import { api, getErrorMessage, queryClient } from '../lib/api'
import { orderNo, today, won } from '../lib/format'
import { statusBadge, type ProductionOrder } from '../lib/production'
import { ProductionOrderModal } from './ProductionOrdersPage'

type Item = {
  productionOrderItemId: number; productId: number; productCode: string; productName: string
  orderedQty: number; receivedQty: number; defectiveQty: number; remainingQty: number; costPrice?: number
}
type Receipt = { productionReceiptId: number; productName: string; locationId: number; locationName: string; receivedDate: string; receivedQty: number; defectiveQty: number }
type Detail = { order: ProductionOrder; items: Item[]; receipts: Receipt[] }

// 발주 상세: 항목별 입고 현황, 입고 처리(일부 입고 가능), 입고 기록
export default function ProductionOrderDetailPage() {
  const navigate = useNavigate()
  const id = Number(useParams().productionOrderId)
  const url = `/production-orders/${id}`
  const { isOwner, adjustLocations } = useStockChannel(['OWNER']) // 입고할 수 있는 창고 (창고 직원 = 담당 창고)
  const [editing, setEditing] = useState(false)
  const [receiving, setReceiving] = useState(false)
  const [correcting, setCorrecting] = useState<Receipt | null>(null)

  const { data } = useQuery({ queryKey: ['/production-orders', id], queryFn: () => api.get<Detail>(url).then((r) => r.data) })
  const order = data?.order
  const open = order?.status === 'ORDERED' || order?.status === 'PARTIAL'

  const cancel = useMutation({
    mutationFn: () => api.post(`${url}/cancel`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['/production-orders'] }),
  })

  const itemColumns: Column<Item>[] = [
    { key: 'productName', title: '상품명' },
    { key: 'productCode', title: '상품번호' },
    { key: 'orderedQty', title: '발주', align: 'right' },
    { key: 'receivedQty', title: '입고', align: 'right' },
    { key: 'defectiveQty', title: '불량', align: 'right' },
    { key: 'remainingQty', title: '남은 수량', align: 'right' },
    ...(isOwner ? [
      { key: 'costPrice', title: '원가', align: 'right' as const, render: (i: Item) => won(i.costPrice) },
      { key: 'amount', title: '금액', align: 'right' as const, render: (i: Item) => won((i.costPrice ?? 0) * i.orderedQty) },
    ] : []),
  ]

  // 입고 기록 수정: 취소 안 된 발주, 내가 처리할 수 있는 창고의 기록만
  const canCorrect = (r: Receipt) => order?.status !== 'CANCELLED' && adjustLocations.some((l) => l.locationId === r.locationId)
  const receiptColumns: Column<Receipt>[] = [
    { key: 'receivedDate', title: '입고일' },
    { key: 'productName', title: '상품명' },
    { key: 'locationName', title: '창고' },
    { key: 'receivedQty', title: '정상', align: 'right' },
    { key: 'defectiveQty', title: '불량', align: 'right' },
    {
      key: 'actions', title: '관리', align: 'center',
      render: (r) => canCorrect(r) && <Button variant="secondary" size="sm" onClick={() => setCorrecting(r)}>수정</Button>,
    },
  ]

  return (
    <>
      <PageHeader
        title={`발주 ${orderNo(id)}`}
        actions={
          <>
            <Button variant="secondary" onClick={() => navigate('/admin/production-orders')}>목록</Button>
            {order?.status === 'ORDERED' && <Button variant="secondary" onClick={() => setEditing(true)}>수정</Button>}
            {open && <Button variant="secondary" disabled={cancel.isPending}
                             onClick={() => window.confirm('발주를 취소할까요? 이미 입고된 재고는 그대로 남아요.') && cancel.mutate()}>발주 취소</Button>}
            {open && adjustLocations.length > 0 && <Button onClick={() => setReceiving(true)}>입고 처리</Button>}
          </>
        }
      />
      {cancel.isError && <p className="form-error">{getErrorMessage(cancel.error)}</p>}

      <Card className="page-section">
        <InfoGrid
          items={[
            { label: '공급처', value: order?.supplierName },
            { label: '상태', value: order && statusBadge(order) },
            { label: '발주일', value: order?.orderDate },
            { label: '입고 예정일', value: order?.expectedDate },
            { label: '발주 / 입고 / 불량', value: order && `${order.orderedQty} / ${order.receivedQty} / ${order.defectiveQty}` },
            ...(isOwner ? [{ label: '발주 금액', value: order && won(order.totalCost) }] : []),
            { label: '메모', value: order?.memo },
          ]}
        />
      </Card>

      <div className="page-section">
        <h2 className="section-title">발주 상품</h2>
        <Table columns={itemColumns} rows={data?.items} rowKey={(i) => i.productionOrderItemId} loading={!data} />
      </div>

      <h2 className="section-title">입고 기록</h2>
      <Table columns={receiptColumns} rows={data?.receipts} rowKey={(r) => r.productionReceiptId} loading={!data}
             emptyText="아직 입고된 상품이 없습니다." />

      {editing && order && <ProductionOrderModal order={order} items={data.items} onClose={() => setEditing(false)} />}
      {correcting && <CorrectModal url={`${url}/receipts/${correcting.productionReceiptId}`} receipt={correcting}
                                   onClose={() => setCorrecting(null)} />}
      {receiving && data && <ReceiveModal url={url} items={data.items} locations={adjustLocations} onClose={() => setReceiving(false)} />}
    </>
  )
}

// 입고 처리: 남은 수량이 기본값. 정상 수량만 창고 재고에 들어가고 불량은 기록만
function ReceiveModal({ url, items, locations, onClose }: { url: string; items: Item[]; locations: StockLocation[]; onClose: () => void }) {
  const remaining = items.filter((i) => i.remainingQty > 0)
  const [locationId, setLocationId] = useState(String(locations[0]?.locationId ?? ''))
  const [receivedDate, setReceivedDate] = useState(today())
  const [qty, setQty] = useState(Object.fromEntries(remaining.map((i) => [i.productionOrderItemId, { ok: String(i.remainingQty), bad: '0' }])))
  const setItem = (itemId: number, patch: Partial<{ ok: string; bad: string }>) => setQty({ ...qty, [itemId]: { ...qty[itemId], ...patch } })

  const save = useMutation({
    mutationFn: () => api.post(`${url}/receipts`, {
      locationId: Number(locationId), receivedDate,
      items: remaining.map((i) => ({
        productionOrderItemId: i.productionOrderItemId,
        receivedQty: Number(qty[i.productionOrderItemId].ok || 0),
        defectiveQty: Number(qty[i.productionOrderItemId].bad || 0),
      })),
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/production-orders'] })
      queryClient.invalidateQueries({ queryKey: ['/products'] })
      onClose()
    },
  })

  return (
    <Modal open width={600} title="입고 처리" onClose={onClose}
           footer={
             <>
               <Button variant="secondary" onClick={onClose}>취소</Button>
               <Button type="submit" form="receive-form" disabled={save.isPending}>입고</Button>
             </>
           }>
      <form id="receive-form" className="form-stack" onSubmit={(e) => { e.preventDefault(); save.mutate() }}>
        <Select label="입고 창고" required value={locationId} onChange={(e) => setLocationId(e.target.value)}
                options={locations.map((l) => ({ value: l.locationId, label: l.locationName }))} />
        <Input label="입고일" type="date" required value={receivedDate} onChange={(e) => setReceivedDate(e.target.value)} />
        <p className="help-text">이번에 들어온 수량만 입력하세요. 남은 수량은 다음에 나눠서 입고할 수 있어요.</p>
        {remaining.map((i) => (
          <div key={i.productionOrderItemId} className="rule-row" style={{ gridTemplateColumns: '1fr 100px 100px' }}>
            <span>{i.productName} <span className="help-text">(남은 {i.remainingQty})</span></span>
            <input className="control-plain" type="number" min={0} max={i.remainingQty} placeholder="정상" aria-label="정상 수량"
                   value={qty[i.productionOrderItemId].ok} onChange={(e) => setItem(i.productionOrderItemId, { ok: e.target.value })} />
            <input className="control-plain" type="number" min={0} placeholder="불량" aria-label="불량 수량"
                   value={qty[i.productionOrderItemId].bad} onChange={(e) => setItem(i.productionOrderItemId, { bad: e.target.value })} />
          </div>
        ))}
        {save.isError && <p className="form-error">{getErrorMessage(save.error)}</p>}
      </form>
    </Modal>
  )
}

// 입고 기록 수정 (잘못 입력한 경우): 정상 수량 차이만큼 창고 재고도 같이 바뀜
function CorrectModal({ url, receipt, onClose }: { url: string; receipt: Receipt; onClose: () => void }) {
  const [ok, setOk] = useState(String(receipt.receivedQty))
  const [bad, setBad] = useState(String(receipt.defectiveQty))

  const save = useMutation({
    mutationFn: () => api.put(url, { receivedQty: Number(ok || 0), defectiveQty: Number(bad || 0) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/production-orders'] })
      queryClient.invalidateQueries({ queryKey: ['/products'] })
      onClose()
    },
  })

  return (
    <Modal open title={`입고 수정 · ${receipt.productName}`} onClose={onClose}
           footer={
             <>
               <Button variant="secondary" onClick={onClose}>취소</Button>
               <Button type="submit" form="correct-form" disabled={save.isPending}>저장</Button>
             </>
           }>
      <form id="correct-form" className="form-stack" onSubmit={(e) => { e.preventDefault(); save.mutate() }}>
        <p className="help-text">{receipt.receivedDate} · {receipt.locationName} 입고분</p>
        <Input label="정상 수량" type="number" min={0} required value={ok} onChange={(e) => setOk(e.target.value)} />
        <Input label="불량 수량" type="number" min={0} required value={bad} onChange={(e) => setBad(e.target.value)} />
        <p className="help-text">
          창고 재고 {Number(ok || 0) - receipt.receivedQty >= 0 ? '+' : ''}{Number(ok || 0) - receipt.receivedQty}개.
          이미 판매·출고돼서 창고 재고가 모자라면 줄일 수 없어요.
        </p>
        {save.isError && <p className="form-error">{getErrorMessage(save.error)}</p>}
      </form>
    </Modal>
  )
}
