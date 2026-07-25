import React, { useEffect, useState } from 'react';
import client from '../services/client';
import { useAuth } from '../context/AuthContext';
import { Calendar, DollarSign, Clock, Check, X, Star, CalendarDays, Plus, Trash } from 'lucide-react';

const LawyerDashboard = () => {
  const { user } = useAuth();
  
  const [appointments, setAppointments] = useState([]);
  const [availabilities, setAvailabilities] = useState([]);
  const [reviews, setReviews] = useState([]);
  
  // Slot configuration form state
  const [dayOfWeek, setDayOfWeek] = useState('Monday');
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('10:00');

  const [loading, setLoading] = useState(true);
  const [earnings, setEarnings] = useState(0.0);
  const [slotMsg, setSlotMsg] = useState('');

  useEffect(() => {
    fetchLawyerData();
  }, []);

  const fetchLawyerData = async () => {
    setLoading(true);
    try {
      // Get appointments
      const apptsRes = await client.get('/api/appointments/lawyer');
      setAppointments(apptsRes.data);

      // Get earnings
      const earningsRes = await client.get('/api/appointments/lawyer/earnings');
      setEarnings(earningsRes.data);

      // Get availability slots
      const availabilityRes = await client.get(`/api/auth/lawyers/my-availability`);
      setAvailabilities(availabilityRes.data);
    } catch (e) {
      console.warn("Using mock lawyer data.");
      setAppointments([
        { id: 201, clientName: "John Doe", appointmentDate: "2026-07-28", startTime: "10:00:00", endTime: "11:00:00", status: "PENDING", notes: "Discuss severance draft review" }
      ]);
      setEarnings(150.0);
      setAvailabilities([
        { id: 101, dayOfWeek: "Monday", startTime: "09:00:00", endTime: "10:00:00", isAvailable: true },
        { id: 102, dayOfWeek: "Wednesday", startTime: "14:00:00", endTime: "15:00:00", isAvailable: true }
      ]);
    }

    try {
      // Fetch reviews
      const reviewsRes = await client.get(`/api/auth/lawyers/my-reviews`);
      setReviews(reviewsRes.data);
    } catch (e) {
      setReviews([
        { id: 1, clientName: "Alice Smith", rating: 5, comment: "Sarah helped me negotiate a fantastic severance package. Highly recommended!" }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatus = async (appointmentId, newStatus) => {
    try {
      await client.put(`/api/appointments/${appointmentId}/status?status=${newStatus}`);
      fetchLawyerData();
    } catch (err) {
      console.error("Failed to update status:", err);
      // Mock update locally if backend offline
      setAppointments(prev => prev.map(a => a.id === appointmentId ? { ...a, status: newStatus } : a));
    }
  };

  const handleAddSlot = async (e) => {
    e.preventDefault();
    setSlotMsg('');
    try {
      const payload = {
        dayOfWeek,
        startTime: startTime + ":00",
        endTime: endTime + ":00"
      };
      await client.post('/api/lawyers/availability', payload);
      setSlotMsg('Availability slot added!');
      fetchLawyerData();
    } catch (err) {
      setSlotMsg(err.response?.data?.message || 'Failed to add availability slot.');
      
      // Mock add slot locally
      setAvailabilities(prev => [
        ...prev,
        { id: Date.now(), dayOfWeek, startTime, endTime, isAvailable: true }
      ]);
    }
  };

  const handleDeleteSlot = async (slotId) => {
    try {
      await client.delete(`/api/lawyers/availability/${slotId}`);
      fetchLawyerData();
    } catch (err) {
      // Mock delete locally
      setAvailabilities(prev => prev.filter(s => s.id !== slotId));
    }
  };

  const pendingAppointments = appointments.filter(a => a.status === 'PENDING');
  const activeAppointments = appointments.filter(a => a.status === 'APPROVED');

  return (
    <div style={{ maxWidth: '1200px', margin: '30px auto', display: 'flex', flexDirection: 'column', gap: '30px' }}>
      
      {/* Header Panel */}
      <div className="glass-panel" style={{ padding: '30px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>Lawyer Portal</span>
          <h1 style={{ fontSize: '2rem', marginTop: '4px' }}>Welcome, {user?.name}</h1>
        </div>
        
        {/* Earnings Stats */}
        <div style={{ display: 'flex', gap: '20px' }}>
          <div className="glass-card" style={{ padding: '12px 24px', display: 'flex', alignItems: 'center', gap: '12px', background: 'rgba(255,255,255,0.01)' }}>
            <DollarSign size={24} style={{ color: 'var(--accent-emerald)' }} />
            <div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Revenue</div>
              <strong style={{ fontSize: '1.2rem', color: 'var(--accent-emerald)' }}>${earnings.toFixed(2)}</strong>
            </div>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '30px' }}>
        
        {/* Left Column: Requests & Active Schedule */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
          
          {/* Pending Requests Queue */}
          <div className="glass-panel" style={{ padding: '24px' }}>
            <h2 style={{ fontSize: '1.25rem', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Clock size={20} className="text-gradient" /> Pending Consultation Requests
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {pendingAppointments.length > 0 ? (
                pendingAppointments.map((appt) => (
                  <div key={appt.id} className="glass-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <strong style={{ fontSize: '1.1rem' }}>Client: {appt.clientName || 'John Doe'}</strong>
                      <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '2px' }}>
                        Date: {appt.appointmentDate} | Slot: {appt.startTime.substring(0, 5)} - {appt.endTime.substring(0, 5)}
                      </div>
                      {appt.notes && <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '8px', fontStyle: 'italic' }}>Notes: "{appt.notes}"</p>}
                    </div>
                    <div style={{ display: 'flex', gap: '10px' }}>
                      <button onClick={() => handleUpdateStatus(appt.id, 'APPROVED')} className="btn-primary" style={{ padding: '8px 16px', background: 'var(--accent-emerald)' }}>
                        <Check size={16} /> Approve
                      </button>
                      <button onClick={() => handleUpdateStatus(appt.id, 'REJECTED')} className="btn-danger" style={{ padding: '8px 16px' }}>
                        <X size={16} /> Reject
                      </button>
                    </div>
                  </div>
                ))
              ) : (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center', padding: '20px' }}>No pending consultation requests.</p>
              )}
            </div>
          </div>

          {/* Active Schedule */}
          <div className="glass-panel" style={{ padding: '24px' }}>
            <h2 style={{ fontSize: '1.25rem', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Calendar size={20} className="text-gradient" /> Confirmed Consultations
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              {activeAppointments.length > 0 ? (
                activeAppointments.map((appt) => (
                  <div key={appt.id} className="glass-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <strong style={{ fontSize: '1.1rem' }}>Client: {appt.clientName || 'John Doe'}</strong>
                      <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '2px' }}>
                        Date: {appt.appointmentDate} | Slot: {appt.startTime.substring(0, 5)} - {appt.endTime.substring(0, 5)}
                      </div>
                    </div>
                    <span className="badge badge-success">Approved</span>
                  </div>
                ))
              ) : (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center', padding: '20px' }}>No active consultations confirmed.</p>
              )}
            </div>
          </div>

        </div>

        {/* Right Column: Availability Slot Editor & Reviews */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
          
          {/* Availability Settings */}
          <div className="glass-panel" style={{ padding: '24px' }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.15rem', marginBottom: '20px' }}>
              <CalendarDays size={18} style={{ color: 'var(--accent-secondary)' }} /> Configure Availability
            </h3>

            {slotMsg && <div className="badge-success" style={{ padding: '10px', borderRadius: '6px', marginBottom: '16px', fontSize: '0.8rem', width: '100%' }}>{slotMsg}</div>}

            <form onSubmit={handleAddSlot} style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginBottom: '24px', paddingBottom: '20px', borderBottom: '1px solid var(--border-glass)' }}>
              <div>
                <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Day of Week</label>
                <select className="input-field" value={dayOfWeek} onChange={(e) => setDayOfWeek(e.target.value)}>
                  {["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"].map(day => (
                    <option key={day} value={day} style={{ background: 'var(--bg-secondary)', color: 'var(--text-primary)' }}>{day}</option>
                  ))}
                </select>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Start Time</label>
                  <input type="time" className="input-field" value={startTime} onChange={(e) => setStartTime(e.target.value)} />
                </div>
                <div>
                  <label style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>End Time</label>
                  <input type="time" className="input-field" value={endTime} onChange={(e) => setEndTime(e.target.value)} />
                </div>
              </div>

              <button type="submit" className="btn-primary" style={{ justifyContent: 'center', padding: '10px', fontSize: '0.9rem', marginTop: '6px' }}>
                <Plus size={16} /> Add Availability Slot
              </button>
            </form>

            {/* List of current slots */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxHeight: '200px', overflowY: 'auto' }}>
              {availabilities.map((slot) => (
                <div key={slot.id} style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '10px 12px',
                  borderRadius: '8px',
                  background: 'rgba(255,255,255,0.01)',
                  border: '1px solid var(--border-glass)',
                  fontSize: '0.85rem'
                }}>
                  <span>{slot.dayOfWeek}: {slot.startTime.substring(0, 5)} - {slot.endTime.substring(0, 5)}</span>
                  <button onClick={() => handleDeleteSlot(slot.id)} style={{ background: 'transparent', border: 'none', color: 'var(--accent-rose)', cursor: 'pointer' }}>
                    <Trash size={14} />
                  </button>
                </div>
              ))}
            </div>
          </div>

          {/* Client Reviews */}
          <div className="glass-panel" style={{ padding: '24px' }}>
            <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.15rem', marginBottom: '20px' }}>
              <Star size={18} style={{ color: 'var(--accent-amber)' }} /> Client Reviews
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', maxHeight: '240px', overflowY: 'auto' }}>
              {reviews.map((rev) => (
                <div key={rev.id} style={{ paddingBottom: '12px', borderBottom: '1px solid var(--border-glass)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem' }}>
                    <strong>{rev.clientName}</strong>
                    <span style={{ color: 'var(--accent-amber)', display: 'flex', alignItems: 'center', gap: '2px' }}>
                      <Star size={12} fill="var(--accent-amber)" /> {rev.rating}
                    </span>
                  </div>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', marginTop: '4px' }}>"{rev.comment}"</p>
                </div>
              ))}
            </div>
          </div>

        </div>

      </div>

    </div>
  );
};

export default LawyerDashboard;
