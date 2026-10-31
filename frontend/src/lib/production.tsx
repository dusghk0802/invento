import { Badge } from '../components/ui'

// 발주·입고 화면 공통 타입·상태 표시
export type ProductionStatus = 'ORDERED' | 'CONFIRMED' | 'PARTIAL' | 'RECEIVED' | 'CANCELLED'
export type ProductionOrder = {
  productionOrderId: number
  supplierId: number
  supplierName: string
  supplierContact: string | null
  orderDate: string
  expectedDate: string | null // 입고 예정일 (희망)
  confirmedDate: string | null // 확정 납기일 (공장 회신)
  status: ProductionStatus
  delayed: boolean // 납기일(확정 납기일, 없으면 예정일) 지났는데 남은 수량 있음
  memo: string | null
  itemCount: number
  orderedQty: number
  receivedQty: number
  defectiveQty: number
  totalCost?: number // 사장에게만
}
export type ProductionItem = { productionOrderItemId: number; productId: number; orderedQty: number }

export const STATUS: Record<ProductionStatus, { label: string; tone: 'primary' | 'warning' | 'success' | 'neutral' }> = {
  ORDERED: { label: '공장 확인 대기', tone: 'neutral' },
  CONFIRMED: { label: '공장 확인', tone: 'primary' },
  PARTIAL: { label: '일부 입고', tone: 'warning' },
  RECEIVED: { label: '입고 완료', tone: 'success' },
  CANCELLED: { label: '취소', tone: 'neutral' },
}

export const statusBadge = (o: ProductionOrder) => (
  <>
    <Badge tone={STATUS[o.status].tone}>{STATUS[o.status].label}</Badge>
    {o.delayed && <> <Badge tone="danger">지연</Badge></>}
  </>
)
