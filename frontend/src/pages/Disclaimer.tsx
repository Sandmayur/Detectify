import React from 'react';

export const Disclaimer: React.FC = () => {
    return (
        <div className="max-w-4xl mx-auto px-4 py-16">
            <h1 className="text-3xl font-bold mb-6">Legal Disclaimer</h1>
            <div className="prose prose-slate max-w-none">
                <p className="text-sm text-slate-500 mb-8">Last Updated: [Date]</p>
                
                <div className="bg-amber-50 border border-amber-200 text-amber-800 p-4 rounded-lg mb-8 font-medium">
                    This is a template and has been flagged for legal review.
                </div>

                <h2>Risk Assessment, Not Fact</h2>
                <p>Detectify is an automated tool that analyzes public data, domain records, and user-submitted reports to generate a <strong>risk assessment</strong> score for job opportunities and companies. </p>
                
                <p>The information provided by Detectify does not constitute legal or professional advice. We do not definitively declare any entity, company, or individual as a scam, fraud, or legitimate business.</p>
                
                <h2>User-Submitted Content</h2>
                <p>Community reports represent unverified allegations submitted by individual users. Detectify does not independently verify the factual accuracy of every report and assumes no liability for the content submitted by users.</p>
                
                <h2>Your Responsibility</h2>
                <p>Users are strongly encouraged to conduct their own independent research and exercise due diligence before accepting any job offer, making payments, or sharing sensitive personal information with any prospective employer.</p>
            </div>
        </div>
    );
};
