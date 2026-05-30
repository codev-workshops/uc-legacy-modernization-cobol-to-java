import { http, HttpResponse } from 'msw';

export const handlers = [
  http.post('/api/auth/login', async ({ request }) => {
    const body = await request.json() as { userId: string; password: string };

    if (body.userId === 'admin01' && body.password === 'password') {
      return HttpResponse.json({
        token: 'mock-admin-jwt-token',
        userId: 'admin01',
        userType: 'ADMIN',
        expiresIn: 3600000,
      });
    }

    if (body.userId === 'user0001' && body.password === 'password') {
      return HttpResponse.json({
        token: 'mock-user-jwt-token',
        userId: 'user0001',
        userType: 'USER',
        expiresIn: 3600000,
      });
    }

    return HttpResponse.json(
      { error: 'Invalid credentials' },
      { status: 401 }
    );
  }),
];
