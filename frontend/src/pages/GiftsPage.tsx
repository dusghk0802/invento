import { Badge, Button, Table, Toolbar, type Column } from '../components/ui'
import { useIsOwner } from '../lib/auth'
import { won } from '../lib/format'
import { ProductStockPage } from './ProductsPage'

// 증정 규칙 (7단계에서 API 연결): 조건을 만족하는 가장 높은 구간 하나만 적용
type GiftRule = {
  giftRuleId: number
  minimumAmount: number // 상품 합계 기준 (배송비 제외)
  giftQty: number // 정확히 이 개수만큼 선택
  freeShipping: boolean // 온라인 주문만
  productTypeNames: string[] // 증정 대상 상품종류
  active: boolean
}

const ruleColumns: Column<GiftRule>[] = [
  { key: 'minimumAmount', title: '조건 (상품 합계)', align: 'right', render: (r) => `${won(r.minimumAmount)} 이상` },
  { key: 'giftQty', title: '증정 개수', align: 'right', render: (r) => `${r.giftQty}개` },
  { key: 'freeShipping', title: '무료배송', align: 'center', render: (r) => (r.freeShipping ? <Badge tone="primary">무료배송</Badge> : '-') },
  { key: 'productTypeNames', title: '증정 대상', render: (r) => r.productTypeNames.map((n) => <Badge key={n}>{n}</Badge>) },
  { key: 'active', title: '사용', align: 'center', render: (r) => <Badge tone={r.active ? 'success' : 'neutral'}>{r.active ? '사용' : '중지'}</Badge> },
]

// 증정품 = 특전(item_kind=SPECIAL_GIFT). 재고 표는 상품 화면과 같음 (창고/매장 필터)
export default function GiftsPage() {
  const isOwner = useIsOwner()

  return (
    <>
      <div className="page-section">
        <ProductStockPage title="증정품" itemKind="SPECIAL_GIFT" />
      </div>

      <Toolbar actions={isOwner && <Button>+ 추가</Button>}>
        <h2 className="section-title">증정 규칙</h2>
      </Toolbar>
      {/* TODO: 7단계 증정 규칙 API 연결 */}
      <Table columns={ruleColumns} rows={[]} rowKey={(r) => r.giftRuleId} emptyText="등록된 증정 규칙이 없습니다." />
    </>
  )
}
