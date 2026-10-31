import { useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { Badge, Button, Input, Modal, MultiSelect, PageHeader, Select, Table, Toolbar, type Column } from '../components/ui'
import { api, getErrorMessage, queryClient } from '../lib/api'
import { useIsOwner } from '../lib/auth'
import { tagTone, won } from '../lib/format'
import { ProductStockTable } from './ProductsPage'

type TypeRef = { productTypeId: number; productTypeName: string }
type GiftRule = {
  giftRuleId: number
  minimumAmount: number // 상품 합계 기준 (배송비 제외)
  giftQty: number // 정확히 이 개수만큼 선택
  freeShipping: boolean // 온라인 주문만
  active: boolean
  productTypes: TypeRef[] // 증정 대상 상품종류
}
type ProductType = TypeRef & { active: boolean }

// 증정품 화면: 위 = 증정 규칙(금액 구간별 대상 상품종류), 아래 = 특전 재고 (특전 등록·재고 관리)
// 재고품은 상품 화면에 있고, 주문 처리할 때 직원이 대상 종류의 재고 있는 상품(재고품·특전) 중에서 골라 증정
export default function GiftsPage() {
  const isOwner = useIsOwner()
  const [editing, setEditing] = useState<GiftRule | 'new' | null>(null)

  const rules = useQuery({ queryKey: ['/gift-rules'], queryFn: () => api.get<GiftRule[]>('/gift-rules').then((r) => r.data) })

  const ruleColumns: Column<GiftRule>[] = [
    { key: 'minimumAmount', title: '조건 (상품 합계)', align: 'right', render: (r) => `${won(r.minimumAmount)} 이상` },
    { key: 'giftQty', title: '증정 개수', align: 'right', render: (r) => (r.giftQty ? `${r.giftQty}개` : '-') },
    { key: 'freeShipping', title: '무료배송', align: 'center', render: (r) => (r.freeShipping ? <Badge tone="primary">무료배송</Badge> : '-') },
    { key: 'productTypes', title: '증정 대상 상품종류', render: (r) => <div className="row-actions">{r.productTypes.map((t) => <Badge key={t.productTypeId} tone={tagTone(t.productTypeName)}>{t.productTypeName}</Badge>)}</div> },
    { key: 'active', title: '사용', align: 'center', render: (r) => <Badge tone={r.active ? 'success' : 'neutral'}>{r.active ? '사용' : '중지'}</Badge> },
    ...(isOwner ? [{
      key: 'actions', title: '관리', align: 'center' as const,
      render: (r: GiftRule) => <Button variant="secondary" size="sm" onClick={() => setEditing(r)}>수정</Button>,
    }] : []),
  ]

  return (
    <>
      <PageHeader title="증정품" />

      <div className="page-section">
        <Toolbar actions={isOwner && <Button onClick={() => setEditing('new')}>+ 추가</Button>}>
          <h2 className="section-title">증정 규칙</h2>
        </Toolbar>
        <p className="help-text">상품 합계가 조건 이상이면 가장 높은 구간 하나만 적용돼요. 대상 상품종류 안에서 주문 처리할 때 직원이 상품을 골라요.</p>
        <Table columns={ruleColumns} rows={rules.data} rowKey={(r) => r.giftRuleId} loading={rules.isPending}
               emptyText="등록된 증정 규칙이 없습니다." />
      </div>

      <ProductStockTable itemKind="SPECIAL_GIFT" section />

      {editing && <GiftRuleModal rule={editing === 'new' ? null : editing} onClose={() => setEditing(null)} />}
    </>
  )
}

// 증정 구간 등록·수정 (사장만): 금액 조건, 증정 개수, 무료배송, 대상 상품종류(여러 개)
function GiftRuleModal({ rule, onClose }: { rule: GiftRule | null; onClose: () => void }) {
  const [form, setForm] = useState({
    minimumAmount: String(rule?.minimumAmount ?? ''),
    giftQty: String(rule?.giftQty ?? ''),
    freeShipping: String(rule?.freeShipping ?? false),
    active: String(rule?.active ?? true),
  })
  const [typeIds, setTypeIds] = useState<number[]>(rule?.productTypes.map((t) => t.productTypeId) ?? [])
  const set = (patch: Partial<typeof form>) => setForm({ ...form, ...patch })

  const types = useQuery({ queryKey: ['/product-types'], queryFn: () => api.get<ProductType[]>('/product-types').then((r) => r.data) })

  const save = useMutation({
    mutationFn: () => {
      const body = {
        minimumAmount: Number(form.minimumAmount), giftQty: Number(form.giftQty || 0),
        freeShipping: form.freeShipping === 'true', active: form.active === 'true', productTypeIds: typeIds,
      }
      return rule ? api.put(`/gift-rules/${rule.giftRuleId}`, body) : api.post('/gift-rules', body)
    },
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['/gift-rules'] }); onClose() },
  })

  return (
    <Modal open title={rule ? '증정 규칙 수정' : '증정 규칙 등록'} onClose={onClose}
           footer={
             <>
               <Button variant="secondary" onClick={onClose}>취소</Button>
               <Button type="submit" form="gift-form" disabled={save.isPending}>저장</Button>
             </>
           }>
      <form id="gift-form" className="form-stack" onSubmit={(e) => { e.preventDefault(); save.mutate() }}>
        <Input label="조건 (상품 합계, 원 이상)" type="number" min={0} required placeholder="예: 30000" value={form.minimumAmount}
               onChange={(e) => set({ minimumAmount: e.target.value })} />
        <Input label="증정 개수" type="number" min={0} required placeholder="예: 2" value={form.giftQty}
               onChange={(e) => set({ giftQty: e.target.value })} />
        <Select label="무료배송 (온라인 주문)" value={form.freeShipping} onChange={(e) => set({ freeShipping: e.target.value })}
                options={[{ value: 'false', label: '아니요' }, { value: 'true', label: '무료배송' }]} />
        <Select label="사용" value={form.active} onChange={(e) => set({ active: e.target.value })}
                options={[{ value: 'true', label: '사용' }, { value: 'false', label: '중지' }]} />

        <MultiSelect label="증정 대상 상품종류" value={typeIds} onChange={setTypeIds}
                     placeholder="상품종류 선택 (여러 개)"
                     options={(types.data ?? []).filter((t) => t.active || typeIds.includes(t.productTypeId))
                       .map((t) => ({ value: t.productTypeId, label: t.productTypeName }))} />
        {save.isError && <p className="form-error">{getErrorMessage(save.error)}</p>}
      </form>
    </Modal>
  )
}
