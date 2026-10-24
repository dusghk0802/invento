import type { ReactNode } from 'react'
import styles from './Table.module.css'

export type Column<T> = {
  key: string
  title: string
  width?: number | string
  align?: 'left' | 'center' | 'right'
  render?: (row: T) => ReactNode // 없으면 row[key] 값을 그대로 표시
  summary?: (rows: T[]) => ReactNode // 맨 아래 합계 줄 (개수, 합계 등)
}

type Props<T> = {
  columns: Column<T>[]
  rows: T[] | undefined
  rowKey: (row: T) => string | number
  loading?: boolean
  emptyText?: string
  onRowClick?: (row: T) => void // 행 클릭 시 상세 화면 이동 등
}

export default function Table<T extends object>({ columns, rows, rowKey, loading, emptyText = '데이터가 없습니다.', onRowClick }: Props<T>) {
  const hasSummary = columns.some((c) => c.summary) && !!rows?.length

  return (
    <div className={styles.wrapper}>
      <table className={styles.table}>
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={c.key} style={{ width: c.width, textAlign: c.align }}>{c.title}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading || !rows?.length ? (
            <tr>
              <td colSpan={columns.length} className={styles.empty}>{loading ? '불러오는 중...' : emptyText}</td>
            </tr>
          ) : (
            rows.map((row) => (
              <tr
                key={rowKey(row)}
                className={onRowClick ? styles.clickable : undefined}
                onClick={onRowClick && (() => onRowClick(row))}
              >
                {columns.map((c) => (
                  <td key={c.key} style={{ textAlign: c.align }}>
                    {c.render ? c.render(row) : String((row as Record<string, unknown>)[c.key] ?? '')}
                  </td>
                ))}
              </tr>
            ))
          )}
        </tbody>
        {hasSummary && (
          <tfoot>
            <tr>
              {columns.map((c) => (
                <td key={c.key} style={{ textAlign: c.align }}>{c.summary?.(rows)}</td>
              ))}
            </tr>
          </tfoot>
        )}
      </table>
    </div>
  )
}
