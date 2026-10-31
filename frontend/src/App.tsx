import { Navigate, Route, Routes } from 'react-router'
import AppLayout from './components/layout/AppLayout'
import { Card, PageHeader } from './components/ui'
import LoginPage from './pages/LoginPage'
import GiftsPage from './pages/GiftsPage'
import GroupBuyDetailPage from './pages/GroupBuyDetailPage'
import GroupBuysPage from './pages/GroupBuysPage'
import OrderDetailPage from './pages/OrderDetailPage'
import OrdersPage from './pages/OrdersPage'
import PackagingPage from './pages/PackagingPage'
import LocationsPage from './pages/LocationsPage'
import ProductsPage from './pages/ProductsPage'
import SalesSitesPage from './pages/SalesSitesPage'
import ShippingMethodsPage from './pages/ShippingMethodsPage'
import SuppliersPage from './pages/SuppliersPage'

// 주소 분리: /admin/** 관리자 백오피스, /shop/** 고객 쇼핑몰(10단계)
export default function App() {
  return (
    <Routes>
      <Route index element={<Navigate to="/admin/products" replace />} />
      <Route path="/admin/login" element={<LoginPage />} />

      {/* 로그인해야 들어갈 수 있는 화면 (AppLayout이 로그인 여부 확인) */}
      <Route path="/admin" element={<AppLayout />}>
        <Route index element={<Navigate to="/admin/products" replace />} />
        <Route path="products" element={<ProductsPage />} />
        <Route path="online-sales" element={<OrdersPage />} />
        <Route path="online-sales/:orderId" element={<OrderDetailPage />} />
        <Route path="gifts" element={<GiftsPage />} />
        <Route path="packaging" element={<PackagingPage />} />
        <Route path="bulk-sales" element={<GroupBuysPage />} />
        <Route path="bulk-sales/:groupBuyId" element={<GroupBuyDetailPage />} />

        {/* 기준정보 */}
        <Route path="locations" element={<LocationsPage />} />
        <Route path="suppliers" element={<SuppliersPage />} />
        <Route path="sales-sites" element={<SalesSitesPage />} />
        <Route path="shipping-methods" element={<ShippingMethodsPage />} />

        {/* 아직 만들지 않은 화면 */}
        <Route path="*" element={<><PageHeader title="준비 중" /><Card>아직 만들지 않은 화면입니다.</Card></>} />
      </Route>

      <Route path="*" element={<Navigate to="/admin/products" replace />} />
    </Routes>
  )
}
