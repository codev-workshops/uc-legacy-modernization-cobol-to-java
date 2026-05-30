import apiClient from './client';

export interface Account {
  accountId: number;
  activeStatus: string;
  currentBalance: number;
  creditLimit: number;
  cashCreditLimit: number;
  openDate: string;
  expirationDate: string;
  reissueDate: string | null;
  currentCycleCredit: number;
  currentCycleDebit: number;
  addressZip: string;
  groupId: string;
}

export interface AccountUpdate {
  activeStatus?: string;
  creditLimit?: number;
  cashCreditLimit?: number;
  addressZip?: string;
  groupId?: string;
}

export interface CardItem {
  cardNum: string;
  accountId: number;
  cvvCode: number;
  embossedName: string;
  expirationDate: string;
  activeStatus: string;
}

export interface CardUpdate {
  embossedName?: string;
  expirationDate?: string;
  activeStatus?: string;
}

export async function getAccount(id: number): Promise<Account> {
  const { data } = await apiClient.get<Account>(`/accounts/${id}`);
  return data;
}

export async function updateAccount(id: number, update: AccountUpdate): Promise<Account> {
  const { data } = await apiClient.put<Account>(`/accounts/${id}`, update);
  return data;
}

export async function getCardsByAccount(acctId: number): Promise<CardItem[]> {
  const { data } = await apiClient.get<CardItem[]>('/cards', { params: { acctId } });
  return data;
}

export async function getCard(cardNum: string): Promise<CardItem> {
  const { data } = await apiClient.get<CardItem>(`/cards/${cardNum}`);
  return data;
}

export async function updateCard(cardNum: string, update: CardUpdate): Promise<CardItem> {
  const { data } = await apiClient.put<CardItem>(`/cards/${cardNum}`, update);
  return data;
}
