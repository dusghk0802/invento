import { Navigate, Route, Routes } from 'react-router'
import AppLayout from './components/layout/AppLayout'
import { Card, PageHeader } from './components/ui'
import LoginPage from './pages/LoginPage'
import GiftsPage from './pages/gifts/GiftsPage'
import GroupBuyDetailPage from './pages/group-buys/GroupBuyDetailPage'
import GroupBuysPage from './pages/group-buys/GroupBuysPage'
import OrderDetailPage from './pages/orders/OrderDetailPage'
import OrdersPage from './pages/orders/OrdersPage'
import ProductsPage from './pages/products/ProductsPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      {/* 로그인해야 들어갈 수 있는 화면 (AppLayout이 로그인 여부 확인) */}
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/products" replace />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/:orderId" element={<OrderDetailPage />} />
        <Route path="/gifts" element={<GiftsPage />} />
        <Route path="/group-buys" element={<GroupBuysPage />} />
        <Route path="/group-buys/:groupBuyId" element={<GroupBuyDetailPage />} />

        {/* 아직 만들지 않은 화면 */}
        <Route path="*" element={<><PageHeader title="준비 중" /><Card>아직 만들지 않은 화면입니다.</Card></>} />
      </Route>
    </Routes>
  )
}
