import { api } from './api';

export interface AnalysisRequest {
    companyIdentifier: string;
    jobUrl?: string;
    jobTitle?: string;
    jobDescription?: string;
    recruiterEmail?: string;
}

export interface SignalDto {
    signalName: string;
    signalType: 'POSITIVE' | 'NEUTRAL' | 'WARNING' | 'RED_FLAG';
    points: number;
    explanation: string;
    evidenceJson?: string;
    confidence: 'LOW' | 'MEDIUM' | 'HIGH';
    dataSource?: string;
}

export interface AnalysisResponse {
    checkId: string;
    companyId: string;
    companyName: string;
    rulesVersion: string;
    finalScore: number;
    riskCategory: 'LOW_RISK' | 'MEDIUM_RISK' | 'HIGH_RISK';
    createdAt: string;
    signals: SignalDto[];
}

export const analysisApi = {
    analyze: async (data: AnalysisRequest) => {
        const response = await api.post<AnalysisResponse>('/api/analysis', data);
        return response.data;
    }
};
