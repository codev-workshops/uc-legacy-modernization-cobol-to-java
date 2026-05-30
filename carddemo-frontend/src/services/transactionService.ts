import api from './api';
import type {
  Transaction,
  TransactionType,
  NewTransaction,
  PaginatedResponse,
} from '../types';

export const transactionService = {
  getTransactions: async (
    page = 0,
    size = 20,
  ): Promise<PaginatedResponse<Transaction>> => {
    const { data } = await api.get<PaginatedResponse<Transaction>>(
      '/transactions',
      { params: { page, size } },
    );
    return data;
  },

  getTransaction: async (id: number): Promise<Transaction> => {
    const { data } = await api.get<Transaction>(`/transactions/${id}`);
    return data;
  },

  createTransaction: async (
    transaction: NewTransaction,
  ): Promise<Transaction> => {
    const { data } = await api.post<Transaction>(
      '/transactions',
      transaction,
    );
    return data;
  },

  getTransactionTypes: async (): Promise<TransactionType[]> => {
    const { data } = await api.get<TransactionType[]>('/transaction-types');
    return data;
  },
};
