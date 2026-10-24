import { useState } from 'react'
import { Navigate, NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import {
  Bell, Box, Boxes, ChevronRight, Gift, LayoutDashboard, LogOut, Package, Search,
  Settings, ShoppingCart, Truck, Users, UsersRound, type LucideIcon,
} from 'lucide-react'
import { api, queryClient } from '../../lib/api'
import { useMe, type Me } from '../../lib/auth'
import styles from './AppLayout.module.css'

type Role = Me['role']
type MenuItem = { to: string; label: string; icon?: LucideIcon; only?: Role }
// dropdown: 제목을 눌러 펼치고 접는 메뉴 (지금 보고 있는 화면이 안에 있으면 펼친 상태로 시작)
type MenuGroup = { title?: string; icon?: LucideIcon; only?: Role; dropdown?: boolean; items: MenuItem[] }

const menuGroups: MenuGroup[] = [
  {
    items: [
      { to: '/dashboard', label: '대시보드', icon: LayoutDashboard },
      { to: '/products', label: '상품관리', icon: Package }, // 시리즈·상품종류·재고·입고는 상품 표의 컬럼
      { to: '/gifts', label: '증정품관리', icon: Gift }, // 한 화면에서 증정품 목록 + 증정 규칙. ADMIN은 조회만
      { to: '/orders', label: '주문관리', icon: ShoppingCart },
    ],
  },
  { only: 'ADMIN', items: [{ to: '/packaging', label: '포장재', icon: Box }] },
  { items: [{ to: '/group-buys', label: '공동구매', icon: Boxes }] },
  { items: [{ to: '/customers', label: '회원 관리', icon: UsersRound }] },
  {
    title: '상품·배송 관리',
    icon: Truck,
    dropdown: true,
    items: [
      { to: '/purchase-sites', label: '구매사이트', only: 'SUPER_ADMIN' },
      { to: '/sales-sites', label: '판매사이트' },
      { to: '/shipping-methods', label: '배송방법' },
    ],
  },
  {
    title: '사이트 관리',
    icon: Settings,
    only: 'SUPER_ADMIN',
    dropdown: true,
    items: [
      { to: '/admins', label: '관리자 계정', icon: Users },
      { to: '/activity-logs', label: '활동 로그' },
      { to: '/settings', label: '메뉴·대시보드 설정' },
    ],
  },
]

function MenuLinks({ items, sub }: { items: MenuItem[]; sub?: boolean }) {
  return items.map(({ to, label, icon: Icon }) => (
    <NavLink
      key={to}
      to={to}
      className={({ isActive }) => `${styles.navItem} ${sub ? styles.subItem : ''} ${isActive ? styles.active : ''}`}
    >
      {!sub && Icon && <Icon size={16} />}
      {label}
    </NavLink>
  ))
}

function MenuDropdown({ title, icon: Icon, items }: { title: string; icon?: LucideIcon; items: MenuItem[] }) {
  const { pathname } = useLocation()
  const [open, setOpen] = useState(() => items.some((i) => pathname.startsWith(i.to)))

  return (
    <div>
      <button type="button" className={styles.navItem} aria-expanded={open} onClick={() => setOpen(!open)}>
        {Icon && <Icon size={16} />}
        {title}
        <ChevronRight size={14} className={`${styles.chevron} ${open ? styles.chevronOpen : ''}`} />
      </button>
      {open && <MenuLinks items={items} sub />}
    </div>
  )
}

export default function AppLayout() {
  const navigate = useNavigate()
  const { data: me, isPending, isError } = useMe()

  if (isPending) return null
  if (isError) return <Navigate to="/login" replace />

  const canSee = (m: { only?: Role }) => !m.only || m.only === me.role
  const visibleGroups = menuGroups
    .filter(canSee)
    .map((g) => ({ ...g, items: g.items.filter(canSee) }))

  async function logout() {
    await api.post('/auth/logout')
    queryClient.clear()
    navigate('/login')
  }

  return (
    <div className={styles.layout}>
      <aside className={styles.sidebar}>
        <div className={styles.logo}>
          <Package size={20} className={styles.logoIcon} />
          Invento
        </div>
        <nav className={styles.nav}>
          {visibleGroups.map((group) =>
            group.dropdown ? (
              <MenuDropdown key={group.title} title={group.title!} icon={group.icon} items={group.items} />
            ) : (
              <div key={group.title ?? group.items[0].to}>
                {group.title && <p className={styles.groupTitle}>{group.title}</p>}
                <MenuLinks items={group.items} />
              </div>
            ),
          )}
        </nav>
      </aside>

      <div className={styles.main}>
        <header className={styles.header}>
          {/* TODO: 통합 검색 기능 연결 */}
          <label className={styles.search}>
            <Search size={14} />
            <input placeholder="검색" />
          </label>

          <div className={styles.headerRight}>
            <button type="button" className={styles.iconButton} aria-label="알림">
              <Bell size={18} />
            </button>
            <span className={styles.user}>
              <span className={styles.avatar}>{me.loginId.charAt(0).toUpperCase()}</span>
              {me.loginId}
            </span>
            <button type="button" className={styles.iconButton} onClick={logout} aria-label="로그아웃" title="로그아웃">
              <LogOut size={18} />
            </button>
          </div>
        </header>
        <main className={styles.content}>
          <Outlet />
        </main>
      </div>
    </div>
  )
}
