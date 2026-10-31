import { useQuery } from '@tanstack/react-query'
import { api } from './api'

// OWNER=사장, STORE=매장 직원, WAREHOUSE=창고 직원, FULFILLMENT=출고 직원
export type Role = 'OWNER' | 'STORE' | 'WAREHOUSE' | 'FULFILLMENT'
// features: 개인용/포폴에 따라 켜고 끄는 기능 (서버 설정 app.features)
export type Features = { salesSites: boolean }
export type Me = { loginId: string; role: Role; features: Features }

export const useMe = () =>
  useQuery({ queryKey: ['me'], queryFn: () => api.get<Me>('/auth/me').then((res) => res.data) })

// 등록·수정·삭제 버튼을 사장에게만 보여줄 때 사용
export const useIsOwner = () => useMe().data?.role === 'OWNER'
