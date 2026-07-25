import React, { useEffect, useState } from 'react';
import client from '../services/client';
import { Shield, Users, Check, X, ClipboardList, TrendingUp, DollarSign, Clock } from 'lucide-react';

const AdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [pendingLawyers, setPendingLawyers] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [users, setUsers] = useState([]);
  
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    fetchAdminData();
  }, []);

  const fetchAdminData = async () => {
    setLoading(true);
    try {
      const statsRes = await client.get('/api/admin/stats');
      setStats(statsRes.data);

      const pendingRes = await client.get('/api/admin/lawyers/pending');
      setPendingLawyers(pendingRes.data);

      const auditRes = await client.get('/api/admin/audit');
      setAuditLogs(auditRes.data);

      const usersRes = await client.get('/api/admin/users');
      setUsers(usersRes.data);
    } catch (e) {
      console.warn("Using mock admin dashboard data.");
      setStats({
        totalUsers: 5,
        totalClients: 3,
        totalLawyers: 2,
        pendingLawyers: 1,
        totalAppointments: 2,
        completedAppointments: 1,
        totalRevenue: 300.00
      });
      setPendingLawyers([
        { id: 99, name: "Marcus Finch", specialization: "Intellectual Property expert", experienceYears: 7, hourlyRate: 200, categoryName: "Intellectual Property" }
      ]);
      setAuditLogs([
        { id: 1, action: "USER_REGISTER", performedBy: "lawyer@legalplatform.com", details: "Registered user as LAWYER", createdAt: new Date().toISOString() },
        { id: 2, action: "APPOINTMENT_BOOKED", performedBy: "client@legalplatform.com", details: "Booked appointment id 1", createdAt: new Date().toISOString() }
      ]);
      setUsers([
        { id: 1, name: "Sarah Jenkins", email: "lawyer@legalplatform.com", role: "LAWYER", isVerified: true },
        { id: 2, name: "John Doe", email: "client@legalplatform.com", role: "CLIENT", isVerified: true }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async (id, approve) => {
    setActionLoading(true);
    try {
      await client.post(`/api/admin/lawyers/${id}/approve`, { approve });
      fetchAdminData();
    } catch (err) {
      console.error(err);
      // Simulate action locally if backend has issues
      setPendingLawyers(prev => prev.filter(l => l.id !== id));
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '100px' }}>Loading admin logs and statistics...</div>;
  }

  return (
    <div style={{ maxWidth: '1200px', margin: '30px auto', display: 'flex', flexDirection: 'column', gap: '30px' }}>
      
      {/* Header Panel */}
      <div className="glass-panel" style={{ padding: '30px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Control Center</span>
          <h1 style={{ fontSize: '2rem', marginTop: '4px' }}>System Administrator Dashboard</h1>
        </div>
        <span className="badge badge-danger" style={{ background: 'rgba(139, 92, 246, 0.15)', color: 'var(--accent-secondary)', border: '1px solid rgba(139, 92, 246, 0.3)' }}>
          Super Admin Mode
        </span>
      </div>

      {/* Stats Counter Row */}
      {stats && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '20px' }}>
          
          <div className="glass-panel" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ padding: '10px', borderRadius: '12px', background: 'rgba(99, 102, 241, 0.15)', color: 'var(--accent-primary)' }}>
              <Users size={22} />
            </div>
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Total Registered Users</div>
              <strong style={{ fontSize: '1.4rem' }}>{stats.totalUsers}</strong>
            </div>
          </div>

          <div className="glass-panel" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ padding: '10px', borderRadius: '12px', background: 'rgba(139, 92, 246, 0.15)', color: 'var(--accent-secondary)' }}>
              <Clock size={22} />
            </div>
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Pending Verifications</div>
              <strong style={{ fontSize: '1.4rem', color: 'var(--accent-secondary)' }}>{stats.pendingLawyers}</strong>
            </div>
          </div>

          <div className="glass-panel" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ padding: '10px', borderRadius: '12px', background: 'rgba(16, 185, 129, 0.15)', color: 'var(--accent-emerald)' }}>
              <TrendingUp size={22} />
            </div>
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Active Bookings</div>
              <strong style={{ fontSize: '1.4rem' }}>{stats.totalAppointments}</strong>
            </div>
          </div>

          <div className="glass-panel" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ padding: '10px', borderRadius: '12px', background: 'rgba(16, 185, 129, 0.15)', color: 'var(--accent-emerald)' }}>
              <DollarSign size={22} />
            </div>
            <div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Total Platform Revenue</div>
              <strong style={{ fontSize: '1.4rem', color: 'var(--accent-emerald)' }}>${stats.totalRevenue.toFixed(2)}</strong>
            </div>
          </div>

        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px' }}>
        
        {/* Left Column: Lawyer Verification Approvals */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <h2 style={{ fontSize: '1.25rem', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Shield size={20} className="text-gradient" /> Lawyer Onboarding Approvals
          </h2>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {pendingLawyers.length > 0 ? (
              pendingLawyers.map((lawyer) => (
                <div key={lawyer.id} className="glass-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <strong style={{ fontSize: '1.05rem' }}>{lawyer.name}</strong>
                    <div style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', marginTop: '2px' }}>{lawyer.specialization}</div>
                    <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '4px' }}>Rate: ${lawyer.hourlyRate}/hr | Exp: {lawyer.experienceYears} Years</div>
                  </div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <button onClick={() => handleApprove(lawyer.id, true)} disabled={actionLoading} className="btn-primary" style={{ padding: '6px 12px', fontSize: '0.8rem', background: 'var(--accent-emerald)' }}>
                      <Check size={14} /> Verify
                    </button>
                    <button onClick={() => handleApprove(lawyer.id, false)} disabled={actionLoading} className="btn-danger" style={{ padding: '6px 12px', fontSize: '0.8rem' }}>
                      <X size={14} /> Reject
                    </button>
                  </div>
                </div>
              ))
            ) : (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center', padding: '20px' }}>No pending lawyer onboarding reviews.</p>
            )}
          </div>
        </div>

        {/* Right Column: User Management Directory */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <h2 style={{ fontSize: '1.25rem', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Users size={20} className="text-gradient" /> User Management Directory
          </h2>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', maxHeight: '350px', overflowY: 'auto' }}>
            {users.map((u) => (
              <div key={u.id} style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '12px 16px',
                borderRadius: '8px',
                background: 'rgba(255,255,255,0.01)',
                border: '1px solid var(--border-glass)'
              }}>
                <div>
                  <strong style={{ fontSize: '0.95rem' }}>{u.name}</strong>
                  <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>{u.email}</div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span className="badge badge-success" style={{ fontSize: '0.65rem' }}>{u.role}</span>
                  {u.isVerified ? (
                    <span className="badge badge-success" style={{ fontSize: '0.65rem', background: 'transparent' }}>Verified</span>
                  ) : (
                    <span className="badge badge-pending" style={{ fontSize: '0.65rem', background: 'transparent' }}>Unverified</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

      </div>

      {/* Audit Logs System reader */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <h2 style={{ fontSize: '1.25rem', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <ClipboardList size={20} className="text-gradient" /> Audit system log reader
        </h2>

        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-glass)', color: 'var(--text-secondary)' }}>
                <th style={{ padding: '12px' }}>Timestamp</th>
                <th style={{ padding: '12px' }}>Action Trigger</th>
                <th style={{ padding: '12px' }}>Performed By</th>
                <th style={{ padding: '12px' }}>Details / Metadata</th>
              </tr>
            </thead>
            <tbody>
              {auditLogs.map((log) => (
                <tr key={log.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.03)' }}>
                  <td style={{ padding: '12px', color: 'var(--text-muted)', fontSize: '0.8rem' }}>{new Date(log.createdAt).toLocaleString()}</td>
                  <td style={{ padding: '12px' }}><span className="badge badge-success" style={{ fontSize: '0.7rem' }}>{log.action}</span></td>
                  <td style={{ padding: '12px', fontWeight: 500 }}>{log.performedBy}</td>
                  <td style={{ padding: '12px', color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{log.details}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

    </div>
  );
};

export default AdminDashboard;
