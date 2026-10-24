import { useNavigate } from 'react-router'
import { Badge, Button, PageHeader, Table, type Column } from '../components/ui'
import { orderNo, tagTone, won } from '../lib/format'

// 주문 목록 API 응답 모양 (주문 백엔드에서 이 형태로 만들 예정)
export type Order = {
  orderId: number
  customerNickname: string
  salesSiteName: string
  shippingMethodName: string | null
  orderDate: string
  packagingCost: number // 포장비용 = Σ 포장재 원가
  actualShippingFee: number // 배송비용 = 실제 배송비
  receivedAmount: number // 입금액
  finalProfit: number // 총순이익
  totalQty: number // 총 굿즈 판매 개수
  totalSalesAmount: number // 총 판매금액
  productProfit: number // 굿즈 순이익
}

const sum = (rows: Order[], pick: (o: Order) => number) => rows.reduce((s, o) => s + pick(o), 0)

const columns: Column<Order>[] = [
  { key: 'orderId', title: '주문번호', render: (o) => <strong>{orderNo(o.orderId)}</strong>, summary: (rows) => `개수 ${rows.length}` },
  { key: 'customerNickname', title: '구매자' },
  { key: 'salesSiteName', title: '판매 사이트', render: (o) => <Badge tone={tagTone(o.salesSiteName)}>{o.salesSiteName}</Badge> },
  {
    key: 'shippingMethodName', title: '배송방법',
    render: (o) => o.shippingMethodName && <Badge tone={tagTone(o.shippingMethodName)}>{o.shippingMethodName}</Badge>,
  },
  { key: 'orderDate', title: '주문일' },
  { key: 'packagingCost', title: '포장비용', align: 'right', render: (o) => won(o.packagingCost) },
  { key: 'actualShippingFee', title: '배송비용', align: 'right', render: (o) => won(o.actualShippingFee) },
  { key: 'receivedAmount', title: '입금액', align: 'right', render: (o) => won(o.receivedAmount) },
  {
    key: 'finalProfit', title: '총순이익', align: 'right', render: (o) => won(o.finalProfit),
    summary: (rows) => `합계 ${won(sum(rows, (o) => o.finalProfit))}`,
  },
  { key: 'totalQty', title: '총 판매 개수', align: 'right' },
  { key: 'totalSalesAmount', title: '총 판매금액', align: 'right', render: (o) => won(o.totalSalesAmount) },
  { key: 'productProfit', title: '굿즈 순이익', align: 'right', render: (o) => won(o.productProfit) },
]

export default function OrdersPage() {
  const navigate = useNavigate()

  return (
    <>
      <PageHeader title="주문" actions={<Button>새로 만들기</Button>} />
      {/* TODO: 주문 API 연결 후 rows에 데이터 전달 */}
      <Table
        columns={columns}
        rows={[]}
        rowKey={(o) => o.orderId}
        onRowClick={(o) => navigate(`/orders/${o.orderId}`)}
        emptyText="등록된 주문이 없습니다."
      />
    </>
  )
}
