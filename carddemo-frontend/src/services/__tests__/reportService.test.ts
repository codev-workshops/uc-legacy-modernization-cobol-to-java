import { describe, it, expect, beforeEach } from 'vitest';
import { reportService } from '../reportService';

describe('reportService', () => {
  beforeEach(() => {
    localStorage.setItem('token', 'mock-jwt-token');
  });

  it('generateReport returns report data', async () => {
    const report = await reportService.generateReport({
      startDate: '2024-01-01',
      endDate: '2024-09-30',
    });
    expect(report.totalAmount).toBe(2500.00);
    expect(report.transactionCount).toBe(10);
    expect(report.transactions).toHaveLength(10);
  });
});
