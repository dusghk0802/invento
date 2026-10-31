import { useRef, useState } from 'react'
import { useNavigate } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import type { StockLocation } from '../components/StockChannel'
import { Badge, Button, Card, PageHeader, Table, type Column } from '../components/ui'
import { api, getErrorMessage, queryClient, useGet } from '../lib/api'
import { useMe } from '../lib/auth'
import { won } from '../lib/format'
import { PAYMENT, type PaymentMethod } from '../lib/sale'
import { useBarcodeScanner } from '../lib/useBarcodeScanner'

type Product = { productId: number; productCode: string; productName: string; sellingPrice: number; stocks: { locationId: number; onHandQty: number }[] }
type Line = { product: Product; quantity: number }

// 계산(POS): 계산대에 켜두는 화면. 바코드 스캐너로 찍거나(화면 어디서든) 검색해서 담고, 결제 완료하면 바로 다음 손님으로 초기화
// 저장하면 영수증이 생기고 매장 재고가 바로 빠짐 (서버 /store-sales)
export default function PosPage() {
  const navigate = useNavigate()
  const me = useMe().data
  const myStore = me?.location?.locationType === 'STORE' ? me.location.locationId : null
  const stores = useGet<StockLocation[]>('/locations').data?.filter((l) => l.active && l.locationType === 'STORE') ?? []
  const products = useGet<Product[]>('/products').data ?? []

  const [pickedStore, setPickedStore] = useState('')
  const locationId = String(myStore ?? (pickedStore || stores[0]?.locationId || ''))
  const [lines, setLines] = useState<Line[]>([])
  const [keyword, setKeyword] = useState('')
  const [payment, setPayment] = useState<PaymentMethod>('CARD')
  const [done, setDone] = useState('') // 방금 끝난 영수증 안내
  const [scanError, setScanError] = useState('')
  const inputRef = useRef<HTMLInputElement>(null)

  const k = keyword.trim().toLowerCase()
  const matches = k ? products.filter((p) => p.productCode.toLowerCase().includes(k) || p.productName.toLowerCase().includes(k)).slice(0, 8) : []
  const stockOf = (p: Product) => p.stocks.find((s) => String(s.locationId) === locationId)?.onHandQty ?? 0
  const total = lines.reduce((s, l) => s + l.product.sellingPrice * l.quantity, 0)

  function add(p: Product) {
    const found = lines.find((l) => l.product.productId === p.productId)
    setLines(found ? lines.map((l) => (l === found ? { ...l, quantity: l.quantity + 1 } : l)) : [...lines, { product: p, quantity: 1 }])
    setKeyword('')
    setDone('')
    setScanError('')
    inputRef.current?.focus()
  }

  // 스캔한 바코드(없으면 상품번호)로 서버에서 상품 조회 후 담기
  useBarcodeScanner((code) => {
    setKeyword('') // 검색칸에 커서가 있었으면 스캐너가 친 글자 지우기
    api.get<Product>(`/products/barcode/${encodeURIComponent(code)}`)
      .then((r) => add(r.data)).catch((e) => setScanError(`${code}: ${getErrorMessage(e)}`))
  })
  const changeQty = (id: number, delta: number) =>
    setLines(lines.map((l) => (l.product.productId === id ? { ...l, quantity: l.quantity + delta } : l)).filter((l) => l.quantity > 0))
  const clear = () => { setLines([]); setPayment('CARD'); inputRef.current?.focus() }

  const pay = useMutation({
    mutationFn: () => api.post<{ receipt: { orderNo: string; receivedAmount: number } }>('/store-sales', {
      locationId: Number(locationId), paymentMethod: payment,
      items: lines.map((l) => ({ productId: l.product.productId, quantity: l.quantity })),
    }),
    onSuccess: (res) => {
      setDone(`${res.data.receipt.orderNo} 결제 완료 · ${won(res.data.receipt.receivedAmount)}`)
      clear()
      queryClient.invalidateQueries({ queryKey: ['/store-sales'] })
      queryClient.invalidateQueries({ queryKey: ['/products'] })
    },
  })

  const columns: Column<Line>[] = [
    { key: 'name', title: '상품명', render: (l) => <>{l.product.productName} <span className="help-text">{l.product.productCode}</span></> },
    { key: 'price', title: '판매가', align: 'right', render: (l) => won(l.product.sellingPrice) },
    {
      key: 'qty', title: '수량', align: 'center',
      render: (l) => (
        <div className="row-actions">
          <Button variant="secondary" size="sm" onClick={() => changeQty(l.product.productId, -1)}>−</Button>
          <span style={{ minWidth: 24, color: l.quantity > stockOf(l.product) ? 'var(--color-primary)' : undefined }}>{l.quantity}</span>
          <Button variant="secondary" size="sm" onClick={() => changeQty(l.product.productId, 1)}>+</Button>
        </div>
      ),
    },
    { key: 'stock', title: '매장 재고', align: 'right', render: (l) => stockOf(l.product) },
    { key: 'amount', title: '금액', align: 'right', render: (l) => won(l.product.sellingPrice * l.quantity) },
    { key: 'remove', title: '', align: 'center', render: (l) => <Button variant="ghost" size="sm" onClick={() => changeQty(l.product.productId, -l.quantity)}>삭제</Button> },
  ]

  return (
    <>
      <PageHeader
        title="계산"
        actions={
          <>
            {myStore ? <Badge tone="primary">{me?.location?.locationName}</Badge> : (
              <select className="control-plain" value={locationId} onChange={(e) => { setPickedStore(e.target.value); clear() }}>
                {stores.map((l) => <option key={l.locationId} value={l.locationId}>{l.locationName}</option>)}
              </select>
            )}
            <Button variant="secondary" onClick={() => navigate('/admin/store-sales')}>판매 내역</Button>
          </>
        }
      />
      <div className="pos">
        <Card className="form-stack">
          <Badge tone="success">바코드 스캔 대기 중</Badge>
          <input ref={inputRef} autoFocus className="control-plain pos-search" placeholder="상품번호·상품명 검색"
                 value={keyword} onChange={(e) => setKeyword(e.target.value)}
                 onKeyDown={(e) => {
                   if (e.key !== 'Enter') return
                   const exact = products.find((p) => p.productCode.toLowerCase() === k)
                   if (exact || matches.length === 1) add(exact ?? matches[0])
                 }} />
          {matches.map((p) => (
            <button key={p.productId} type="button" className="control-plain pos-match" onClick={() => add(p)}>
              {p.productName} <span className="help-text">({p.productCode}) · {won(p.sellingPrice)} · 매장 재고 {stockOf(p)}</span>
            </button>
          ))}
          <Table columns={columns} rows={lines} rowKey={(l) => l.product.productId} emptyText="상품을 찍거나 검색해서 담아 주세요." />
        </Card>

        <Card className="form-stack">
          <div className="pos-row"><span>상품 합계</span><strong>{won(total)}</strong></div>
          <div className="row-actions">
            {(Object.keys(PAYMENT) as PaymentMethod[]).map((m) => (
              <Button key={m} variant={payment === m ? 'primary' : 'secondary'} onClick={() => setPayment(m)}>{PAYMENT[m]}</Button>
            ))}
          </div>
          <div className="pos-row pos-total"><span>받을 금액</span><strong>{won(total)}</strong></div>
          <Button block disabled={lines.length === 0 || pay.isPending} onClick={() => pay.mutate()}>결제 완료</Button>
          <Button block variant="secondary" disabled={lines.length === 0} onClick={clear}>비우기</Button>
          {pay.isError && <p className="form-error">{getErrorMessage(pay.error)}</p>}
          {scanError && <p className="form-error">{scanError}</p>}
          {done && <p className="help-text">{done}</p>}
        </Card>
      </div>
    </>
  )
}
