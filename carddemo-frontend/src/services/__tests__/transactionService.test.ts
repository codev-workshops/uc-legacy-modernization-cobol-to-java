import { describe, it, expect, beforeEach } from 'vitest';
import { transactionService } from '../transactionService';

describe('transactionService', () => {
  beforeEach(() => {
    localStorage.setItem('token', 'mock-jwt-token');
  });

  it('getTransactions returns paginated response', async () => {
    const result = await transactionService.getTransactions(0, 20);
    expect(result.content.length).toBeGreaterThan(0);
    expect(result.totalPages).toBe(2);
  });

  it('getTransaction returns single transaction', async () => {
    const txn = await transactionService.getTransaction(1);
    expect(txn.id).toBe(1);
    expect(txn.cardNumber).toBe('4111111111111111');
  });

  it('createTransaction creates new transaction', async () => {
    const txn = await transactionService.createTransaction({
      cardNumber: '4111111111111111',
      transactionTypeCode: 'PUR',
      amount: 99.99,
      merchantName: 'Test Store',
      description: 'Test purchase',
    });
    expect(txn.id).toBe(100);
    expect(txn.status).toBe('COMPLETED');
  });

  it('getTransactionTypes returns type list', async () => {
    const types = await transactionService.getTransactionTypes();
    expect(types).toHaveLength(4);
    expect(types[0].typeCode).toBe('PUR');
  });
});
