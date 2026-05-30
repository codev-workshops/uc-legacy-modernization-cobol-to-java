import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export function Navbar() {
  const { user, logout, isAuthenticated } = useAuth();

  if (!isAuthenticated) return null;

  return (
    <nav className="bg-blue-800 text-white shadow-lg">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3">
        <Link to="/dashboard" className="text-xl font-bold">
          CardDemo
        </Link>
        <div className="flex items-center gap-4">
          <span className="text-sm">
            {user?.firstName} {user?.lastName}
          </span>
          <button
            onClick={logout}
            className="rounded bg-blue-700 px-3 py-1 text-sm hover:bg-blue-600"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
}
