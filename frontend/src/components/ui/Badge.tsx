import type { ReactNode } from 'react'
import styles from './ui.module.css'

type Props = {
  tone?: 'neutral' | 'primary' | 'success' | 'warning' | 'danger'
  children: ReactNode
}

const tones = {
  neutral: styles.badgeNeutral,
  primary: styles.badgePrimary,
  success: styles.badgeSuccess,
  warning: styles.badgeWarning,
  danger: styles.badgeDanger,
}

export default function Badge({ tone = 'neutral', children }: Props) {
  return <span className={`${styles.badge} ${tones[tone]}`}>{children}</span>
}
