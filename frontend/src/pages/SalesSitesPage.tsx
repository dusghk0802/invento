import CrudPage from '../components/CrudPage'
import { useIsOwner } from '../lib/auth'

type SalesSite = { salesSiteId: number; salesSiteName: string; active: boolean }

export default function SalesSitesPage() {
  return (
    <CrudPage<SalesSite>
      title="판매사이트"
      endpoint="/sales-sites"
      idKey="salesSiteId"
      canWrite={useIsOwner()}
      emptyText="등록된 판매사이트가 없습니다."
      columns={[{ key: 'salesSiteName', title: '판매사이트명' }]}
      fields={[
        { name: 'salesSiteName', label: '판매사이트명', required: true, maxLength: 100, placeholder: '예: 자사몰, 스마트스토어' },
        { name: 'active', label: '사용', type: 'active' },
      ]}
    />
  )
}
