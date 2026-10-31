import { useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Badge, Button, Input, Modal, PageHeader, Select, Table, Toolbar, type Column } from '../components/ui'
import { api, getErrorMessage, queryClient } from '../lib/api'
import { useStockChannel, type StockLocation } from '../components/StockChannel'
import { AdjustModal, HistoryModal } from '../components/StockModals'
import { won } from '../lib/format'

export type ItemKind = 'GOODS' | 'SPECIAL_GIFT' // 재고품 / 특전 (특전은 증정품 화면에서 관리)
type Stock = { locationId: number; locationName: string; onHandQty: number; reservedQty: number; availableQty: number; minimumQty: number }
type Product = {
  productId: number
  itemKind: ItemKind
  productCode: string
  seriesId: number | null
  seriesName: string | null
  productTypeId: number
  productTypeName: string
  productName: string
  imageUrl: string | null
  description: string | null
  shopVisible: boolean
  costPrice?: number // 사장에게만 내려옴
  sellingPrice: number
  releaseDate: string | null
  boxQty: number | null // 박스 입수량
  stocks: Stock[]
}
type Series = { seriesId: number; seriesName: string; active: boolean }
type ProductType = { productTypeId: number; productTypeName: string; active: boolean }

const stockAt = (p: Product, locationId: number) => p.stocks.find((s) => s.locationId === locationId)
const sumAt = (p: Product, locs: StockLocation[], key: 'onHandQty' | 'reservedQty' | 'availableQty') =>
  locs.reduce((sum, l) => sum + (stockAt(p, l.locationId)?.[key] ?? 0), 0)

const useList = <T,>(url: string) => useQuery({ queryKey: [url], queryFn: () => api.get<T[]>(url).then((r) => r.data) })

export default function ProductsPage() {
  return <ProductStockTable itemKind="GOODS" />
}

