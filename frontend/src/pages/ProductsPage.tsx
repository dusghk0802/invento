import { ProductStockTable } from '../components/ProductStockTable'

// 상품 화면 = 재고품만 (특전은 증정품 화면)
export default function ProductsPage() {
  return <ProductStockTable itemKind="GOODS" />
}
