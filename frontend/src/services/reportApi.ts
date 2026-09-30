import { api } from './api';

export interface SubmitReportRequest {
    companyDomain?: string;
    companyName?: string;
    description: string;
    jobUrl?: string;
    recruiterEmail?: string;
    recruiterPhone?: string;
    evidenceUrl?: string;
}

export interface ReportResponse {
    id: string;
    companyId: string;
    companyName: string;
    companyDomain?: string;
    description: string;
    jobUrl?: string;
    recruiterEmail?: string;
    recruiterPhone?: string;
    evidenceUrl?: string;
    netUpvotes: number;
    status: string;
    createdAt: string;
    userHasVoted: boolean;
    userVoteIsUpvote: boolean | null;
}

export interface PaginatedResponse<T> {
    content: T[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

export const reportApi = {
    submitReport: async (data: SubmitReportRequest) => {
        const response = await api.post<ReportResponse>('/api/reports', data);
        return response.data;
    },
    
    getReports: async (page = 0, size = 20) => {
        const response = await api.get<PaginatedResponse<ReportResponse>>(`/api/reports?page=${page}&size=${size}`);
        return response.data;
    },

    vote: async (reportId: string, isUpvote: boolean) => {
        const response = await api.post<ReportResponse>(`/api/reports/${reportId}/vote`, { isUpvote });
        return response.data;
    }
};
