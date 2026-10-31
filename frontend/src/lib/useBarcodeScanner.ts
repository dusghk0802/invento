import { useEffect, useRef } from 'react'

// USB·블루투스 바코드 스캐너는 키보드처럼 숫자를 아주 빠르게 입력하고 Enter를 누름
// → 사람 타이핑과 속도로 구분: 글자 간격 30ms 미만으로 4자 이상 들어온 뒤 Enter면 스캔으로 보고 onScan 호출
// 어느 칸에 커서가 있든 화면 전체에서 받음 (Enter는 막아서 폼 저장·검색이 같이 실행되지 않게)
export function useBarcodeScanner(onScan: (code: string) => void) {
  const handler = useRef(onScan)
  useEffect(() => { handler.current = onScan })

  useEffect(() => {
    let buffer = ''
    let last = 0
    const onKey = (e: KeyboardEvent) => {
      if (e.timeStamp - last > 30) buffer = ''
      last = e.timeStamp
      if (e.key === 'Enter') {
        if (buffer.length >= 4) {
          e.preventDefault()
          e.stopPropagation()
          handler.current(buffer)
        }
        buffer = ''
      } else if (e.key.length === 1) {
        buffer += e.key
      }
    }
    window.addEventListener('keydown', onKey, true)
    return () => window.removeEventListener('keydown', onKey, true)
  }, [])
}
