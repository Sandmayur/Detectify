import { api } from './api';
import { type PaginatedResponse, type ReportResponse } from './reportApi';

export interface AdminUserDto {
    id: string;
    email: string;
    role: string;
    isSuspended: boolean;
}

export interface DisputeResponse {
    id: string;
    reportId?: string;
    companyId: string;
    companyDomain: string;
    contactEmail: string;
    reason: string;
    status: 'PENDING' | 'RESOLVED' | 'REJECTED';
    createdAt: string;
}

export const adminApi = {
    getReports: async (status = 'PENDING', page = 0, size = 20) => {
        const response = await api.get<PaginatedResponse<ReportResponse>>(`/api/admin/reports?status=${status}&page=${page}&size=${size}`);
        return response.data;
    },

    updateReportStatus: async (reportId: string, status: 'APPROVED' | 'REJECTED' | 'PENDING') => {
        const response = await api.patch<ReportResponse>(`/api/admin/reports/${reportId}/status`, { status });
        return response.data;
    },

    getDisputes: async (status = 'PENDING', page = 0, size = 20) => {
        const response = await api.get<PaginatedResponse<DisputeResponse>>(`/api/admin/disputes?status=${status}&page=${page}&size=${size}`);
        return response.data;
    },

    resolveDispute: async (disputeId: string, status: 'RESOLVED' | 'REJECTED') => {
        await api.patch(`/api/admin/disputes/${disputeId}/status?status=${status}`);
    },

    getUsers: async (page = 0, size = 20) => {
        const response = await api.get<PaginatedResponse<AdminUserDto>>(`/api/admin/users?page=${page}&size=${size}`);
        return response.data;
    },

    suspendUser: async (userId: string, suspend: boolean) => {
        await api.post(`/api/admin/users/${userId}/suspend?suspend=${suspend}`);
    }
};
