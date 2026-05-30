import { useState, FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function LoginPage() {
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await login({ userId, password });
      if (response.userType === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/menu');
      }
    } catch {
      setError('Invalid user ID or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <h1>CardDemo Sign On</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label htmlFor="userId">User ID</label>
          <input
            id="userId"
            type="text"
            maxLength={8}
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            required
          />
        </div>
        <div>
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        {error && <div role="alert" className="error">{error}</div>}
        <button type="submit" disabled={loading}>
          {loading ? 'Signing in...' : 'Sign In'}
        </button>
      </form>
    </div>
  );
}
