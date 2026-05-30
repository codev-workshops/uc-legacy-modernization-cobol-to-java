import { describe, it, expect, beforeEach } from 'vitest';
import { accountService } from '../accountService';

describe('accountService', () => {
  beforeEach(() => {
    localStorage.setItem('token', 'mock-jwt-token');
  });

  it('getAccount returns account', async () => {
    const account = await accountService.getAccount(1);
    expect(account.accountNumber).toBe('00000000001');
    expect(account.status).toBe('ACTIVE');
  });

  it('updateAccount updates and returns account', async () => {
    const account = await accountService.updateAccount(1, {
      status: 'CLOSED',
    });
    expect(account.status).toBe('CLOSED');
  });

  it('getCards returns card list', async () => {
    const cards = await accountService.getCards();
    expect(cards).toHaveLength(2);
    expect(cards[0].cardNumber).toBe('4111111111111111');
  });

  it('getCustomer returns customer', async () => {
    const customer = await accountService.getCustomer(1);
    expect(customer.firstName).toBe('John');
  });

  it('getBilling returns billing statement', async () => {
    const billing = await accountService.getBilling(1);
    expect(billing.totalBalance).toBe(5432.10);
    expect(billing.transactions).toHaveLength(5);
  });
});
