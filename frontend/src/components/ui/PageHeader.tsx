import type { ReactNode } from 'react'
import styles from './ui.module.css'

type Props = { title: string; description?: string; actions?: ReactNode }

export default function PageHeader({ title, description, actions }: Props) {
  return (
    <div className={styles.pageHeader}>
      <div>
        <h1 className={styles.pageTitle}>{title}</h1>
        {description && <p className={styles.pageDescription}>{description}</p>}
      </div>
      {actions && <div className={styles.pageActions}>{actions}</div>}
    </div>
  )
}