// 상품(재고품)·특전 재고 표 공통. section이면 화면 안의 한 구역으로 표시 (증정품 화면의 특전 표)
export function ProductStockTable({ itemKind, section }: { itemKind: ItemKind; section?: boolean }) {
  const noun = itemKind === 'GOODS' ? '상품' : '특전'
  // 사장·매장 직원은 필터로 창고/매장 표를 전환, 창고·출고 직원은 창고 표만
  const { me, isOwner, channel, channelLocations, adjustLocations, filter } = useStockChannel(['OWNER', 'STORE'])
  const canAdjust = isOwner || me?.role === 'STORE' || me?.role === 'WAREHOUSE'

  const [search, setSearch] = useState({ keyword: '', seriesId: '', productTypeId: '' })
  const [editing, setEditing] = useState<Product | 'new' | null>(null)
  const [adjusting, setAdjusting] = useState<Product | null>(null)
  const [history, setHistory] = useState<Product | null>(null)

  const series = useList<Series>('/series')
  const types = useList<ProductType>('/product-types')
  const products = useQuery({
    queryKey: ['/products', search, itemKind],
    queryFn: () => api.get<Product[]>('/products', {
      params: { ...Object.fromEntries(Object.entries(search).filter(([, v]) => v !== '')), itemKind }, // 빈 조건은 보내지 않음
    }).then((r) => r.data),
  })

  const total = (p: Product) => sumAt(p, channelLocations, 'onHandQty')

  const common: Column<Product>[] = [
    { key: 'seriesName', title: '시리즈명', render: (p) => p.seriesName && <Badge tone="primary">{p.seriesName}</Badge> },
    { key: 'productName', title: `${noun}명` },
    { key: 'productCode', title: `${noun}번호` },
    { key: 'productTypeName', title: '상품종류', render: (p) => <Badge>{p.productTypeName}</Badge> },
  ]

  // 위치마다 재고 컬럼 1개. 최소 재고 이하이면 빨간색
  const locationColumns: Column<Product>[] = channelLocations.map((l) => ({
    key: `loc-${l.locationId}`, title: l.locationName, align: 'right',
    render: (p) => {
      const s = stockAt(p, l.locationId)
      const qty = (channel === 'ONLINE' ? s?.availableQty : s?.onHandQty) ?? 0
      const low = !!s && s.minimumQty > 0 && qty <= s.minimumQty
      return <span style={{ color: low ? 'var(--color-primary)' : undefined }}>{qty}</span>
    },
  }))

  // 온라인 표: 창고 재고 = 보유 - 주문 예약 = 판매 가능
  const onlineColumns: Column<Product>[] = [
    { key: 'onHand', title: '재고수량', align: 'right', render: total, summary: (rows) => rows.reduce((s, p) => s + total(p), 0) },
    { key: 'reserved', title: '주문예약수량', align: 'right', render: (p) => sumAt(p, channelLocations, 'reservedQty') },
    { key: 'available', title: '판매가능수량', align: 'right', render: (p) => sumAt(p, channelLocations, 'availableQty') },
    { key: 'boxQty', title: '박스(개수)', align: 'right', render: (p) => (p.boxQty ? `${p.boxQty}개` : '-') },
  ]

  // 오프라인 표: 매장별 보유 수량만 (매장 판매는 바로 차감이라 예약 없음). 합계는 매장이 2개 이상일 때만
  const offlineColumns: Column<Product>[] = channelLocations.length > 1 ? [
    { key: 'onHand', title: '매장 재고 합계', align: 'right', render: total, summary: (rows) => rows.reduce((s, p) => s + total(p), 0) },
  ] : []

  const columns: Column<Product>[] = [
    ...common,
    ...locationColumns,
    ...(channel === 'ONLINE' ? onlineColumns : offlineColumns),
    ...(isOwner ? [{ key: 'costPrice', title: '원가', align: 'right' as const, render: (p: Product) => won(p.costPrice ?? 0) }] : []),
    { key: 'sellingPrice', title: '판매가', align: 'right', render: (p) => won(p.sellingPrice) },
    ...(isOwner ? [{
      key: 'remain', title: '남은 금액', align: 'right' as const,
      render: (p: Product) => won((p.costPrice ?? 0) * total(p)),
      summary: (rows: Product[]) => won(rows.reduce((s, p) => s + (p.costPrice ?? 0) * total(p), 0)),
    }] : []),
    { key: 'releaseDate', title: '발매일' },
    {
      key: 'status', title: '상태', align: 'center',
      render: (p) => (total(p) > 0 ? <Badge tone="success">재고 있음</Badge> : <Badge tone="danger">품절</Badge>),
    },
    {
      key: 'actions', title: '관리', align: 'center',
      render: (p) => (
        <div className="row-actions">
          {canAdjust && adjustLocations.length > 0 && <Button variant="ghost" size="sm" onClick={() => setAdjusting(p)}>재고 조정</Button>}
          <Button variant="ghost" size="sm" onClick={() => setHistory(p)}>이력</Button>
          {isOwner && <Button variant="secondary" size="sm" onClick={() => setEditing(p)}>수정</Button>}
        </div>
      ),
    },
  ]

  return (
    <>
      {section ? (
        <Toolbar actions={isOwner && <Button onClick={() => setEditing('new')}>+ 추가</Button>}>
          <h2 className="section-title">증정 대상 재고 ({noun})</h2>
        </Toolbar>
      ) : (
        <PageHeader title={noun} actions={isOwner && <Button onClick={() => setEditing('new')}>+ 추가</Button>} />
      )}
      <Toolbar>
        {filter}
        <input className="control-plain" placeholder={`${noun}명·${noun}번호 검색`} value={search.keyword}
               onChange={(e) => setSearch({ ...search, keyword: e.target.value })} />
        <select className="control-plain" value={search.seriesId} onChange={(e) => setSearch({ ...search, seriesId: e.target.value })}>
          <option value="">전체 시리즈</option>
          {series.data?.map((s) => <option key={s.seriesId} value={s.seriesId}>{s.seriesName}</option>)}
        </select>
        <select className="control-plain" value={search.productTypeId} onChange={(e) => setSearch({ ...search, productTypeId: e.target.value })}>
          <option value="">전체 상품종류</option>
          {types.data?.map((t) => <option key={t.productTypeId} value={t.productTypeId}>{t.productTypeName}</option>)}
        </select>
      </Toolbar>
      <Table key={channel} columns={columns} rows={products.data} rowKey={(p) => p.productId} loading={products.isPending}
             emptyText={`등록된 ${noun}이 없습니다.`} />

      {editing && <ProductModal product={editing === 'new' ? null : editing} itemKind={itemKind} noun={noun} series={series.data ?? []}
                                types={types.data ?? []} onClose={() => setEditing(null)} />}
      {adjusting && <AdjustModal name={adjusting.productName} stocks={adjusting.stocks} locations={adjustLocations}
                                 url="/stocks/adjust" target={{ productId: adjusting.productId }} invalidate="/products"
                                 onClose={() => setAdjusting(null)} />}
      {history && <HistoryModal name={history.productName} query={`productId=${history.productId}`} onClose={() => setHistory(null)} />}
    </>
  )
}

const NEW = '__new__'

