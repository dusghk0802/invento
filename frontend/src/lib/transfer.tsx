import { Badge } from '../components/ui'

// 재고 이동(매장 출고) 화면 공통 타입·상태 표시
export type TransferStatus = 'REQUESTED' | 'APPROVED' | 'REJECTED' | 'SHIPPED' | 'RECEIVED' | 'CANCELLED'
export type Transfer = {
  stockTransferId: number
  fromLocationId: number
  fromLocationName: string
  toLocationId: number
  toLocationName: string
  status: TransferStatus
  itemCount: number
  totalQty: number
  requestedAt: string
  decidedAt: string | null // 승인·반려일
  rejectReason: string | null
  shippedAt: string | null
  receivedAt: string | null
  memo: string | null
}

export const TRANSFER_STATUS: Record<TransferStatus, { label: string; tone: 'primary' | 'warning' | 'success' | 'neutral' | 'danger' }> = {
  REQUESTED: { label: '승인 대기', tone: 'warning' },
  APPROVED: { label: '출고 준비', tone: 'primary' },
  REJECTED: { label: '반려', tone: 'danger' },
  SHIPPED: { label: '배송 중', tone: 'primary' },
  RECEIVED: { label: '도착 완료', tone: 'success' },
  CANCELLED: { label: '취소', tone: 'neutral' },
}

export const transferBadge = (t: Transfer) => <Badge tone={TRANSFER_STATUS[t.status].tone}>{TRANSFER_STATUS[t.status].label}</Badge>
