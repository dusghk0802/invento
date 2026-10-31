import { useNavigate, useParams } from 'react-router'
import { Button, Card, InfoGrid, PageHeader, Table, Toolbar, type Column } from '../components/ui'
import { orderNo, won } from '../lib/format'
import type { Order } from './OrdersPage'

// 주문한 상품 API 응답 모양
type OrderItem = {
  orderItemId: number
  productId: number
  productName: string
  orderId: number
  productCode: string
  quantity: number
  unitCost: number // 원가 (주문 시점)
  unitPrice: number // 판매가 (주문 시점)
  lineTotalAmount: number // 판매금액 = 판매가 × 수량
  lineProfit: number // 굿즈 순이익 = (판매가 - 원가) × 수량
}

const itemColumns: Column<OrderItem>[] = [
  { key: 'productName', title: '상품명' },
  { key: 'orderId', title: '주문번호', render: (i) => orderNo(i.orderId) },
  { key: 'productCode', title: '상품번호' },
  { key: 'quantity', title: '수량', align: 'right' },
  { key: 'unitCost', title: '원가', align: 'right', render: (i) => won(i.unitCost) },
  { key: 'unitPrice', title: '판매가', align: 'right', render: (i) => won(i.unitPrice) },
  { key: 'lineTotalAmount', title: '판매금액', align: 'right', render: (i) => won(i.lineTotalAmount) },
  { key: 'lineProfit', title: '굿즈 순이익', align: 'right', render: (i) => won(i.lineProfit) },
]

export default function OrderDetailPage() {
  const navigate = useNavigate()
  const orderId = Number(useParams().orderId)
  // TODO: 주문 상세 API 연결 (지금은 빈 화면)
  const order = undefined as Order | undefined
  const items: OrderItem[] = []

  return (
    <>
      <PageHeader
        title={`주문서 ${orderNo(orderId)}`}
        actions={<Button variant="secondary" onClick={() => navigate('/admin/orders')}>목록</Button>}
      />

      <Card className="page-section">
        <InfoGrid
          items={[
            { label: '구매자', value: order?.customerNickname },
            { label: '판매 사이트', value: order?.salesSiteName },
            { label: '배송방법', value: order?.shippingMethodName },
            { label: '주문일', value: order?.orderDate },
            { label: '포장비용', value: order && won(order.packagingCost) },
            { label: '배송비', value: order && won(order.actualShippingFee) },
            { label: '입금액', value: order && won(order.receivedAmount) },
            { label: '순이익', value: order && won(order.finalProfit) },
          ]}
        />
      </Card>

      <h2 className="section-title">주문한 상품</h2>
      <Toolbar actions={<Button>새로 만들기</Button>} />
      <Table
        columns={itemColumns}
        rows={items}
        rowKey={(i) => i.orderItemId}
        onRowClick={(i) => navigate(`/admin/products/${i.productId}`)}
        emptyText="주문한 상품이 없습니다."
      />
    </>
  )
}
