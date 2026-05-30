import { createContext, useContext, useState, useCallback, useEffect, ReactNode } from 'react';
import { login as apiLogin, LoginRequest, LoginResponse } from '../api/auth';
import { setTokenGetter } from '../api/client';

interface AuthState {
  token: string | null;
  userId: string | null;
  userType: 'ADMIN' | 'USER' | null;
  expiresAt: number | null;
}

interface AuthContextType {
  auth: AuthState;
  login: (request: LoginRequest) => Promise<LoginResponse>;
  logout: () => void;
  isAuthenticated: boolean;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState<AuthState>({
    token: null,
    userId: null,
    userType: null,
    expiresAt: null,
  });

  useEffect(() => {
    setTokenGetter(() => auth.token);
  }, [auth.token]);

  const login = useCallback(async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await apiLogin(request);
    setAuth({
      token: response.token,
      userId: response.userId,
      userType: response.userType,
      expiresAt: Date.now() + response.expiresIn,
    });
    return response;
  }, []);

  const logout = useCallback(() => {
    setAuth({ token: null, userId: null, userType: null, expiresAt: null });
  }, []);

  const isAuthenticated = auth.token !== null && auth.expiresAt !== null && Date.now() < auth.expiresAt;
  const isAdmin = isAuthenticated && auth.userType === 'ADMIN';

  return (
    <AuthContext.Provider value={{ auth, login, logout, isAuthenticated, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
