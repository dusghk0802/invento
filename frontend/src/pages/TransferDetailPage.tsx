import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import { Button, Card, FormModal, InfoGrid, Input, PageHeader, Table, type Column } from '../components/ui'
import { api, getErrorMessage, queryClient, useGet } from '../lib/api'
import { useMe } from '../lib/auth'
import { dateTime, orderNo } from '../lib/format'
import { transferBadge, type Transfer } from '../lib/transfer'

type Item = {
  stockTransferItemId: number; productId: number; productCode: string; productName: string
  boxQty: number | null; boxCount: number; looseQty: number; quantity: number
}
type Detail = { transfer: Transfer; items: Item[] }

const itemColumns: Column<Item>[] = [
  { key: 'productName', title: '상품명' },
  { key: 'productCode', title: '상품번호' },
  { key: 'boxQty', title: '박스 입수', align: 'right', render: (i) => (i.boxQty ? `${i.boxQty}개` : '-') },
  { key: 'boxCount', title: '박스', align: 'right' },
  { key: 'looseQty', title: '낱개', align: 'right' },
  { key: 'quantity', title: '수량', align: 'right', summary: (rows) => rows.reduce((s, i) => s + i.quantity, 0) },
]

// 출고 요청 상세: 단계별 버튼은 담당자에게만 (출고 처리 = 출발 창고, 도착 확인 = 도착 매장, 사장은 전부)
export default function TransferDetailPage() {
  const navigate = useNavigate()
  const id = Number(useParams().stockTransferId)
  const url = `/transfers/${id}`
  const me = useMe().data
  const [deciding, setDeciding] = useState<'approve' | 'ship' | 'reject' | null>(null)

  const { data } = useGet<Detail>(url)
  const t = data?.transfer
  const isOwner = me?.role === 'OWNER'
  const mine = me?.location?.locationId
  // 승인·반려·출고 = 출발 창고 담당, 도착 확인·취소 = 요청한 매장 담당, 사장은 전부
  const fromSide = !!t && (isOwner || mine === t.fromLocationId)
  const toSide = !!t && (isOwner || mine === t.toLocationId)
  const canDecide = t?.status === 'REQUESTED' && fromSide
  const canShip = t?.status === 'APPROVED' && fromSide
  const canReceive = t?.status === 'SHIPPED' && toSide
  const canCancel = (t?.status === 'REQUESTED' || t?.status === 'APPROVED') && toSide

  const done = () => {
    queryClient.invalidateQueries({ queryKey: ['/transfers'] })
    queryClient.invalidateQueries({ queryKey: ['/products'] })
  }
  const receive = useMutation({ mutationFn: () => api.post(`${url}/receive`), onSuccess: done })
  const cancel = useMutation({ mutationFn: () => api.post(`${url}/cancel`), onSuccess: done })
  const error = receive.error ?? cancel.error

  return (
    <>
      <PageHeader
        title={`매장 출고 ${orderNo(id)}`}
        actions={
          <>
            <Button variant="secondary" onClick={() => navigate('/admin/transfers')}>목록</Button>
            {canCancel && <Button variant="secondary" disabled={cancel.isPending}
                                  onClick={() => window.confirm('출고 요청을 취소할까요?') && cancel.mutate()}>요청 취소</Button>}
            {canDecide && <Button variant="secondary" onClick={() => setDeciding('reject')}>반려</Button>}
            {canDecide && <Button onClick={() => setDeciding('approve')}>승인</Button>}
            {canShip && <Button onClick={() => setDeciding('ship')}>출고 처리</Button>}
            {canReceive && <Button disabled={receive.isPending}
                                   onClick={() => window.confirm('도착을 확인할까요? 보낸 수량만큼 매장 재고가 늘어나요.') && receive.mutate()}>도착 확인</Button>}
          </>
        }
      />
      {error && <p className="form-error">{getErrorMessage(error)}</p>}

      <Card className="page-section">
        <InfoGrid
          items={[
            { label: '출발', value: t?.fromLocationName },
            { label: '도착', value: t?.toLocationName },
            { label: '상태', value: t && transferBadge(t) },
            { label: '총 수량', value: t && `${t.totalQty}개` },
            { label: '요청일', value: t && dateTime(t.requestedAt) },
            { label: '승인·반려일', value: t && dateTime(t.decidedAt) },
            ...(t?.rejectReason ? [{ label: '반려 사유', value: t.rejectReason }] : []),
            { label: '출고일', value: t && dateTime(t.shippedAt) },
            { label: '도착일', value: t && dateTime(t.receivedAt) },
            { label: '메모', value: t?.memo },
          ]}
        />
      </Card>
      {t?.status === 'RECEIVED' && (
        <p className="help-text page-section">실제로 받은 수량이 다르면 상품 화면의 재고 조정에서 사유 '이동 수량 차이'로 맞춰 주세요.</p>
      )}

      <h2 className="section-title">이동 상품</h2>
      <Table columns={itemColumns} rows={data?.items} rowKey={(i) => i.stockTransferItemId} loading={!data} />

      {(deciding === 'approve' || deciding === 'ship') && data &&
        <QtyModal url={url} action={deciding} items={data.items} onClose={() => setDeciding(null)} />}
      {deciding === 'reject' && <RejectModal url={url} onClose={() => setDeciding(null)} />}
    </>
  )
}

