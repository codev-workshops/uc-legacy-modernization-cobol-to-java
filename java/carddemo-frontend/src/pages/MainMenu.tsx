import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const menuItems = [
  { id: 1, label: 'Account View', path: '/account-view', program: 'COACTVWC' },
  { id: 2, label: 'Account Update', path: '/account-update', program: 'COACTUPC' },
  { id: 3, label: 'Credit Card List', path: '/card-list', program: 'COCRDLIC' },
  { id: 4, label: 'Credit Card View', path: '/card-view', program: 'COCRDSLC' },
  { id: 5, label: 'Credit Card Update', path: '/card-update', program: 'COCRDUPC' },
  { id: 6, label: 'Transaction List', path: '/transaction-list', program: 'COTRN00C' },
  { id: 7, label: 'Transaction View', path: '/transaction-view', program: 'COTRN01C' },
  { id: 8, label: 'Transaction Add', path: '/transaction-add', program: 'COTRN02C' },
  { id: 9, label: 'Transaction Reports', path: '/transaction-reports', program: 'CORPT00C' },
  { id: 10, label: 'Bill Payment', path: '/bill-payment', program: 'COBIL00C' },
  { id: 11, label: 'Pending Authorization View', path: '/pending-auth', program: 'COPAUS0C' },
];

export function MainMenu() {
  const { logout, auth } = useAuth();

  return (
    <div className="main-menu">
      <h1>CardDemo Main Menu</h1>
      <p>Welcome, {auth.userId}</p>
      <nav>
        <ul>
          {menuItems.map((item) => (
            <li key={item.id}>
              <Link to={item.path}>
                {item.id}. {item.label}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
      <button onClick={logout}>Sign Out</button>
    </div>
  );
}
