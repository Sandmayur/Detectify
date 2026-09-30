import { api } from './api';
import { type PaginatedResponse, type ReportResponse } from './reportApi';

export const adminApi = {
    getReports: async (status = 'PENDING', page = 0, size = 20) => {
        const response = await api.get<PaginatedResponse<ReportResponse>>(`/api/admin/reports?status=${status}&page=${page}&size=${size}`);
        return response.data;
    },

    updateReportStatus: async (reportId: string, status: 'APPROVED' | 'REJECTED' | 'PENDING') => {
        const response = await api.patch<ReportResponse>(`/api/admin/reports/${reportId}/status`, { status });
        return response.data;
    }
};
