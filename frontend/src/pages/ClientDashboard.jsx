import React, { useEffect, useState } from 'react';
import client from '../services/client';
import { useAuth } from '../context/AuthContext';
import { Calendar, FileText, Bell, CheckSquare, Upload, ShieldAlert, BookOpen } from 'lucide-react';

const ClientDashboard = () => {
  const { user } = useAuth();
  
  const [appointments, setAppointments] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [notifications, setNotifications] = useState([]);
  
  // Document upload states
  const [file, setFile] = useState(null);
  const [uploadLoading, setUploadLoading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  const [uploadSuccess, setUploadSuccess] = useState('');

  // Active AI checklists
  const [aiChecklist, setAiChecklist] = useState([
    { id: 1, text: "Review signed Employment Agreement clauses", completed: false },
    { id: 2, text: "Collate bank transfer transaction statements", completed: false },
    { id: 3, text: "Book follow-up consultation slot with Sarah Jenkins", completed: false }
  ]);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const apptsRes = await client.get('/api/appointments/client');
      setAppointments(apptsRes.data);
    } catch (e) {
      console.warn("Using mock appointments.");
      setAppointments([
        { id: 201, lawyerName: "Sarah Jenkins", specialization: "Labor Law", appointmentDate: "2026-07-28", startTime: "10:00:00", endTime: "11:00:00", status: "PENDING", notes: "Discuss severance draft review" }
      ]);
    }

    try {
      const docsRes = await client.get('/api/documents');
      setDocuments(docsRes.data);
    } catch (e) {
      console.warn("Using mock documents.");
      setDocuments([
        { id: 301, fileName: "Employment_Contract.txt", path: "/uploads/Employment_Contract.txt", summary: "### Summary\n- **Parties**: Employee John Doe & Employer TechCorp Inc.\n- **Obligation**: 40 hours/week, monthly salary of $5,000.\n- **Termination**: 2-weeks notice period required." }
      ]);
    }

    try {
      const notifsRes = await client.get('/api/notifications');
      setNotifications(notifsRes.data);
    } catch (e) {
      console.warn("Using mock notifications.");
      setNotifications([
        { id: 401, message: "Welcome to LexiFlow! Please set up your first consultation.", isRead: false }
      ]);
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setFile(e.target.files[0]);
    }
  };

  const handleUploadSubmit = async (e) => {
    e.preventDefault();
    if (!file) return;

    setUploadLoading(true);
    setUploadError('');
    setUploadSuccess('');
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await client.post('/api/documents/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });

      setUploadSuccess("Document uploaded and analyzed successfully!");
      setFile(null);
      fetchDashboardData();
    } catch (err) {
      setUploadError(err.response?.data?.message || "File analysis failed. Try text/pdf documents.");
    } finally {
      setUploadLoading(false);
    }
  };

  const toggleChecklist = (id) => {
    setAiChecklist(prev => prev.map(item => item.id === id ? { ...item, completed: !item.completed } : item));
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'APPROVED': return <span className="badge badge-success">Approved</span>;
      case 'PENDING': return <span className="badge badge-pending">Pending Review</span>;
      case 'REJECTED': return <span className="badge badge-danger">Rejected</span>;
      default: return <span className="badge badge-pending">{status}</span>;
    }
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '30px auto', display: 'flex', flexDirection: 'column', gap: '30px' }}>
      
      {/* Header section */}
      <div className="glass-panel" style={{ padding: '30px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Client Portal</span>
          <h1 style={{ fontSize: '2rem', marginTop: '4px' }}>Welcome, {user?.name}</h1>
        </div>
        <div style={{ textAlign: 'right' }}>
          <span className="badge badge-success">Verified Client</span>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '30px' }}>
        
        {/* Left Column: Appointments & Documents */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
          
          {/* Consultations Card */}
          <div className="glass-panel" style={{ padding: '24px' }}>
            <h2 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.25rem', marginBottom: '20px' }}>
              <Calendar size={20} className="text-gradient" /> Upcoming Consultations
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {appointments.length > 0 ? (
                appointments.map((appt) => (
                  <div key={appt.id} className="glass-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <strong style={{ fontSize: '1.1rem', color: 'var(--text-primary)' }}>{appt.lawyerName || 'Sarah Jenkins'}</strong>
                      <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '2px' }}>{appt.specialization || 'Labor Law'}</div>
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '4px' }}>
                        Date: {appt.appointmentDate} | Time: {appt.startTime.substring(0, 5)} - {appt.endTime.substring(0, 5)}
                      </div>
                      {appt.notes && <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '8px', fontStyle: 'italic' }}>Notes: {appt.notes}</div>}
                    </div>
                    <div>
                      {getStatusBadge(appt.status)}
                    </div>
                  </div>
                ))
              ) : (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center', padding: '20px' }}>No consultations booked yet.</p>
              )}
            </div>
          </div>

          {/* Document Center */}
          <div className="glass-panel" style={{ padding: '24px' }}>
            <h2 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.25rem', marginBottom: '20px' }}>
              <FileText size={20} className="text-gradient" /> AI Document Summarizer & Vault
            </h2>

            {/* Upload form */}
            <form onSubmit={handleUploadSubmit} className="glass-card" style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', alignItems: 'center', marginBottom: '24px', background: 'rgba(255,255,255,0.01)' }}>
              <div style={{ flex: 1, minWidth: '200px' }}>
                <label className="btn-secondary" style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', justifyContent: 'center' }}>
                  <Upload size={18} /> {file ? file.name : 'Select legal document (txt/pdf)'}
                  <input type="file" required style={{ display: 'none' }} accept=".txt,.pdf" onChange={handleFileChange} />
                </label>
              </div>
              <button type="submit" className="btn-primary" disabled={uploadLoading || !file} style={{ minWidth: '150px', justifyContent: 'center' }}>
                {uploadLoading ? 'Analyzing...' : 'Analyze & Save'}
              </button>
            </form>

            {uploadSuccess && <div className="badge-success" style={{ padding: '10px', borderRadius: '6px', marginBottom: '20px', fontSize: '0.85rem' }}>{uploadSuccess}</div>}
            {uploadError && <div className="badge-danger" style={{ padding: '10px', borderRadius: '6px', marginBottom: '20px', fontSize: '0.85rem' }}>{uploadError}</div>}

            {/* Document Listing */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {documents.map((doc) => (
                <details key={doc.id} className="glass-card" style={{ cursor: 'pointer' }}>
                  <summary style={{ fontWeight: 600, display: 'flex', justifyContent: 'space-between', alignItems: 'center', listStyle: 'none' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <FileText size={18} style={{ color: 'var(--accent-primary)' }} />
                      <span>{doc.fileName}</span>
                    </div>
                    <span style={{ fontSize: '0.8rem', color: 'var(--accent-primary)' }}>View AI Summary</span>
                  </summary>
                  <div style={{ marginTop: '16px', borderTop: '1px solid var(--border-glass)', paddingTop: '16px', fontSize: '0.9rem', color: 'var(--text-secondary)', whiteSpace: 'pre-line' }}>
                    {doc.summary}
                  </div>
                </details>
              ))}
            </div>
          </div>

        </div>

        {/* Right Column: Checklists & Alerts */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
          
          {/* AI Checklist */}
          <div className="glass-panel" style={{ padding: '24px' }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.15rem', marginBottom: '20px' }}>
              <CheckSquare size={18} style={{ color: 'var(--accent-secondary)' }} /> Case Tasks Checklist
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {aiChecklist.map((item) => (
                <div 
                  key={item.id} 
                  onClick={() => toggleChecklist(item.id)}
                  style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: '10px',
                    cursor: 'pointer',
                    color: item.completed ? 'var(--text-muted)' : 'var(--text-primary)',
                    textDecoration: item.completed ? 'line-through' : 'none',
                    fontSize: '0.85rem'
                  }}
                >
                  <input type="checkbox" checked={item.completed} readOnly style={{ marginTop: '2px', accentColor: 'var(--accent-primary)' }} />
                  <span>{item.text}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Notifications Console */}
          <div className="glass-panel" style={{ padding: '24px' }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.15rem', marginBottom: '20px' }}>
              <Bell size={18} style={{ color: 'var(--accent-amber)' }} /> Alerts Console
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', maxHeight: '300px', overflowY: 'auto' }}>
              {notifications.map((notif) => (
                <div key={notif.id} style={{
                  padding: '12px',
                  borderRadius: '8px',
                  background: 'rgba(255,255,255,0.01)',
                  border: '1px solid var(--border-glass)',
                  fontSize: '0.85rem',
                  color: 'var(--text-secondary)'
                }}>
                  {notif.message}
                </div>
              ))}
            </div>
          </div>

        </div>

      </div>

    </div>
  );
};

export default ClientDashboard;
