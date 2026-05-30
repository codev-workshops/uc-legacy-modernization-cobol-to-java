import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { MainMenu } from './pages/MainMenu';
import { AdminMenu } from './pages/AdminMenu';
import { StubPage } from './pages/StubPage';
import { AccountView } from './pages/AccountView';
import { AccountEdit } from './pages/AccountEdit';
import { CardList } from './pages/CardList';
import { CardDetail } from './pages/CardDetail';
import { CardEdit } from './pages/CardEdit';
import { TransactionList } from './pages/TransactionList';
import { TransactionDetail } from './pages/TransactionDetail';
import { TransactionAdd } from './pages/TransactionAdd';
import { BillPayment } from './pages/BillPayment';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/menu" element={<ProtectedRoute><MainMenu /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute requireAdmin><AdminMenu /></ProtectedRoute>} />

          {/* Account & card pages */}
          <Route path="/account-view/:id" element={<ProtectedRoute><AccountView /></ProtectedRoute>} />
          <Route path="/account-view" element={<ProtectedRoute><AccountView /></ProtectedRoute>} />
          <Route path="/account-update/:id" element={<ProtectedRoute><AccountEdit /></ProtectedRoute>} />
          <Route path="/account-update" element={<ProtectedRoute><AccountEdit /></ProtectedRoute>} />
          <Route path="/card-list" element={<ProtectedRoute><CardList /></ProtectedRoute>} />
          <Route path="/card-view/:num" element={<ProtectedRoute><CardDetail /></ProtectedRoute>} />
          <Route path="/card-view" element={<ProtectedRoute><CardDetail /></ProtectedRoute>} />
          <Route path="/card-update/:num" element={<ProtectedRoute><CardEdit /></ProtectedRoute>} />
          <Route path="/card-update" element={<ProtectedRoute><CardEdit /></ProtectedRoute>} />

          {/* Transaction pages */}
          <Route path="/transaction-list" element={<ProtectedRoute><TransactionList /></ProtectedRoute>} />
          <Route path="/transaction-view/:id" element={<ProtectedRoute><TransactionDetail /></ProtectedRoute>} />
          <Route path="/transaction-view" element={<ProtectedRoute><TransactionDetail /></ProtectedRoute>} />
          <Route path="/transaction-add" element={<ProtectedRoute><TransactionAdd /></ProtectedRoute>} />
          <Route path="/bill-payment" element={<ProtectedRoute><BillPayment /></ProtectedRoute>} />

          {/* Remaining stub pages */}
          <Route path="/transaction-reports" element={<ProtectedRoute><StubPage title="Transaction Reports" /></ProtectedRoute>} />
          <Route path="/pending-auth" element={<ProtectedRoute><StubPage title="Pending Authorization View" /></ProtectedRoute>} />

          {/* Admin menu stub pages */}
          <Route path="/admin/user-list" element={<ProtectedRoute requireAdmin><StubPage title="User List (Security)" /></ProtectedRoute>} />
          <Route path="/admin/user-add" element={<ProtectedRoute requireAdmin><StubPage title="User Add (Security)" /></ProtectedRoute>} />
          <Route path="/admin/user-update" element={<ProtectedRoute requireAdmin><StubPage title="User Update (Security)" /></ProtectedRoute>} />
          <Route path="/admin/user-delete" element={<ProtectedRoute requireAdmin><StubPage title="User Delete (Security)" /></ProtectedRoute>} />
          <Route path="/admin/tran-type-list" element={<ProtectedRoute requireAdmin><StubPage title="Transaction Type List/Update (Db2)" /></ProtectedRoute>} />
          <Route path="/admin/tran-type-maint" element={<ProtectedRoute requireAdmin><StubPage title="Transaction Type Maintenance (Db2)" /></ProtectedRoute>} />

          <Route path="/" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
