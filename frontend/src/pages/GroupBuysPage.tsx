import { useNavigate } from 'react-router'
import { Badge, Button, PageHeader, Table, type Column } from '../components/ui'
import { tagTone } from '../lib/format'

// 공동구매 목록 API 응답 모양
export type GroupBuy = {
  groupBuyId: number
  groupBuyName: string
  purchaseDate: string | null // 구매일자
  releaseDate: string | null // 발매일
  forwardingArrivalDate: string | null // 배대지 도착일자
  shippingStartDate: string | null // 배송시작일자
  shippingType: string | null // 배송유형 (합배송, 단건배송)
}

const columns: Column<GroupBuy>[] = [
  { key: 'groupBuyName', title: '시리즈명', render: (g) => <strong>{g.groupBuyName}</strong> },
  { key: 'purchaseDate', title: '구매일자' },
  { key: 'releaseDate', title: '발매일' },
  { key: 'forwardingArrivalDate', title: '배대지 도착일자' },
  { key: 'shippingStartDate', title: '배송시작일자' },
  { key: 'shippingType', title: '배송유형', render: (g) => g.shippingType && <Badge tone={tagTone(g.shippingType)}>{g.shippingType}</Badge> },
]

export default function GroupBuysPage() {
  const navigate = useNavigate()

  return (
    <>
      <PageHeader title="공동구매" actions={<Button>새로 만들기</Button>} />
      {/* TODO: 공동구매 API 연결 후 rows에 데이터 전달 */}
      <Table
        columns={columns}
        rows={[]}
        rowKey={(g) => g.groupBuyId}
        onRowClick={(g) => navigate(`/group-buys/${g.groupBuyId}`)}
        emptyText="등록된 공동구매가 없습니다."
      />
    </>
  )
}
