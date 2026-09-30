import { api } from './api';
import { type DisputeResponse } from './adminApi';

export interface SubmitDisputeRequest {
    reportId?: string;
    companyDomain: string;
    contactEmail: string;
    reason: string;
}

export const disputeApi = {
    submitDispute: async (data: SubmitDisputeRequest) => {
        const response = await api.post<DisputeResponse>('/api/disputes', data);
        return response.data;
    }
};
