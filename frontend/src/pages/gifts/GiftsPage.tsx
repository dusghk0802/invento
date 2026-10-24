import { Badge, Button, PageHeader, Table, Toolbar, type Column } from '../../components/ui'
import { useIsSuper } from '../../lib/auth'
import { won } from '../../lib/format'

// 증정품 = 특전(products.item_kind=SPECIAL_GIFT). 판매도 하고 증정도 함
type Gift = {
  productId: number
  productName: string // 특전명
  productCode: string // 특전번호
  firstIncomingDate: string | null // 입고일
  incomingQty: number // 입고수량 (증정하면 줄어듦)
  soldQty: number // 판매개수
  costPrice: number
  sellingPrice: number
}

type GiftRule = {
  giftRuleId: number
  ruleType: 'GIFT' | 'FREE_SHIPPING' // 증정 / 무료배송
  minimumAmount: number // 상품 합계 기준
  giftQty: number | null // 증정 개수 (무료배송이면 null)
  giftSource: 'GOODS' | 'SPECIAL_GIFT' | 'BOTH' | null // 어디서 줄지 (무료배송이면 null)
  active: boolean
}

const sourceLabel = { GOODS: '재고상품', SPECIAL_GIFT: '특전', BOTH: '재고상품 + 특전' }

const stockOf = (g: Gift) => g.incomingQty - g.soldQty

const giftColumns: Column<Gift>[] = [
  { key: 'productName', title: '특전명' },
  { key: 'productCode', title: '특전번호' },
  { key: 'firstIncomingDate', title: '입고일' },
  { key: 'incomingQty', title: '입고수량', align: 'right' },
  { key: 'soldQty', title: '판매개수', align: 'right' },
  { key: 'stock', title: '재고개수', align: 'right', render: stockOf },
  { key: 'sellingPrice', title: '판매가', align: 'right', render: (g) => won(g.sellingPrice) },
  { key: 'profit', title: '순수익', align: 'right', render: (g) => won((g.sellingPrice - g.costPrice) * g.soldQty) },
]

const ruleColumns: Column<GiftRule>[] = [
  {
    key: 'ruleType', title: '구분',
    render: (r) => r.ruleType === 'GIFT' ? <Badge tone="success">증정</Badge> : <Badge tone="primary">무료배송</Badge>,
  },
  { key: 'minimumAmount', title: '조건 (상품 합계)', align: 'right', render: (r) => `${won(r.minimumAmount)} 이상` },
  { key: 'giftQty', title: '증정 개수', align: 'right', render: (r) => (r.giftQty ? `${r.giftQty}개` : '-') },
  { key: 'giftSource', title: '증정 대상', render: (r) => (r.giftSource ? sourceLabel[r.giftSource] : '-') },
  { key: 'active', title: '사용', align: 'center', render: (r) => <Badge tone={r.active ? 'success' : 'neutral'}>{r.active ? '사용' : '중지'}</Badge> },
]

export default function GiftsPage() {
  const isSuper = useIsSuper()
  // TODO: 증정품·증정 규칙 API 연결 후 rows에 데이터 전달

  return (
    <>
      <PageHeader title="증정품" actions={isSuper && <Button>새로 만들기</Button>}/>
      <div className="page-section">
        <Table columns={giftColumns} rows={[]} rowKey={(g) => g.productId} emptyText="등록된 증정품이 없습니다." />
      </div>

      <Toolbar actions={isSuper && <Button>새로 만들기</Button>}>
        <h2 className="section-title">증정 규칙</h2>
      </Toolbar>
      <Table columns={ruleColumns} rows={[]} rowKey={(r) => r.giftRuleId} emptyText="등록된 증정 규칙이 없습니다." />
    </>
  )
}
