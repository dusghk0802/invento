import { useQuery } from '@tanstack/react-query'
import { api } from './api'

export type Me = { loginId: string; role: 'SUPER_ADMIN' | 'ADMIN' }

export const useMe = () =>
  useQuery({ queryKey: ['me'], queryFn: () => api.get<Me>('/auth/me').then((res) => res.data) })

// 등록·수정·삭제 버튼을 SUPER_ADMIN에게만 보여줄 때 사용
export const useIsSuper = () => useMe().data?.role === 'SUPER_ADMIN'
