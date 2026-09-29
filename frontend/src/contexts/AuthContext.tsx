import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { api, setAccessToken } from '../services/api';

export interface User {
    id: string;
    email: string;
    role: string;
}

interface AuthContextType {
    user: User | null;
    login: (user: User, token: string) => void;
    logout: () => void;
    isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const attemptRefresh = async () => {
            try {
                const res = await api.post('/api/auth/refresh');
                setAccessToken(res.data.accessToken);
                setUser(res.data.user);
            } catch (error) {
                // Ignore, means user is not logged in
            } finally {
                setIsLoading(false);
            }
        };

        attemptRefresh();

        const handleLogout = () => {
            setUser(null);
        };

        const handleTokenRefreshed = (e: Event) => {
            const customEvent = e as CustomEvent;
            if (customEvent.detail && customEvent.detail.user) {
                setUser(customEvent.detail.user);
            }
        };

        window.addEventListener('logout', handleLogout);
        window.addEventListener('tokenRefreshed', handleTokenRefreshed);

        return () => {
            window.removeEventListener('logout', handleLogout);
            window.removeEventListener('tokenRefreshed', handleTokenRefreshed);
        };
    }, []);

    const login = (newUser: User, token: string) => {
        setAccessToken(token);
        setUser(newUser);
    };

    const logout = async () => {
        try {
            await api.post('/api/auth/logout');
        } catch (error) {
            console.error(error);
        }
        setAccessToken(null);
        setUser(null);
    };

    return (
        <AuthContext.Provider value={{ user, login, logout, isLoading }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};
