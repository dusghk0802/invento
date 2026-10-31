import { useId, type ReactNode } from 'react'
import { getErrorMessage } from '../../lib/api'
import Button from './Button'
import Modal from './Modal'

type Props = {
  title: string
  width?: number
  onClose: () => void
  onSubmit: (form: HTMLFormElement) => void
  saving?: boolean
  error?: unknown // 있으면 폼 아래에 서버 오류 메시지
  submitLabel?: string
  left?: ReactNode // 왼쪽 버튼 (삭제 등)
  children: ReactNode
}

// 등록·수정 창 공통: 모달 + 폼 + [취소][저장] + 오류 메시지
export default function FormModal({ title, width, onClose, onSubmit, saving, error, submitLabel = '저장', left, children }: Props) {
  const id = useId()
  return (
    <Modal open width={width} title={title} onClose={onClose}
           footer={
             <>
               {left}
               <Button variant="secondary" onClick={onClose}>취소</Button>
               <Button type="submit" form={id} disabled={saving}>{submitLabel}</Button>
             </>
           }>
      <form id={id} className="form-stack" onSubmit={(e) => { e.preventDefault(); onSubmit(e.currentTarget) }}>
        {children}
        {!!error && <p className="form-error">{getErrorMessage(error)}</p>}
      </form>
    </Modal>
  )
}
