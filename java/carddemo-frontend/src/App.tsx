import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { MainMenu } from './pages/MainMenu';
import { AdminMenu } from './pages/AdminMenu';
import { StubPage } from './pages/StubPage';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/menu" element={<ProtectedRoute><MainMenu /></ProtectedRoute>} />
          <Route path="/admin" element={<ProtectedRoute requireAdmin><AdminMenu /></ProtectedRoute>} />

          {/* User menu stub pages */}
          <Route path="/account-view" element={<ProtectedRoute><StubPage title="Account View" /></ProtectedRoute>} />
          <Route path="/account-update" element={<ProtectedRoute><StubPage title="Account Update" /></ProtectedRoute>} />
          <Route path="/card-list" element={<ProtectedRoute><StubPage title="Credit Card List" /></ProtectedRoute>} />
          <Route path="/card-view" element={<ProtectedRoute><StubPage title="Credit Card View" /></ProtectedRoute>} />
          <Route path="/card-update" element={<ProtectedRoute><StubPage title="Credit Card Update" /></ProtectedRoute>} />
          <Route path="/transaction-list" element={<ProtectedRoute><StubPage title="Transaction List" /></ProtectedRoute>} />
          <Route path="/transaction-view" element={<ProtectedRoute><StubPage title="Transaction View" /></ProtectedRoute>} />
          <Route path="/transaction-add" element={<ProtectedRoute><StubPage title="Transaction Add" /></ProtectedRoute>} />
          <Route path="/transaction-reports" element={<ProtectedRoute><StubPage title="Transaction Reports" /></ProtectedRoute>} />
          <Route path="/bill-payment" element={<ProtectedRoute><StubPage title="Bill Payment" /></ProtectedRoute>} />
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
