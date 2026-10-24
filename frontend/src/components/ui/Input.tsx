import { useId, type InputHTMLAttributes } from 'react'
import styles from './ui.module.css'

type Props = InputHTMLAttributes<HTMLInputElement> & { label?: string; error?: string }

export default function Input({ label, error, id, className = '', ...props }: Props) {
  const autoId = useId()
  const inputId = id ?? autoId

  return (
    <div className={styles.field}>
      {label && (
        <label htmlFor={inputId} className={styles.fieldLabel}>
          {label}
          {props.required && <span className={styles.required}>*</span>}
        </label>
      )}
      <input id={inputId} className={`${styles.control} ${error ? styles.invalid : ''} ${className}`} {...props} />
      {error && <p className={styles.fieldError}>{error}</p>}
    </div>
  )
}
