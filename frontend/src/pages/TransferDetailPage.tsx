import { useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { useMutation } from '@tanstack/react-query'
import { Button, Card, FormModal, InfoGrid, PageHeader, Table, type Column } from '../components/ui'
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
  const [shipping, setShipping] = useState(false)

  const { data } = useGet<Detail>(url)
  const t = data?.transfer
  const isOwner = me?.role === 'OWNER'
  const mine = me?.location?.locationId
  const canShip = t?.status === 'REQUESTED' && (isOwner || mine === t.fromLocationId)
  const canReceive = t?.status === 'SHIPPED' && (isOwner || mine === t.toLocationId)
  const canCancel = t?.status === 'REQUESTED'

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
            {canShip && <Button onClick={() => setShipping(true)}>출고 처리</Button>}
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

      {shipping && data && <ShipModal url={url} items={data.items} onClose={() => setShipping(false)} />}
    </>
  )
}

// 출고 처리: 실제로 보내는 박스·낱개 수 확인(수정 가능) → 창고 재고 차감
function ShipModal({ url, items, onClose }: { url: string; items: Item[]; onClose: () => void }) {
  const [qty, setQty] = useState(Object.fromEntries(items.map((i) => [i.stockTransferItemId, { box: String(i.boxCount), loose: String(i.looseQty) }])))
  const setItem = (itemId: number, patch: Partial<{ box: string; loose: string }>) => setQty({ ...qty, [itemId]: { ...qty[itemId], ...patch } })
  const qtyOf = (i: Item) => Number(qty[i.stockTransferItemId].box || 0) * (i.boxQty ?? 0) + Number(qty[i.stockTransferItemId].loose || 0)

  const save = useMutation({
    mutationFn: () => api.post(`${url}/ship`, {
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
    <FormModal title="출고 처리" width={620} onClose={onClose} onSubmit={() => save.mutate()}
               saving={save.isPending} error={save.error} submitLabel="출고">
      <p className="help-text">실제로 보내는 수량을 확인하세요. 0이면 이번에 보내지 않아요. 출고하면 창고 재고가 바로 빠져요.</p>
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
