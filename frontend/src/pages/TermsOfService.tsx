import React from 'react';

export const TermsOfService: React.FC = () => {
    return (
        <div className="max-w-4xl mx-auto px-4 py-16">
            <h1 className="text-3xl font-bold mb-6">Terms of Service</h1>
            <div className="prose prose-slate max-w-none">
                <p className="text-sm text-slate-500 mb-8">Last Updated: [Date]</p>
                
                <div className="bg-amber-50 border border-amber-200 text-amber-800 p-4 rounded-lg mb-8 font-medium">
                    This is a template and has been flagged for legal review.
                </div>

                <h2>1. Acceptance of Terms</h2>
                <p>By accessing or using our services, you agree to be bound by these Terms of Service.</p>
                
                <h2>2. User Conduct</h2>
                <p>You agree not to misuse the service, including submitting false information, attempting to reverse engineer the risk analysis engine, or using the service for unauthorized commercial purposes.</p>
                
                <h2>3. Community Reports</h2>
                <p>When you submit a community report, you represent that the information provided is truthful to the best of your knowledge. We reserve the right to moderate, approve, or reject reports at our discretion.</p>
                
                <h2>4. Limitation of Liability</h2>
                <p><strong>Disclaimer:</strong> Detectify provides risk assessments based on available data and community reports. It does not definitively declare any entity as a scam or fraud. We are not liable for any employment decisions made based on this information.</p>
                
                <h2>5. Changes to Terms</h2>
                <p>We may modify these terms at any time. Your continued use of the service constitutes acceptance of those changes.</p>
            </div>
        </div>
    );
};
