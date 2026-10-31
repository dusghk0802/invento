import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router'
import { api, queryClient } from '../lib/api'
import { Button, Input } from '../components/ui'

export default function LoginPage() {
  const navigate = useNavigate()

  // 백엔드 formLogin은 폼 형식(x-www-form-urlencoded)으로 받음
  const login = useMutation({
    mutationFn: (form: HTMLFormElement) =>
      api.post('/auth/login', Object.fromEntries(new FormData(form)), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      }),
    onSuccess: () => {
      queryClient.removeQueries({ queryKey: ['me'] })
      navigate('/admin/products')
    },
  })

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={(e) => { e.preventDefault(); login.mutate(e.currentTarget) }}>
        <div className="login-heading">
          <h1 className="login-logo">Invento</h1>
          <p className="login-subtitle">백오피스 재고·판매 관리</p>
        </div>
        <Input name="loginId" label="아이디" autoComplete="username" required autoFocus />
        <Input name="password" label="비밀번호" type="password" autoComplete="current-password" required />
        {login.isError && <p className="login-error">아이디 또는 비밀번호가 올바르지 않습니다.</p>}
        <Button type="submit" block disabled={login.isPending}>
          {login.isPending ? '로그인 중...' : '로그인'}
        </Button>
      </form>
    </div>
  )
}
