import { useId, type SelectHTMLAttributes } from 'react'
import styles from './Field.module.css'

type Option = { value: string | number; label: string }

type Props = SelectHTMLAttributes<HTMLSelectElement> & {
  label?: string
  error?: string
  options: Option[]
  placeholder?: string
}

export default function Select({ label, error, options, placeholder, id, className = '', ...props }: Props) {
  const autoId = useId()
  const selectId = id ?? autoId

  return (
    <div className={styles.field}>
      {label && (
        <label htmlFor={selectId} className={styles.label}>
          {label}
          {props.required && <span className={styles.required}>*</span>}
        </label>
      )}
      <select id={selectId} className={`${styles.control} ${error ? styles.invalid : ''} ${className}`} {...props}>
        {placeholder && <option value="">{placeholder}</option>}
        {options.map((o) => (
          <option key={o.value} value={o.value}>{o.label}</option>
        ))}
      </select>
      {error && <p className={styles.error}>{error}</p>}
    </div>
  )
}
