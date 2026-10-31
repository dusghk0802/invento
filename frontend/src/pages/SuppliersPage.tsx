import CrudPage from '../components/CrudPage'
import { useMe } from '../lib/auth'

type Supplier = { supplierId: number; supplierName: string; contact: string | null; active: boolean }

// 등록·수정: 사장 + 창고 직원 (발주를 창고 직원도 하므로)
export default function SuppliersPage() {
  const role = useMe().data?.role
  const canWrite = role === 'OWNER' || role === 'WAREHOUSE'

  return (
    <CrudPage<Supplier>
      title="공급처(공장)"
      endpoint="/suppliers"
      idKey="supplierId"
      canWrite={canWrite}
      emptyText="등록된 공급처가 없습니다."
      columns={[
        { key: 'supplierName', title: '공급처명' },
        { key: 'contact', title: '연락처' },
      ]}
      fields={[
        { name: 'supplierName', label: '공급처명', required: true, maxLength: 100 },
        { name: 'contact', label: '연락처', maxLength: 100, placeholder: '전화번호, 담당자 등' },
        { name: 'active', label: '사용', type: 'active' },
      ]}
    />
  )
}
