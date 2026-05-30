import api from './api';
import type { ReportData, ReportRequest } from '../types';

export const reportService = {
  generateReport: async (request: ReportRequest): Promise<ReportData> => {
    const { data } = await api.get<ReportData>('/reports/transactions', {
      params: request,
    });
    return data;
  },
};
