import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

interface StubPageProps {
  title: string;
}

export function StubPage({ title }: StubPageProps) {
  const { isAdmin } = useAuth();

  return (
    <div className="stub-page">
      <h1>{title}</h1>
      <p>This page is under construction.</p>
      <Link to={isAdmin ? '/admin' : '/menu'}>Back to Menu</Link>
    </div>
  );
}
