import { useNavigate } from 'react-router'
import { Badge, Button, Card, InfoGrid, PageHeader, Table, Toolbar, type Column } from '../components/ui'
import { tagTone, won } from '../lib/format'
import type { GroupBuy } from './GroupBuysPage'

// 공동구매 주문서 API 응답 모양 (2차금은 소수점 있음)
type GroupBuyOrder = {
  groupBuyOrderId: number
  customerNickname: string
  productNames: string // 구매 굿즈명
  firstPaymentDate: string | null
  secondPaymentDate: string | null
  shippingMethodName: string | null
  firstPayment: number // 1차금
  secondPayment: number // 2차금
  bonusCount: number // 특전 개수
  actualDomesticShipping: number // 실제 배송비
  domesticShipping: number // 국내 배송비
  packagingCost: number // 포장비용
  totalProductAmount: number // 총 굿즈금액
  overseasShipping: number // 해외 배송비
  profit: number // 순이익
}

const columns: Column<GroupBuyOrder>[] = [
  { key: 'customerNickname', title: '구매자', render: (o) => <strong>{o.customerNickname}</strong> },
  { key: 'productNames', title: '구매 굿즈명' },
  { key: 'firstPaymentDate', title: '1차 입금일자' },
  { key: 'secondPaymentDate', title: '2차 입금일자' },
  {
    key: 'shippingMethodName', title: '배송방법',
    render: (o) => o.shippingMethodName && <Badge tone={tagTone(o.shippingMethodName)}>{o.shippingMethodName}</Badge>,
  },
  { key: 'firstPayment', title: '1차금', align: 'right', render: (o) => won(o.firstPayment) },
  { key: 'secondPayment', title: '2차금', align: 'right', render: (o) => won(o.secondPayment) },
  { key: 'bonusCount', title: '특전 개수', align: 'right' },
  { key: 'actualDomesticShipping', title: '실제 배송비', align: 'right', render: (o) => won(o.actualDomesticShipping) },
  { key: 'domesticShipping', title: '국내 배송비', align: 'right', render: (o) => won(o.domesticShipping) },
  { key: 'finalSecond', title: '최종 2차금', align: 'right', render: (o) => won(o.secondPayment + o.domesticShipping) },
  { key: 'packagingCost', title: '포장비용', align: 'right', render: (o) => won(o.packagingCost) },
  { key: 'totalProductAmount', title: '총 굿즈금액', align: 'right', render: (o) => won(o.totalProductAmount) },
  { key: 'overseasShipping', title: '해외 배송비', align: 'right', render: (o) => won(o.overseasShipping) },
  { key: 'profit', title: '순이익', align: 'right', render: (o) => won(o.profit) },
]

export default function GroupBuyDetailPage() {
  const navigate = useNavigate()
  // TODO: 공동구매 상세 API 연결 (지금은 빈 화면)
  const groupBuy = undefined as GroupBuy | undefined
  const orders: GroupBuyOrder[] = []

  return (
    <>
      <PageHeader
        title={groupBuy?.groupBuyName ?? '대량 판매'}
        actions={<Button variant="secondary" onClick={() => navigate('/admin/bulk-sales')}>목록</Button>}
      />

      <Card className="page-section">
        <InfoGrid
          items={[
            { label: '구매일자', value: groupBuy?.purchaseDate },
            { label: '발매일', value: groupBuy?.releaseDate },
            { label: '배대지 도착일자', value: groupBuy?.forwardingArrivalDate },
            { label: '배송시작일자', value: groupBuy?.shippingStartDate },
            { label: '배송유형', value: groupBuy?.shippingType },
          ]}
        />
      </Card>

      <h2 className="section-title">주문서</h2>
      <Toolbar actions={<Button>+ 추가</Button>} />
      <Table columns={columns} rows={orders} rowKey={(o) => o.groupBuyOrderId} emptyText="등록된 주문서가 없습니다." />
    </>
  )
}
