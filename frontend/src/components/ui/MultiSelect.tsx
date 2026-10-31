import { useEffect, useRef, useState } from 'react'
import { getErrorMessage } from '../../lib/api'
import { tagTone } from '../../lib/format'
import Badge from './Badge'
import styles from './ui.module.css'

type Option = { value: number; label: string }

type Props = {
  label?: string
  options: Option[]
  value: number[]
  onChange: (value: number[]) => void
  onCreate?: (name: string) => Promise<number> // 있으면 검색어로 새 항목 만들기 가능 (만든 항목 ID 반환)
  placeholder?: string
}

// 노션식 다중 선택: 칸 안에 바로 입력해서 검색, 아래 목록에서 고르면 칸 안에 태그로 표시 (× 로 해제)
export default function MultiSelect({ label, options, value, onChange, onCreate, placeholder = '선택' }: Props) {
  const [open, setOpen] = useState(false)
  const [keyword, setKeyword] = useState('')
  const [error, setError] = useState('')
  const ref = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  // 바깥을 누르면 닫기
  useEffect(() => {
    if (!open) return
    const close = (e: MouseEvent) => !ref.current?.contains(e.target as Node) && setOpen(false)
    document.addEventListener('mousedown', close)
    return () => document.removeEventListener('mousedown', close)
  }, [open])

  const selected = value.map((v) => options.find((o) => o.value === v)).filter((o) => !!o)
  const k = keyword.trim()
  const filtered = options.filter((o) => !value.includes(o.value) && o.label.includes(k))
  const canCreate = !!onCreate && k !== '' && !options.some((o) => o.label === k)

  const toggle = (v: number) => onChange(value.includes(v) ? value.filter((x) => x !== v) : [...value, v])

  async function create() {
    try {
      const id = await onCreate!(k)
      onChange([...value, id])
      setKeyword('')
    } catch (e) {
      setError(getErrorMessage(e))
    }
  }

  return (
    <div className={styles.field} ref={ref}>
      {label && <span className={styles.fieldLabel}>{label}</span>}
      <div className={`${styles.multiSelect} ${open ? styles.multiSelectOpen : ''}`}
           onClick={() => { setOpen(true); inputRef.current?.focus() }}>
        {selected.map((o) => (
          <Badge key={o.value} tone={tagTone(o.label)}>
            {o.label}
            <button type="button" className={styles.tagRemove} aria-label={`${o.label} 해제`}
                    onClick={(e) => { e.stopPropagation(); toggle(o.value) }}>×</button>
          </Badge>
        ))}
        <input ref={inputRef} className={styles.multiSelectInput} value={keyword}
               placeholder={selected.length === 0 ? placeholder : ''}
               onFocus={() => setOpen(true)}
               onChange={(e) => { setKeyword(e.target.value); setError(''); setOpen(true) }}
               onKeyDown={(e) => {
                 if (e.key === 'Escape' && open) { e.preventDefault(); setOpen(false) }
                 if (e.key === 'Backspace' && keyword === '' && value.length) onChange(value.slice(0, -1)) // 마지막 태그 지우기
                 if (e.key === 'Enter') {
                   e.preventDefault()
                   if (filtered[0]) { toggle(filtered[0].value); setKeyword('') } else if (canCreate) create()
                 }
               }} />
      </div>

      {open && (
        <div className={styles.multiSelectMenu}>
          <div className={styles.multiSelectList}>
            {filtered.map((o) => (
              <button key={o.value} type="button" className={styles.multiSelectOption} onClick={() => { toggle(o.value); setKeyword('') }}>
                <Badge tone={tagTone(o.label)}>{o.label}</Badge>
              </button>
            ))}
            {canCreate && (
              <button type="button" className={styles.multiSelectOption} onClick={create}>
                만들기 <Badge tone={tagTone(k)}>{k}</Badge>
              </button>
            )}
            {filtered.length === 0 && !canCreate && <p className={styles.multiSelectEmpty}>선택할 항목이 없습니다.</p>}
          </div>
          {error && <p className={styles.fieldError}>{error}</p>}
        </div>
      )}
    </div>
  )
}
