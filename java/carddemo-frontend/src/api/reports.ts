import apiClient from './client';

export interface GeneratedReport {
  id: number;
  accountId: number;
  customerName: string;
  reportType: string;
  textContent: string;
  htmlContent: string;
  generatedAt: string;
}

export async function generateReports(): Promise<{ status: string }> {
  const res = await apiClient.post('/reports/generate');
  return res.data;
}

export async function listReports(): Promise<GeneratedReport[]> {
  const res = await apiClient.get('/reports');
  return res.data;
}

export async function getReport(id: number): Promise<GeneratedReport> {
  const res = await apiClient.get(`/reports/${id}`);
  return res.data;
}
