import axios, { isAxiosError } from 'axios'
import { QueryClient } from '@tanstack/react-query'

export const api = axios.create({ baseURL: '/api' })

export const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: false, refetchOnWindowFocus: false } },
})

// 세션이 끊기면(401) 로그인 화면으로 이동
api.interceptors.response.use(undefined, (error) => {
  if (isAxiosError(error) && error.response?.status === 401 && location.pathname !== '/login') {
    location.href = '/login'
  }
  return Promise.reject(error)
})

// 서버 오류 응답(ProblemDetail)의 detail 메시지 꺼내기
export function getErrorMessage(error: unknown, fallback = '요청을 처리하지 못했습니다.') {
  return (isAxiosError<{ detail?: string }>(error) && error.response?.data?.detail) || fallback
}
