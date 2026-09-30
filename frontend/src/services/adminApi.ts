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

export interface AdminStatisticsDto {
    totalCompanies: number;
    totalReports: number;
    totalUsers: number;
    pendingReports: number;
    pendingDisputes: number;
    flaggedReports: number;
}

export interface ScamKeywordDto {
    id: string;
    keyword: string;
    category: string;
    weight: number;
    isActive: boolean;
}

export interface ScamKeywordRequest {
    keyword: string;
    category: string;
    weight: number;
    isActive: boolean;
}

export const adminApi = {
    getReports: async (status = 'PENDING', page = 0, size = 20) => {
        const response = await api.get<PaginatedResponse<ReportResponse>>(`/api/admin/reports?status=${status}&page=${page}&size=${size}`);
        return response.data;
    },

    updateReportStatus: async (reportId: string, status: 'APPROVED' | 'REJECTED' | 'PENDING') => {
        if (status === 'APPROVED') {
            const response = await api.patch<ReportResponse>(`/api/admin/reports/${reportId}/approve`);
            return response.data;
        } else if (status === 'REJECTED') {
            const response = await api.patch<ReportResponse>(`/api/admin/reports/${reportId}/reject`);
            return response.data;
        } else {
            const response = await api.patch<ReportResponse>(`/api/admin/reports/${reportId}/status`, { status });
            return response.data;
        }
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
    },

    getStatistics: async () => {
        const response = await api.get<AdminStatisticsDto>('/api/admin/statistics');
        return response.data;
    },

    getKeywords: async (page = 0, size = 50) => {
        const response = await api.get<PaginatedResponse<ScamKeywordDto>>(`/api/admin/keywords?page=${page}&size=${size}`);
        return response.data;
    },

    addKeyword: async (data: ScamKeywordRequest) => {
        const response = await api.post<ScamKeywordDto>('/api/admin/keywords', data);
        return response.data;
    },

    deleteKeyword: async (id: string) => {
        await api.delete(`/api/admin/keywords/${id}`);
    }
};
