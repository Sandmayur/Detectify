import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export const AdminRoute: React.FC = () => {
    const { user, isLoading } = useAuth();

    if (isLoading) {
        return <div className="flex justify-center items-center h-screen"><span className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></span></div>;
    }

    if (!user || user.role !== 'ADMIN') {
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
};
