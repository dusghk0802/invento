import { useState, type ReactNode } from 'react'
import { useMutation } from '@tanstack/react-query'
import { api, queryClient, useGet } from '../lib/api'
import { Badge, Button, FormModal, Input, PageHeader, Select, Table, type Column } from './ui'

// 기준정보처럼 "목록 + 등록/수정 모달" 구조인 화면을 설정만으로 만드는 공통 화면
export type Field = {
  name: string
  label: string
  type?: 'text' | 'number' | 'select' | 'active' // active = 사용/중지 선택
  options?: { value: string | number; label: string }[]
  numeric?: boolean // select 값을 숫자로 보낼 때 (ID 등)
  required?: boolean
  maxLength?: number
  placeholder?: string
}

type Props<T> = {
  title: string
  endpoint: string // 예: '/locations'
  idKey: keyof T & string
  columns: Column<T>[]
  fields: Field[]
  canWrite: boolean // 등록·수정 버튼 표시 여부
  emptyText?: string
  rowActions?: (row: T) => ReactNode // 수정 버튼 옆 추가 버튼
}

// 폼 입력값 → 서버로 보낼 JSON (빈 값은 null, 숫자·사용여부 변환)
function toBody(fields: Field[], form: HTMLFormElement) {
  const data = new FormData(form)
  return Object.fromEntries(fields.map((f) => {
    const v = String(data.get(f.name) ?? '').trim()
    if (f.type === 'active') return [f.name, v === 'true']
    if (v === '') return [f.name, null]
    return [f.name, f.type === 'number' || f.numeric ? Number(v) : v]
  }))
}

export default function CrudPage<T extends { active: boolean }>(props: Props<T>) {
  const { title, endpoint, idKey, columns, fields, canWrite, emptyText, rowActions } = props
  const [editing, setEditing] = useState<T | 'new' | null>(null)

  const { data, isPending } = useGet<T[]>(endpoint)

  const save = useMutation({
    mutationFn: (body: Record<string, unknown>) =>
      editing === 'new' ? api.post(endpoint, body) : api.put(`${endpoint}/${String((editing as T)[idKey])}`, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [endpoint] })
      close()
    },
  })

  function close() {
    save.reset()
    setEditing(null)
  }

  const allColumns: Column<T>[] = [
    ...columns,
    {
      key: 'active', title: '사용', width: 80, align: 'center',
      render: (r) => <Badge tone={r.active ? 'success' : 'neutral'}>{r.active ? '사용' : '중지'}</Badge>,
    },
    ...(canWrite || rowActions ? [{
      key: 'actions', title: '관리', width: 160, align: 'center' as const,
      render: (r: T) => (
        <div className="row-actions">
          {rowActions?.(r)}
          {canWrite && <Button variant="secondary" size="sm" onClick={() => setEditing(r)}>수정</Button>}
        </div>
      ),
    }] : []),
  ]

  const current = editing && editing !== 'new' ? (editing as Record<string, unknown>) : undefined

  return (
    <>
      <PageHeader title={title} actions={canWrite && <Button onClick={() => setEditing('new')}>+ 추가</Button>} />
      <Table columns={allColumns} rows={data} rowKey={(r) => String(r[idKey])} loading={isPending} emptyText={emptyText} />

      {editing && (
        <FormModal title={editing === 'new' ? `${title} 등록` : `${title} 수정`} onClose={close}
                   onSubmit={(form) => save.mutate(toBody(fields, form))} saving={save.isPending} error={save.error}>
          {fields.map((f) => {
            const value = current?.[f.name]
            if (f.type === 'active') {
              return (
                <Select key={f.name} name={f.name} label={f.label} defaultValue={String(value ?? true)}
                        options={[{ value: 'true', label: '사용' }, { value: 'false', label: '중지' }]} />
              )
            }
            if (f.type === 'select') {
              return (
                <Select key={f.name} name={f.name} label={f.label} required={f.required} defaultValue={String(value ?? '')}
                        options={f.options ?? []} placeholder={f.placeholder ?? '선택'} />
              )
            }
            return (
              <Input key={f.name} name={f.name} label={f.label} type={f.type === 'number' ? 'number' : 'text'}
                     required={f.required} maxLength={f.maxLength} placeholder={f.placeholder}
                     defaultValue={value == null ? '' : String(value)} />
            )
          })}
        </FormModal>
      )}
    </>
  )
}
