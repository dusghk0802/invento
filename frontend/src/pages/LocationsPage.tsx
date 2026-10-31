import CrudPage from '../components/CrudPage'
import { Badge } from '../components/ui'
import { useIsOwner } from '../lib/auth'

type Location = { locationId: number; locationName: string; locationType: 'WAREHOUSE' | 'STORE'; active: boolean }

const typeLabel = { WAREHOUSE: '창고', STORE: '매장' }

export default function LocationsPage() {
  return (
    <CrudPage<Location>
      title="창고·매장"
      endpoint="/locations"
      idKey="locationId"
      canWrite={useIsOwner()}
      emptyText="등록된 창고·매장이 없습니다."
      columns={[
        { key: 'locationName', title: '이름' },
        {
          key: 'locationType', title: '구분', width: 100,
          render: (l) => <Badge tone={l.locationType === 'STORE' ? 'primary' : 'neutral'}>{typeLabel[l.locationType]}</Badge>,
        },
      ]}
      fields={[
        { name: 'locationName', label: '이름', required: true, maxLength: 50, placeholder: '예: 본 창고, 1호 매장' },
        {
          name: 'locationType', label: '구분', type: 'select', required: true,
          options: [{ value: 'WAREHOUSE', label: '창고' }, { value: 'STORE', label: '매장' }],
        },
        { name: 'active', label: '사용', type: 'active' },
      ]}
    />
  )
}
