import React from 'react';
import { type ReportResponse, reportApi } from '../services/reportApi';
import { useAuth } from '../contexts/AuthContext';
import { ThumbsUp, ThumbsDown, Link as LinkIcon, Mail, Phone, Image as ImageIcon } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface ReportCardProps {
    report: ReportResponse;
    onVoteUpdated: (updatedReport: ReportResponse) => void;
}

export const ReportCard: React.FC<ReportCardProps> = ({ report, onVoteUpdated }) => {
    const { user } = useAuth();
    const navigate = useNavigate();
    
    const handleVote = async (isUpvote: boolean) => {
        if (!user) {
            navigate('/login');
            return;
        }
        
        try {
            const updated = await reportApi.vote(report.id, isUpvote);
            onVoteUpdated(updated);
        } catch (error: any) {
            if (error.response?.data?.message) {
                alert(error.response.data.message);
            }
        }
    };

    return (
        <div className="bg-white p-6 rounded-2xl shadow-sm border border-slate-200">
            <div className="flex gap-4">
                <div className="flex flex-col items-center gap-1 min-w-[40px]">
                    <button 
                        onClick={() => handleVote(true)}
                        className={`p-1.5 rounded-lg transition-colors ${report.userHasVoted && report.userVoteIsUpvote === true ? 'bg-indigo-100 text-indigo-600' : 'text-slate-400 hover:bg-slate-100 hover:text-indigo-600'}`}
                        aria-label="Upvote"
                    >
                        <ThumbsUp className="w-5 h-5" />
                    </button>
                    <span className={`font-semibold ${report.netUpvotes > 0 ? 'text-indigo-600' : report.netUpvotes < 0 ? 'text-red-600' : 'text-slate-600'}`}>
                        {report.netUpvotes}
                    </span>
                    <button 
                        onClick={() => handleVote(false)}
                        className={`p-1.5 rounded-lg transition-colors ${report.userHasVoted && report.userVoteIsUpvote === false ? 'bg-red-100 text-red-600' : 'text-slate-400 hover:bg-slate-100 hover:text-red-600'}`}
                        aria-label="Downvote"
                    >
                        <ThumbsDown className="w-5 h-5" />
                    </button>
                </div>
                
                <div className="flex-1">
                    <div className="flex justify-between items-start">
                        <div>
                            <h3 className="text-lg font-bold text-slate-900">{report.companyName}</h3>
                            {report.companyDomain && (
                                <a href={`https://${report.companyDomain}`} target="_blank" rel="noreferrer" className="text-sm text-indigo-600 hover:underline">
                                    {report.companyDomain}
                                </a>
                            )}
                        </div>
                        <span className="text-xs text-slate-500 bg-slate-100 px-2 py-1 rounded-full">
                            {new Date(report.createdAt).toLocaleDateString()}
                        </span>
                    </div>
                    
                    <p className="mt-4 text-slate-700 whitespace-pre-wrap">
                        {report.description}
                    </p>
                    
                    <div className="mt-6 flex flex-wrap gap-3">
                        {report.jobUrl && (
                            <div className="flex items-center gap-1.5 text-sm text-slate-600 bg-slate-50 border border-slate-200 px-3 py-1.5 rounded-lg">
                                <LinkIcon className="w-4 h-4 text-slate-400" />
                                <a href={report.jobUrl} target="_blank" rel="noreferrer" className="hover:text-indigo-600 truncate max-w-[200px]">
                                    {report.jobUrl}
                                </a>
                            </div>
                        )}
                        {report.recruiterEmail && (
                            <div className="flex items-center gap-1.5 text-sm text-slate-600 bg-slate-50 border border-slate-200 px-3 py-1.5 rounded-lg">
                                <Mail className="w-4 h-4 text-slate-400" />
                                <span>{report.recruiterEmail}</span>
                            </div>
                        )}
                        {report.recruiterPhone && (
                            <div className="flex items-center gap-1.5 text-sm text-slate-600 bg-slate-50 border border-slate-200 px-3 py-1.5 rounded-lg">
                                <Phone className="w-4 h-4 text-slate-400" />
                                <span>{report.recruiterPhone}</span>
                            </div>
                        )}
                        {report.evidenceUrl && (
                            <div className="flex items-center gap-1.5 text-sm text-slate-600 bg-slate-50 border border-slate-200 px-3 py-1.5 rounded-lg">
                                <ImageIcon className="w-4 h-4 text-slate-400" />
                                <a href={report.evidenceUrl} target="_blank" rel="noreferrer" className="hover:text-indigo-600">
                                    View Evidence
                                </a>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};
