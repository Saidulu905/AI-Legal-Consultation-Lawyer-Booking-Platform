import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import AIChat from './pages/AIChat';
import Lawyers from './pages/Lawyers';
import LawyerDetail from './pages/LawyerDetail';
import ClientDashboard from './pages/ClientDashboard';
import LawyerDashboard from './pages/LawyerDashboard';
import AdminDashboard from './pages/AdminDashboard';
import { Scale, LogOut, LayoutDashboard, MessageSquare, Shield, Users, Calendar } from 'lucide-react';

// Protected Route Guard
const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, token, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <div className="text-gradient" style={{ fontSize: '1.5rem', fontWeight: 600 }}>Loading Application...</div>
      </div>
    );
  }

  if (!token || !user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/" replace />;
  }

  return children;
};

// Navbar Component
const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="glass-panel" style={{
      margin: '16px 24px',
      padding: '12px 24px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      position: 'sticky',
      top: '16px',
      zIndex: 1000
    }}>
      <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '8px', textDecoration: 'none', color: 'inherit' }}>
        <Scale className="text-gradient" size={28} />
        <span style={{ fontWeight: 800, fontSize: '1.4rem', letterSpacing: '-0.03em' }}>
          LEXI<span className="text-gradient">FLOW</span>
        </span>
      </Link>

      <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
        <Link to="/" style={{ textDecoration: 'none', color: 'var(--text-secondary)', fontWeight: 500, transition: 'var(--transition-smooth)' }} className="nav-link">Home</Link>
        <Link to="/lawyers" style={{ textDecoration: 'none', color: 'var(--text-secondary)', fontWeight: 500 }} className="nav-link">Find Lawyers</Link>
        
        {user && (
          <>
            <Link to="/ai-chat" style={{ textDecoration: 'none', color: 'var(--text-secondary)', fontWeight: 500 }} className="nav-link">AI Consult</Link>
            
            {user.role === 'CLIENT' && (
              <Link to="/dashboard/client" style={{ display: 'flex', alignItems: 'center', gap: '6px', textDecoration: 'none', color: 'var(--accent-primary)', fontWeight: 600 }}>
                <LayoutDashboard size={18} /> Dashboard
              </Link>
            )}
            {user.role === 'LAWYER' && (
              <Link to="/dashboard/lawyer" style={{ display: 'flex', alignItems: 'center', gap: '6px', textDecoration: 'none', color: 'var(--accent-primary)', fontWeight: 600 }}>
                <LayoutDashboard size={18} /> Lawyer Panel
              </Link>
            )}
            {user.role === 'ADMIN' && (
              <Link to="/dashboard/admin" style={{ display: 'flex', alignItems: 'center', gap: '6px', textDecoration: 'none', color: 'var(--accent-secondary)', fontWeight: 600 }}>
                <Shield size={18} /> Admin Panel
              </Link>
            )}
          </>
        )}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        {user ? (
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
              Hello, <strong style={{ color: 'var(--text-primary)' }}>{user.name}</strong>
            </span>
            <button onClick={handleLogout} className="btn-secondary" style={{ padding: '8px 16px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <LogOut size={16} /> Logout
            </button>
          </div>
        ) : (
          <>
            <Link to="/login" className="btn-secondary" style={{ padding: '8px 20px', textDecoration: 'none' }}>Log In</Link>
            <Link to="/register" className="btn-primary" style={{ padding: '8px 20px', textDecoration: 'none' }}>Get Started</Link>
          </>
        )}
      </div>
    </nav>
  );
};

// Footer Component
const Footer = () => {
  return (
    <footer className="glass-panel" style={{
      margin: '40px 24px 24px 24px',
      padding: '24px',
      textAlign: 'center',
      fontSize: '0.9rem',
      color: 'var(--text-muted)'
    }}>
      <p>&copy; {new Date().getFullYear()} LexiFlow AI Legal Consultation & Lawyer Booking Platform. All rights reserved.</p>
      <p style={{ marginTop: '8px', fontSize: '0.8rem' }}>Disclaimer: AI consultation responses are generated for educational guidance and do not constitute formal legal representation.</p>
    </footer>
  );
};

// Main App Router Component
function App() {
  return (
    <AuthProvider>
      <Router>
        <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
          <Navbar />
          <div style={{ flex: 1, padding: '0 24px' }}>
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/lawyers" element={<Lawyers />} />
              <Route path="/lawyers/:id" element={<LawyerDetail />} />
              
              {/* Client Routes */}
              <Route path="/ai-chat" element={
                <ProtectedRoute allowedRoles={['CLIENT']}>
                  <AIChat />
                </ProtectedRoute>
              } />
              <Route path="/dashboard/client" element={
                <ProtectedRoute allowedRoles={['CLIENT']}>
                  <ClientDashboard />
                </ProtectedRoute>
              } />

              {/* Lawyer Routes */}
              <Route path="/dashboard/lawyer" element={
                <ProtectedRoute allowedRoles={['LAWYER']}>
                  <LawyerDashboard />
                </ProtectedRoute>
              } />

              {/* Admin Routes */}
              <Route path="/dashboard/admin" element={
                <ProtectedRoute allowedRoles={['ADMIN']}>
                  <AdminDashboard />
                </ProtectedRoute>
              } />

              {/* Fallback */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </div>
          <Footer />
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
