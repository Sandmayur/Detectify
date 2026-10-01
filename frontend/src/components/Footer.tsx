import React from 'react';
import { Link } from 'react-router-dom';
import { Shield } from 'lucide-react';

export const Footer: React.FC = () => {
    return (
        <footer className="bg-slate-900 text-slate-400 py-12 mt-auto">
            <div className="max-w-6xl mx-auto px-4 grid grid-cols-1 md:grid-cols-4 gap-8">
                <div className="md:col-span-2">
                    <div className="flex items-center gap-2 text-white mb-4">
                        <Shield className="w-6 h-6 text-indigo-500" />
                        <span className="text-xl font-bold">Detectify</span>
                    </div>
                    <p className="text-sm leading-relaxed max-w-sm mb-4">
                        Protecting job seekers through data-driven risk analysis and community intelligence.
                    </p>
                    <p className="text-xs text-slate-500">
                        Disclaimer: Detectify provides risk assessments based on available data and community reports. It does not definitively declare any entity as a scam or fraud. Always conduct your own research.
                    </p>
                </div>
                <div>
                    <h3 className="text-white font-semibold mb-4">Resources</h3>
                    <ul className="space-y-2 text-sm">
                        <li><Link to="/reports" className="hover:text-white transition-colors">Community Reports</Link></li>
                        <li><Link to="/dispute" className="hover:text-white transition-colors">Company Appeal</Link></li>
                    </ul>
                </div>
                <div>
                    <h3 className="text-white font-semibold mb-4">Legal</h3>
                    <ul className="space-y-2 text-sm">
                        <li><Link to="/privacy" className="hover:text-white transition-colors">Privacy Policy</Link></li>
                        <li><Link to="/terms" className="hover:text-white transition-colors">Terms of Service</Link></li>
                        <li><Link to="/disclaimer" className="hover:text-white transition-colors">Disclaimer</Link></li>
                    </ul>
                </div>
            </div>
            <div className="max-w-6xl mx-auto px-4 mt-12 pt-8 border-t border-slate-800 text-sm text-center">
                &copy; {new Date().getFullYear()} Detectify. All rights reserved.
            </div>
        </footer>
    );
};
