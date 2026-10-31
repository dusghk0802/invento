import { useState } from 'react'
import { Navigate, NavLink, Outlet, useLocation, useNavigate } from 'react-router'
import {
  Bell, ChevronRight, LayoutDashboard, LogOut, Package, Search, Settings, ShoppingCart, Truck, type LucideIcon,
} from 'lucide-react'
import { api, queryClient } from '../../lib/api'
import { useMe, type Features, type Role } from '../../lib/auth'
import styles from './AppLayout.module.css'

// roles: 이 역할들에게만 보임 (없으면 모든 직원). 볼 메뉴가 하나도 없는 그룹은 자동으로 숨김
// feature: 이 기능이 켜져 있을 때만 보임 (예: 판매사이트는 개인용만)
type MenuItem = { to: string; label: string; icon?: LucideIcon; roles?: Role[]; feature?: keyof Features }
// dropdown: 제목을 눌러 펼치고 접는 메뉴 (지금 보고 있는 화면이 안에 있으면 펼친 상태로 시작)
type MenuGroup = { title?: string; icon?: LucideIcon; roles?: Role[]; dropdown?: boolean; items: MenuItem[] }

const menuGroups: MenuGroup[] = [
  { items: [{ to: '/admin/dashboard', label: '대시보드', icon: LayoutDashboard }] },
  {
    title: '재고관리',
    icon: Package,
    dropdown: true,
    items: [
      { to: '/admin/products', label: '상품' }, // 등록·수정은 사장만, 원가 컬럼은 사장만
      { to: '/admin/gifts', label: '증정품' }, // 증정품 목록 + 증정 규칙. 수정·순수익은 사장만
      { to: '/admin/packaging', label: '포장재', roles: ['OWNER', 'FULFILLMENT'] },
    ],
  },
  {
    title: '상품판매관리',
    icon: ShoppingCart,
    dropdown: true,
    items: [
      { to: '/admin/store-sales', label: '매장판매', roles: ['OWNER', 'STORE'] },
      // 창고 직원은 출고할 상품 확인용 조회만
      { to: '/admin/online-sales', label: '온라인판매', roles: ['OWNER', 'FULFILLMENT', 'WAREHOUSE'] },
      { to: '/admin/bulk-sales', label: '대량판매', roles: ['OWNER', 'FULFILLMENT'] },
    ],
  },
  {
    title: '상품·배송관리',
    icon: Truck,
    dropdown: true,
    items: [
      { to: '/admin/production-orders', label: '발주·입고', roles: ['OWNER', 'WAREHOUSE'] },
      { to: '/admin/transfers', label: '매장출고현황', roles: ['OWNER', 'WAREHOUSE', 'STORE'] },
      { to: '/admin/suppliers', label: '공급처', roles: ['OWNER', 'WAREHOUSE'] },
      { to: '/admin/sales-sites', label: '판매사이트', roles: ['OWNER'], feature: 'salesSites' },
      { to: '/admin/shipping-methods', label: '배송방법', roles: ['OWNER', 'FULFILLMENT'] },
    ],
  },
  {
    title: '사이트 관리',
    icon: Settings,
    dropdown: true,
    items: [
      { to: '/admin/customers', label: '회원관리', roles: ['OWNER', 'FULFILLMENT'] }, // 온라인 주문 담당만
      { to: '/admin/admins', label: '직원계정관리', roles: ['OWNER'] },
      { to: '/admin/locations', label: '창고·매장', roles: ['OWNER'] },
      { to: '/admin/activity-logs', label: '활동 로그', roles: ['OWNER'] },
      { to: '/admin/settings', label: '메뉴·대시보드 설정', roles: ['OWNER'] },
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

  // features가 없는 응답(재시작 전 서버 등)이 와도 화면이 깨지지 않게 ?. 사용
  const canSee = (m: { roles?: Role[]; feature?: keyof Features }) =>
    (!m.roles || m.roles.includes(me.role)) && (!m.feature || !!me.features?.[m.feature])
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
