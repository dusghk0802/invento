import { useState } from 'react'
import { useNavigate } from 'react-router'
import type { StockLocation } from '../components/StockChannel'
import { Badge, Button, InfoGrid, Modal, PageHeader, Table, Toolbar, type Column } from '../components/ui'
import { useGet } from '../lib/api'
import { useMe } from '../lib/auth'
import { dateTime, today, won } from '../lib/format'
import { PAYMENT, type PaymentMethod } from '../lib/sale'

type Receipt = {
  orderId: number; orderNo: string; orderDate: string; locationId: number; locationName: string
  itemCount: number; totalQty: number; totalAmount: number; receivedAmount: number
  paymentMethod: PaymentMethod; finalProfit?: number // 사장에게만
}
type ReceiptItem = {
  productId: number; productCode: string; productName: string; quantity: number; unitPrice: number
  lineTotalAmount: number; unitCost?: number; lineProfit?: number
}

const sum = <T,>(rows: T[], f: (r: T) => number) => rows.reduce((s, r) => s + f(r), 0)

// 매장 판매 내역: 계산(POS) 화면에서 결제한 영수증 조회. 사장은 전체 매장, 매장 직원은 담당 매장만
export default function StoreSalesPage() {
  const navigate = useNavigate()
  const me = useMe().data
  const isOwner = me?.role === 'OWNER'
  const [search, setSearch] = useState({ from: today(), to: today(), locationId: '' })
  const [viewing, setViewing] = useState<Receipt | null>(null)

  const stores = useGet<StockLocation[]>('/locations').data?.filter((l) => l.active && l.locationType === 'STORE') ?? []
  const { data, isPending } = useGet<Receipt[]>('/store-sales', { ...search, locationId: search.locationId || undefined })

  const columns: Column<Receipt>[] = [
    { key: 'orderNo', title: '영수증 번호' },
    { key: 'orderDate', title: '일시', render: (r) => dateTime(r.orderDate) },
    { key: 'locationName', title: '매장' },
    { key: 'totalQty', title: '수량', align: 'right', summary: (rows) => sum(rows, (r) => r.totalQty) },
    { key: 'totalAmount', title: '상품 합계', align: 'right', render: (r) => won(r.totalAmount), summary: (rows) => won(sum(rows, (r) => r.totalAmount)) },
    { key: 'receivedAmount', title: '받은 금액', align: 'right', render: (r) => won(r.receivedAmount), summary: (rows) => won(sum(rows, (r) => r.receivedAmount)) },
    { key: 'paymentMethod', title: '결제', align: 'center', render: (r) => <Badge>{PAYMENT[r.paymentMethod]}</Badge> },
    ...(isOwner ? [{
      key: 'finalProfit', title: '이익', align: 'right' as const, render: (r: Receipt) => won(r.finalProfit),
      summary: (rows: Receipt[]) => won(sum(rows, (r) => r.finalProfit ?? 0)),
    }] : []),
  ]

  return (
    <>
      <PageHeader title="매장 판매 내역" actions={<Button onClick={() => navigate('/admin/pos')}>계산하기</Button>} />
      <Toolbar>
        <input className="control-plain" type="date" value={search.from} onChange={(e) => setSearch({ ...search, from: e.target.value })} />
        ~
        <input className="control-plain" type="date" value={search.to} onChange={(e) => setSearch({ ...search, to: e.target.value })} />
        {isOwner && (
          <select className="control-plain" value={search.locationId} onChange={(e) => setSearch({ ...search, locationId: e.target.value })}>
            <option value="">전체 매장</option>
            {stores.map((l) => <option key={l.locationId} value={l.locationId}>{l.locationName}</option>)}
          </select>
        )}
      </Toolbar>
      <Table columns={columns} rows={data} rowKey={(r) => r.orderId} loading={isPending} onRowClick={setViewing}
             emptyText="이 기간에 등록된 영수증이 없습니다." />

      {viewing && <ReceiptDetail receipt={viewing} isOwner={isOwner} onClose={() => setViewing(null)} />}
    </>
  )
}

// 영수증 상세 (원가·이익은 사장만)
function ReceiptDetail({ receipt, isOwner, onClose }: { receipt: Receipt; isOwner: boolean; onClose: () => void }) {
  const items = useGet<{ items: ReceiptItem[] }>(`/store-sales/${receipt.orderId}`).data?.items

  const columns: Column<ReceiptItem>[] = [
    { key: 'productName', title: '상품명' },
    { key: 'productCode', title: '상품번호' },
    { key: 'quantity', title: '수량', align: 'right' },
    { key: 'unitPrice', title: '판매가', align: 'right', render: (i) => won(i.unitPrice) },
    { key: 'lineTotalAmount', title: '금액', align: 'right', render: (i) => won(i.lineTotalAmount) },
    ...(isOwner ? [
      { key: 'unitCost', title: '원가', align: 'right' as const, render: (i: ReceiptItem) => won(i.unitCost) },
      { key: 'lineProfit', title: '이익', align: 'right' as const, render: (i: ReceiptItem) => won(i.lineProfit) },
    ] : []),
  ]

  return (
    <Modal open width={720} title={`영수증 ${receipt.orderNo}`} onClose={onClose}>
      <InfoGrid items={[
        { label: '일시', value: dateTime(receipt.orderDate) },
        { label: '매장', value: receipt.locationName },
        { label: '결제', value: PAYMENT[receipt.paymentMethod] },
        { label: '상품 합계', value: won(receipt.totalAmount) },
        { label: '받은 금액', value: won(receipt.receivedAmount) },
        ...(isOwner ? [{ label: '이익', value: won(receipt.finalProfit) }] : []),
      ]} />
      <Table columns={columns} rows={items} rowKey={(i) => i.productId} loading={!items} />
    </Modal>
  )
}
