import axios from 'axios';
import type { Expense, ExpenseRequest, DashboardResponse, CsvUploadResult } from '../types';

const client = axios.create({
  baseURL: import.meta.env.API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const api = {
  // Expenses
  listExpenses: () =>
    client.get<Expense[]>('/expenses').then(r => r.data),

  getExpense: (id: number) =>
    client.get<Expense>(`/expenses/${id}`).then(r => r.data),

  createExpense: (req: ExpenseRequest) =>
    client.post<Expense>('/expenses', req).then(r => r.data),

  deleteExpense: (id: number) =>
    client.delete(`/expenses/${id}`),

  // CSV
  uploadCsv: (file: File) => {
    const fd = new FormData();
    fd.append('file', file);
    return client.post<CsvUploadResult>('/expenses/upload-csv', fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data);
  },

  // Dashboard
  getDashboard: () =>
    client.get<DashboardResponse>('/expenses/dashboard').then(r => r.data),

  // Categories (vendor → category rules map)
  getCategories: () =>
    client.get<Record<string, string>>('/expenses/categories').then(r => r.data),
};
