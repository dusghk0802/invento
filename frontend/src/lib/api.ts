import axios, { isAxiosError } from 'axios'
import { QueryClient, useQuery } from '@tanstack/react-query'

export const api = axios.create({ baseURL: '/api/admin' })

export const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false, refetchOnWindowFocus: false } },
})

// 세션이 끊기면(401) 로그인 화면으로 이동
api.interceptors.response.use(undefined, (error) => {
  if (isAxiosError(error) && error.response?.status === 401 && location.pathname !== '/admin/login') {
    location.href = '/admin/login'
  }
  return Promise.reject(error)
})

// 서버 오류 응답(ProblemDetail)의 detail 메시지 꺼내기
export function getErrorMessage(error: unknown, fallback = '요청을 처리하지 못했습니다.') {
  return (isAxiosError<{ detail?: string }>(error) && error.response?.data?.detail) || fallback
}

// GET 조회 공통. 캐시 키 = 주소 조각(+조건) → ['/products']를 새로고침하면 '/products/1'·검색 결과도 같이 새로고침
export const useGet = <T>(url: string, params?: object) =>
  useQuery({
    queryKey: [...url.split('/').filter(Boolean).map((s, i) => (i === 0 ? `/${s}` : s)), ...(params ? [params] : [])],
    queryFn: () => api.get<T>(url, { params }).then((r) => r.data),
  })

// 파일 다운로드 (엑셀 등): 서버가 만든 파일을 받아 브라우저에 저장
export async function download(url: string, filename: string) {
  const res = await api.get<Blob>(url, { responseType: 'blob' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(res.data)
  a.download = filename
  a.click()
  URL.revokeObjectURL(a.href)
}
