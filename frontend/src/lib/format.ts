// 금액: ₩1,234 (소수점 있는 금액도 그대로 표시)
export const won = (n: number | null | undefined) => (n == null ? '-' : `${n < 0 ? '-' : ''}₩${Math.abs(n).toLocaleString()}`)

// 주문번호: 1 → 001
export const orderNo = (id: number) => String(id).padStart(3, '0')

// 태그 색: 같은 글자는 항상 같은 색 (노션 태그처럼)
const tones = ['primary', 'success', 'warning', 'danger', 'neutral'] as const
export const tagTone = (text: string) => tones[[...text].reduce((sum, c) => sum + c.charCodeAt(0), 0) % tones.length]

// 오늘 날짜 (내 PC 시간 기준) YYYY-MM-DD: date input 기본값용
export const today = () => new Date().toLocaleDateString('sv-SE')

// 일시: 2026-10-31T14:05:12 → 2026-10-31 14:05
export const dateTime = (s: string | null | undefined) => (s ? s.replace('T', ' ').slice(0, 16) : '-')
