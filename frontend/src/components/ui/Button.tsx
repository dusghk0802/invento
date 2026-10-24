import type { ButtonHTMLAttributes } from 'react'
import styles from './ui.module.css'

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost'
  size?: 'sm' | 'md'
  block?: boolean
}

const variants = { primary: styles.btnPrimary, secondary: styles.btnSecondary, danger: styles.btnDanger, ghost: styles.btnGhost }
const sizes = { md: styles.btnMd, sm: styles.btnSm }

export default function Button({ variant = 'primary', size = 'md', block, type = 'button', className = '', ...props }: Props) {
  return (
    <button
      type={type}
      className={`${styles.button} ${variants[variant]} ${sizes[size]} ${block ? styles.btnBlock : ''} ${className}`}
      {...props}
    />
  )
}
