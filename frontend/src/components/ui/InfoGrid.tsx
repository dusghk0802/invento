import type { ReactNode } from 'react'
import styles from './ui.module.css'

// 상세 화면 상단의 "항목명: 값" 묶음
type Item = { label: string; value: ReactNode }

export default function InfoGrid({ items }: { items: Item[] }) {
  return (
    <dl className={styles.infoGrid}>
      {items.map((i) => (
        <div key={i.label}>
          <dt className={styles.infoLabel}>{i.label}</dt>
          <dd className={styles.infoValue}>{i.value ?? '-'}</dd>
        </div>
      ))}
    </dl>
  )
}
