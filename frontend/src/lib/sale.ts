// 매장 판매 공통
export type PaymentMethod = 'CARD' | 'CASH' | 'TRANSFER'
export const PAYMENT: Record<PaymentMethod, string> = { CARD: '카드', CASH: '현금', TRANSFER: '계좌이체' }
