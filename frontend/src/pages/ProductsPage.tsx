import { Badge, Button, PageHeader, Table, type Column } from '../components/ui'
import { useIsOwner } from '../lib/auth'
import { won } from '../lib/format'

// 상품 API 응답 모양 (3단계 백엔드에서 이 형태로 만들 예정)
type Product = {
  productId: number
  seriesName: string | null
  productName: string
  productCode: string
  productTypeName: string
  incomingQty: number
  soldQty: number
  costPrice: number
  sellingPrice: number
  releaseDate: string | null
  purchaseSiteName: string | null
  purchaseDate: string | null
  firstIncomingDate: string | null
}

const stockOf = (p: Product) => p.incomingQty - p.soldQty

// 시리즈, 상품종류, 재고, 구매·입고는 별도 화면 없이 이 표의 컬럼으로 표시
const columns: Column<Product>[] = [
  { key: 'seriesName', title: '시리즈명', render: (p) => p.seriesName && <Badge tone="primary">{p.seriesName}</Badge> },
  { key: 'productName', title: '상품명' },
  { key: 'productCode', title: '상품번호' },
  { key: 'productTypeName', title: '상품종류', render: (p) => <Badge>{p.productTypeName}</Badge> },
  { key: 'incomingQty', title: '입고수량', align: 'right' },
  { key: 'soldQty', title: '판매량', align: 'right' },
  { key: 'stock', title: '재고수량', align: 'right', render: stockOf },
  { key: 'costPrice', title: '원가', align: 'right', render: (p) => won(p.costPrice) },
  { key: 'sellingPrice', title: '판매가', align: 'right', render: (p) => won(p.sellingPrice) },
  { key: 'profit', title: '순매출 추정', align: 'right', render: (p) => won((p.sellingPrice - p.costPrice) * p.soldQty) },
  { key: 'remain', title: '남은 금액', align: 'right', render: (p) => won(p.costPrice * stockOf(p)) },
  { key: 'releaseDate', title: '발매일' },
  { key: 'purchaseSiteName', title: '구매사이트' },
  { key: 'purchaseDate', title: '구매일' },
  { key: 'firstIncomingDate', title: '입고일' },
  {
    key: 'status', title: '상태',
    render: (p) => (stockOf(p) > 0 ? <Badge tone="success">재고 있음</Badge> : <Badge tone="danger">품절</Badge>),
  },
]

export default function ProductsPage() {
  const isOwner = useIsOwner()

  return (
    <>
      <PageHeader title="상품재고" actions={isOwner && <Button>새로 만들기</Button>} />
      {/* TODO: 상품 API 연결 후 rows에 데이터 전달 */}
      <Table columns={columns} rows={[]} rowKey={(p) => p.productId} emptyText="등록된 상품이 없습니다." />
    </>
  )
}
