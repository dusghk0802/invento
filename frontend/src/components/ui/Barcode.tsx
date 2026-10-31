import { useEffect, useRef } from 'react'
import JsBarcode from 'jsbarcode'

// 바코드 그리기 (JsBarcode, SVG). 숫자 13자리면 EAN-13(시중 상품 바코드), 그 외(상품번호 등)는 CODE128
export default function Barcode({ value, height = 50 }: { value: string; height?: number }) {
  const ref = useRef<SVGSVGElement>(null)
  useEffect(() => {
    if (ref.current) JsBarcode(ref.current, value, { format: /^\d{13}$/.test(value) ? 'EAN13' : 'CODE128', height, margin: 0, fontSize: 14 })
  }, [value, height])
  return <svg ref={ref} />
}