// 상품 등록·수정 (사장만). 시리즈·상품종류는 목록에 없으면 "+ 새로 만들기"로 바로 추가
function ProductModal({ product, itemKind, noun, series, types, onClose }: {
  product: Product | null; itemKind: ItemKind; noun: string; series: Series[]; types: ProductType[]; onClose: () => void
}) {
  const [form, setForm] = useState({
    itemKind: product?.itemKind ?? itemKind, // 구분은 화면으로 정해짐 (상품 화면 = 재고품, 증정품 화면 = 특전)
    productCode: product?.productCode ?? '',
    productName: product?.productName ?? '',
    seriesId: product?.seriesId ? String(product.seriesId) : '',
    productTypeId: product ? String(product.productTypeId) : '',
    costPrice: String(product?.costPrice ?? ''),
    sellingPrice: String(product?.sellingPrice ?? ''),
    releaseDate: product?.releaseDate ?? '',
    boxQty: String(product?.boxQty ?? ''),
    shopVisible: String(product?.shopVisible ?? false),
    imageUrl: product?.imageUrl ?? '',
    description: product?.description ?? '',
  })
  const set = (patch: Partial<typeof form>) => setForm({ ...form, ...patch })

  const save = useMutation({
    mutationFn: () => {
      const body = {
        ...form,
        seriesId: form.seriesId ? Number(form.seriesId) : null,
        productTypeId: Number(form.productTypeId),
        costPrice: Number(form.costPrice),
        sellingPrice: Number(form.sellingPrice),
        releaseDate: form.releaseDate || null,
        boxQty: form.boxQty ? Number(form.boxQty) : null,
        shopVisible: form.shopVisible === 'true',
        imageUrl: form.imageUrl || null,
        description: form.description || null,
      }
      return product ? api.put(`/products/${product.productId}`, body) : api.post('/products', body)
    },
    onSuccess: done,
  })
  const remove = useMutation({ mutationFn: () => api.delete(`/products/${product!.productId}`), onSuccess: done })
  const create = useMutation({
    mutationFn: ({ url, body }: { url: string; body: object }) => api.post(url, body).then((r) => r.data),
  })

  function done() {
    queryClient.invalidateQueries({ queryKey: ['/products'] })
    onClose()
  }

  // 선택지에서 "+ 새로 만들기"를 고르면 이름을 입력받아 바로 등록하고 선택
  async function pickOrCreate(value: string, kind: 'series' | 'type') {
    if (value !== NEW) return set(kind === 'series' ? { seriesId: value } : { productTypeId: value })
    const name = window.prompt(kind === 'series' ? '새 시리즈 이름' : '새 상품종류 이름')?.trim()
    if (!name) return
    if (kind === 'series') {
      const s = await create.mutateAsync({ url: '/series', body: { seriesName: name } })
      queryClient.invalidateQueries({ queryKey: ['/series'] })
      set({ seriesId: String(s.seriesId) })
    } else {
      const t = await create.mutateAsync({ url: '/product-types', body: { productTypeName: name, parentId: null, active: true } })
      queryClient.invalidateQueries({ queryKey: ['/product-types'] })
      set({ productTypeId: String(t.productTypeId) })
    }
  }

  const error = save.error ?? remove.error ?? create.error

  return (
    <Modal open width={560} title={product ? `${noun} 수정` : `${noun} 등록`} onClose={onClose}
           footer={
             <>
               {product && <Button variant="ghost" style={{ marginRight: 'auto' }} disabled={remove.isPending}
                                   onClick={() => window.confirm(`이 ${noun}을 삭제할까요?`) && remove.mutate()}>삭제</Button>}
               <Button variant="secondary" onClick={onClose}>취소</Button>
               <Button type="submit" form="product-form" disabled={save.isPending}>저장</Button>
             </>
           }>
      <form id="product-form" className="form-stack" onSubmit={(e) => { e.preventDefault(); save.mutate() }}>
        <Input label={`${noun}명`} required maxLength={200} value={form.productName} onChange={(e) => set({ productName: e.target.value })} />
        <Input label={`${noun}번호`} required maxLength={50} value={form.productCode} onChange={(e) => set({ productCode: e.target.value })} />
        <Select label="시리즈" value={form.seriesId} placeholder="없음" onChange={(e) => pickOrCreate(e.target.value, 'series')}
                options={[...series.filter((s) => s.active || String(s.seriesId) === form.seriesId)
                  .map((s) => ({ value: s.seriesId, label: s.seriesName })), { value: NEW, label: '+ 새로 만들기' }]} />
        <Select label="상품종류" required value={form.productTypeId} placeholder="선택" onChange={(e) => pickOrCreate(e.target.value, 'type')}
                options={[...types.filter((t) => t.active || String(t.productTypeId) === form.productTypeId)
                  .map((t) => ({ value: t.productTypeId, label: t.productTypeName })), { value: NEW, label: '+ 새로 만들기' }]} />
        <Input label="원가" type="number" min={0} required value={form.costPrice} onChange={(e) => set({ costPrice: e.target.value })} />
        <Input label="판매가" type="number" min={0} required value={form.sellingPrice} onChange={(e) => set({ sellingPrice: e.target.value })} />
        <Input label="박스 입수량" type="number" min={1} placeholder="1박스에 몇 개인지 (매장 출고 단위)" value={form.boxQty}
               onChange={(e) => set({ boxQty: e.target.value })} />
        <Input label="발매일" type="date" value={form.releaseDate} onChange={(e) => set({ releaseDate: e.target.value })} />
        <Select label="쇼핑몰 노출" value={form.shopVisible} onChange={(e) => set({ shopVisible: e.target.value })}
                options={[{ value: 'false', label: '숨김' }, { value: 'true', label: '노출' }]} />
        <Input label="이미지 URL" maxLength={1000} value={form.imageUrl} onChange={(e) => set({ imageUrl: e.target.value })} />
        <Input label="상세 설명" value={form.description} onChange={(e) => set({ description: e.target.value })} />
        {error && <p className="form-error">{getErrorMessage(error)}</p>}
      </form>
    </Modal>
  )
}