const ACTION = {
  approve: { title: '승인', label: '승인', help: '창고에서 보낼 수 있는 수량으로 확정하세요. 재고는 출고할 때 빠져요.' },
  ship: { title: '출고 처리', label: '출고', help: '실제로 보내는 수량을 확인하세요. 0이면 이번에 보내지 않아요. 출고하면 창고 재고가 바로 빠져요.' },
}

// 승인(수량 확정) / 출고 처리(실제 보내는 수량, 창고 재고 차감) 공통: 박스·낱개 수 수정 가능
function QtyModal({ url, action, items, onClose }: { url: string; action: 'approve' | 'ship'; items: Item[]; onClose: () => void }) {
  const [qty, setQty] = useState(Object.fromEntries(items.map((i) => [i.stockTransferItemId, { box: String(i.boxCount), loose: String(i.looseQty) }])))
  const setItem = (itemId: number, patch: Partial<{ box: string; loose: string }>) => setQty({ ...qty, [itemId]: { ...qty[itemId], ...patch } })
  const qtyOf = (i: Item) => Number(qty[i.stockTransferItemId].box || 0) * (i.boxQty ?? 0) + Number(qty[i.stockTransferItemId].loose || 0)

  const save = useMutation({
    mutationFn: () => api.post(`${url}/${action}`, {
      items: items.map((i) => ({
        stockTransferItemId: i.stockTransferItemId,
        boxCount: Number(qty[i.stockTransferItemId].box || 0),
        looseQty: Number(qty[i.stockTransferItemId].loose || 0),
      })),
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/transfers'] })
      queryClient.invalidateQueries({ queryKey: ['/products'] })
      onClose()
    },
  })

  return (
    <FormModal title={ACTION[action].title} width={620} onClose={onClose} onSubmit={() => save.mutate()}
               saving={save.isPending} error={save.error} submitLabel={ACTION[action].label}>
      <p className="help-text">{ACTION[action].help}</p>
      {items.map((i) => (
        <div key={i.stockTransferItemId} className="rule-row" style={{ gridTemplateColumns: '1fr 80px 80px 70px' }}>
          <span>{i.productName} <span className="help-text">{i.boxQty ? `(1박스 ${i.boxQty}개)` : '(낱개만)'}</span></span>
          <input className="control-plain" type="number" min={0} placeholder="박스" aria-label="박스 수" disabled={!i.boxQty}
                 value={qty[i.stockTransferItemId].box} onChange={(e) => setItem(i.stockTransferItemId, { box: e.target.value })} />
          <input className="control-plain" type="number" min={0} placeholder="낱개" aria-label="낱개 수"
                 value={qty[i.stockTransferItemId].loose} onChange={(e) => setItem(i.stockTransferItemId, { loose: e.target.value })} />
          <span style={{ textAlign: 'right' }}>= {qtyOf(i)}개</span>
        </div>
      ))}
    </FormModal>
  )
}

// 반려: 사유는 요청한 매장에 그대로 보임 (예: 창고 재고 없음, 다음 주 입고 후 재요청)
function RejectModal({ url, onClose }: { url: string; onClose: () => void }) {
  const [reason, setReason] = useState('')
  const save = useMutation({
    mutationFn: () => api.post(`${url}/reject`, { reason }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['/transfers'] }); onClose() },
  })
  return (
    <FormModal title="반려" onClose={onClose} onSubmit={() => save.mutate()} saving={save.isPending} error={save.error} submitLabel="반려">
      <Input label="반려 사유" required maxLength={500} placeholder="예: 창고 재고 없음, 다음 주 입고 후 재요청" value={reason}
             onChange={(e) => setReason(e.target.value)} />
    </FormModal>
  )
}
