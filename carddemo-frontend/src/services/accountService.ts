import api from './api';
import type {
  Account,
  Card,
  Customer,
  BillingStatement,
} from '../types';

export const accountService = {
  getAccount: async (id: number): Promise<Account> => {
    const { data } = await api.get<Account>(`/accounts/${id}`);
    return data;
  },

  updateAccount: async (
    id: number,
    account: Partial<Account>,
  ): Promise<Account> => {
    const { data } = await api.put<Account>(`/accounts/${id}`, account);
    return data;
  },

  getCards: async (): Promise<Card[]> => {
    const { data } = await api.get<Card[]>('/cards');
    return data;
  },

  getCustomer: async (id: number): Promise<Customer> => {
    const { data } = await api.get<Customer>(`/customers/${id}`);
    return data;
  },

  getBilling: async (accountId: number): Promise<BillingStatement> => {
    const { data } = await api.get<BillingStatement>(
      `/accounts/${accountId}/billing`,
    );
    return data;
  },
};
