import React from 'react';

export const PrivacyPolicy: React.FC = () => {
    return (
        <div className="max-w-4xl mx-auto px-4 py-16">
            <h1 className="text-3xl font-bold mb-6">Privacy Policy</h1>
            <div className="prose prose-slate max-w-none">
                <p className="text-sm text-slate-500 mb-8">Last Updated: [Date]</p>
                
                <div className="bg-amber-50 border border-amber-200 text-amber-800 p-4 rounded-lg mb-8 font-medium">
                    This is a template and has been flagged for legal review.
                </div>

                <h2>1. Information We Collect</h2>
                <p>We collect information you provide directly to us when you create an account, submit a community report, or communicate with us. This may include your email address and any other information you choose to provide.</p>
                
                <h2>2. How We Use Your Information</h2>
                <p>We use the information we collect to provide, maintain, and improve our services, as well as to verify the authenticity of community reports. We do not sell your personal data.</p>
                
                <h2>3. Account Deletion</h2>
                <p>You have the right to delete your account at any time. When you delete your account, your personal data is removed, and any community reports you have submitted are anonymized.</p>
                
                <h2>4. Third-Party Services</h2>
                <p>We may use third-party services for hosting, analytics, and data enrichment. These services have their own privacy policies.</p>
                
                <h2>5. Contact Us</h2>
                <p>If you have any questions about this Privacy Policy, please contact us at privacy@detectify.example.com.</p>
            </div>
        </div>
    );
};
