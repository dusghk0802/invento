import { useEffect, useRef, type ReactNode } from 'react'
import styles from './ui.module.css'

type Props = {
  open: boolean
  title: string
  onClose: () => void
  children: ReactNode
  footer?: ReactNode
  width?: number
}

// 브라우저 기본 <dialog> 사용: ESC 닫기, 배경 클릭 닫기, 포커스 가두기 자동 지원
export default function Modal({ open, title, onClose, children, footer, width = 480 }: Props) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialog = ref.current
    if (open && !dialog?.open) dialog?.showModal()
    if (!open && dialog?.open) dialog.close()
  }, [open])

  return (
    <dialog
      ref={ref}
      className={styles.dialog}
      style={{ width }}
      onClose={onClose}
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      {open && (
        <>
          <div className={styles.modalHeader}>
            <h2 className={styles.modalTitle}>{title}</h2>
            <button type="button" className={styles.modalClose} onClick={onClose} aria-label="닫기">×</button>
          </div>
          <div className={styles.modalBody}>{children}</div>
          {footer && <div className={styles.modalFooter}>{footer}</div>}
        </>
      )}
    </dialog>
  )
}
