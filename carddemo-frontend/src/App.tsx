import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import { ErrorBoundary } from './components/ErrorBoundary';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Layout } from './components/Layout';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { AdminPage } from './pages/AdminPage';
import { AccountViewPage } from './pages/AccountViewPage';
import { AccountEditPage } from './pages/AccountEditPage';
import { CardListPage } from './pages/CardListPage';
import { CardDetailPage } from './pages/CardDetailPage';
import { CardEditPage } from './pages/CardEditPage';
import { TransactionListPage } from './pages/TransactionListPage';
import { TransactionDetailPage } from './pages/TransactionDetailPage';
import { TransactionNewPage } from './pages/TransactionNewPage';
import { UserManagementPage } from './pages/UserManagementPage';
import { BillingPage } from './pages/BillingPage';
import { ReportsPage } from './pages/ReportsPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export default function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route
                element={
                  <ProtectedRoute>
                    <Layout />
                  </ProtectedRoute>
                }
              >
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/admin" element={<AdminPage />} />
                <Route path="/accounts/:id" element={<AccountViewPage />} />
                <Route path="/accounts/:id/edit" element={<AccountEditPage />} />
                <Route path="/accounts/:id/billing" element={<BillingPage />} />
                <Route path="/cards" element={<CardListPage />} />
                <Route path="/cards/:num" element={<CardDetailPage />} />
                <Route path="/cards/:num/edit" element={<CardEditPage />} />
                <Route path="/transactions" element={<TransactionListPage />} />
                <Route path="/transactions/new" element={<TransactionNewPage />} />
                <Route path="/transactions/:id" element={<TransactionDetailPage />} />
                <Route path="/admin/users" element={<UserManagementPage />} />
                <Route path="/reports" element={<ReportsPage />} />
              </Route>
              <Route path="*" element={<Navigate to="/login" replace />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}
