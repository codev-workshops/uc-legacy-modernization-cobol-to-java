import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const adminMenuItems = [
  { id: 1, label: 'User List (Security)', path: '/admin/user-list', program: 'COUSR00C' },
  { id: 2, label: 'User Add (Security)', path: '/admin/user-add', program: 'COUSR01C' },
  { id: 3, label: 'User Update (Security)', path: '/admin/user-update', program: 'COUSR02C' },
  { id: 4, label: 'User Delete (Security)', path: '/admin/user-delete', program: 'COUSR03C' },
  { id: 5, label: 'Transaction Type List/Update (Db2)', path: '/admin/tran-type-list', program: 'COTRTLIC' },
  { id: 6, label: 'Transaction Type Maintenance (Db2)', path: '/admin/tran-type-maint', program: 'COTRTUPC' },
];

export function AdminMenu() {
  const { logout, auth } = useAuth();

  return (
    <div className="admin-menu">
      <h1>CardDemo Admin Menu</h1>
      <p>Welcome, {auth.userId} (Administrator)</p>
      <nav>
        <ul>
          {adminMenuItems.map((item) => (
            <li key={item.id}>
              <Link to={item.path}>
                {item.id}. {item.label}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
      <Link to="/menu">Main Menu</Link>
      <button onClick={logout}>Sign Out</button>
    </div>
  );
}
