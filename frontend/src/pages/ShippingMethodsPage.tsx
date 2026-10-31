import { useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import CrudPage from '../components/CrudPage'
import { Button, Modal } from '../components/ui'
import { api, getErrorMessage, queryClient } from '../lib/api'
import { useIsOwner } from '../lib/auth'
import { won } from '../lib/format'

type ShippingMethod = { shippingMethodId: number; shippingMethodName: string; defaultFee: number; active: boolean }
type ProductType = { productTypeId: number; productTypeName: string }
type FeeRule = { productTypeId: number; fee: number | null; allowed: boolean }

export default function ShippingMethodsPage() {
  const isOwner = useIsOwner()
  const [ruleTarget, setRuleTarget] = useState<ShippingMethod | null>(null)

  return (
    <>
      <CrudPage<ShippingMethod>
        title="배송방법"
        endpoint="/shipping-methods"
        idKey="shippingMethodId"
        canWrite={isOwner}
        emptyText="등록된 배송방법이 없습니다."
        columns={[
          { key: 'shippingMethodName', title: '배송방법' },
          { key: 'defaultFee', title: '기본 배송비', align: 'right', render: (m) => won(m.defaultFee) },
        ]}
        fields={[
          { name: 'shippingMethodName', label: '배송방법', required: true, maxLength: 100, placeholder: '예: CJ대한통운, 반택(CU)' },
          { name: 'defaultFee', label: '기본 배송비 (고객 부담)', type: 'number', required: true, placeholder: '예: 3900' },
          { name: 'active', label: '사용', type: 'active' },
        ]}
        rowActions={(m) => <Button variant="ghost" size="sm" onClick={() => setRuleTarget(m)}>상품종류 규칙</Button>}
      />
      {ruleTarget && <FeeRulesModal method={ruleTarget} canWrite={isOwner} onClose={() => setRuleTarget(null)} />}
    </>
  )
}

// 상품종류별 배송 규칙 (예: 포스터 → 4000원 / 선택 불가)
function FeeRulesModal({ method, canWrite, onClose }: { method: ShippingMethod; canWrite: boolean; onClose: () => void }) {
  const url = `/shipping-methods/${method.shippingMethodId}/fee-rules`
  const [rules, setRules] = useState<FeeRule[] | null>(null)

  const types = useQuery({ queryKey: ['/product-types'], queryFn: () => api.get<ProductType[]>('/product-types').then((r) => r.data) })
  const saved = useQuery({ queryKey: [url], queryFn: () => api.get<FeeRule[]>(url).then((r) => r.data) })
  const rows = rules ?? saved.data ?? [] // 수정을 시작하면 화면의 값(rules), 아니면 저장된 값

  const save = useMutation({
    mutationFn: () => api.put(url, rows),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: [url] }); onClose() },
  })

  const change = (i: number, patch: Partial<FeeRule>) => setRules(rows.map((r, idx) => (idx === i ? { ...r, ...patch } : r)))

  return (
    <Modal
      open
      width={560}
      title={`${method.shippingMethodName} · 상품종류 규칙`}
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>닫기</Button>
          {canWrite && <Button onClick={() => save.mutate()} disabled={save.isPending}>저장</Button>}
        </>
      }
    >
      <p className="help-text">
        이 종류가 주문에 있으면 배송비를 바꾸거나 선택을 막아요. 배송비를 비우면 기본 배송비({won(method.defaultFee)})를 써요.
      </p>
      {rows.map((r, i) => (
        <div key={i} className="rule-row">
          <select className="control-plain" value={r.productTypeId} disabled={!canWrite}
                  onChange={(e) => change(i, { productTypeId: Number(e.target.value) })}>
            <option value="">상품종류 선택</option>
            {types.data?.map((t) => <option key={t.productTypeId} value={t.productTypeId}>{t.productTypeName}</option>)}
          </select>
          <input className="control-plain" type="number" placeholder="기본" value={r.fee ?? ''} disabled={!canWrite}
                 onChange={(e) => change(i, { fee: e.target.value === '' ? null : Number(e.target.value) })} />
          <select className="control-plain" value={String(r.allowed)} disabled={!canWrite}
                  onChange={(e) => change(i, { allowed: e.target.value === 'true' })}>
            <option value="true">선택 가능</option>
            <option value="false">선택 불가</option>
          </select>
          {canWrite && <Button variant="ghost" size="sm" onClick={() => setRules(rows.filter((_, idx) => idx !== i))}>삭제</Button>}
        </div>
      ))}
      {rows.length === 0 && <p className="help-text">등록된 규칙이 없습니다.</p>}
      {canWrite && (
        <Button variant="secondary" size="sm" disabled={!types.data?.length}
                onClick={() => setRules([...rows, { productTypeId: types.data![0].productTypeId, fee: null, allowed: true }])}>
          + 규칙 추가
        </Button>
      )}
      {!types.data?.length && <p className="help-text">상품종류를 먼저 등록해야 규칙을 추가할 수 있어요.</p>}
      {save.isError && <p className="form-error">{getErrorMessage(save.error)}</p>}
    </Modal>
  )
}
