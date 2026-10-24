import type { ReactNode } from 'react'
import styles from './ui.module.css'

// 탭 안 화면 상단: 왼쪽 설명(나중에 검색·필터), 오른쪽 버튼
type Props = { children?: ReactNode; actions?: ReactNode }

export default function Toolbar({ children, actions }: Props) {
  return (
    <div className={styles.toolbar}>
      <div className={styles.toolbarLeft}>{children}</div>
      {actions && <div className={styles.toolbarActions}>{actions}</div>}
    </div>
  )
}
