import { useState } from 'react'
import { Navigate, NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import {
  ArrowLeftRight, Bell, Box, Boxes, ChevronRight, Factory, Gift, LayoutDashboard, LogOut, Package,
  Receipt, Search, Settings, ShoppingCart, Truck, Users, UsersRound, type LucideIcon,
} from 'lucide-react'
import { api, queryClient } from '../../lib/api'
import { useMe, type Role } from '../../lib/auth'
import styles from './AppLayout.module.css'

// roles: 이 역할들에게만 보임 (없으면 모든 직원)
type MenuItem = { to: string; label: string; icon?: LucideIcon; roles?: Role[] }
// dropdown: 제목을 눌러 펼치고 접는 메뉴 (지금 보고 있는 화면이 안에 있으면 펼친 상태로 시작)
type MenuGroup = { title?: string; icon?: LucideIcon; roles?: Role[]; dropdown?: boolean; items: MenuItem[] }

const menuGroups: MenuGroup[] = [
  {
    items: [
      { to: '/admin/dashboard', label: '대시보드', icon: LayoutDashboard },
      { to: '/admin/products', label: '상품관리', icon: Package }, // 등록·수정은 사장만
      { to: '/admin/gifts', label: '증정품관리', icon: Gift }, // 증정품 목록 + 증정 규칙. 수정은 사장만
      { to: '/admin/production-orders', label: '발주·입고', icon: Factory, roles: ['OWNER', 'WAREHOUSE'] },
      { to: '/admin/transfers', label: '재고 이동', icon: ArrowLeftRight, roles: ['OWNER', 'WAREHOUSE', 'STORE'] },
      { to: '/admin/store-sales', label: '매장 판매', icon: Receipt, roles: ['OWNER', 'STORE'] },
      { to: '/admin/orders', label: '주문관리', icon: ShoppingCart, roles: ['OWNER', 'FULFILLMENT'] },
      { to: '/admin/packaging', label: '포장재', icon: Box, roles: ['OWNER', 'FULFILLMENT'] },
      { to: '/admin/group-buys', label: '공동구매', icon: Boxes, roles: ['OWNER', 'FULFILLMENT'] },
      { to: '/admin/customers', label: '회원 관리', icon: UsersRound },
    ],
  },
  {
    title: '상품·배송 관리',
    icon: Truck,
    dropdown: true,
    roles: ['OWNER', 'WAREHOUSE', 'FULFILLMENT'],
    items: [
      { to: '/admin/suppliers', label: '공급처(공장)', roles: ['OWNER', 'WAREHOUSE'] },
      { to: '/admin/sales-sites', label: '판매사이트', roles: ['OWNER'] },
      { to: '/admin/shipping-methods', label: '배송방법', roles: ['OWNER', 'FULFILLMENT'] },
    ],
  },
  {
    title: '사이트 관리',
    icon: Settings,
    roles: ['OWNER'],
    dropdown: true,
    items: [
      { to: '/admin/admins', label: '직원 계정', icon: Users },
      { to: '/admin/locations', label: '창고·매장' },
      { to: '/admin/activity-logs', label: '활동 로그' },
      { to: '/admin/settings', label: '메뉴·대시보드 설정' },
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
  if (isError) return <Navigate to="/admin/login" replace />

  const canSee = (m: { roles?: Role[] }) => !m.roles || m.roles.includes(me.role)
  const visibleGroups = menuGroups
    .filter(canSee)
    .map((g) => ({ ...g, items: g.items.filter(canSee) }))
    .filter((g) => g.items.length > 0)

  async function logout() {
    await api.post('/auth/logout')
    queryClient.clear()
    navigate('/admin/login')
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
