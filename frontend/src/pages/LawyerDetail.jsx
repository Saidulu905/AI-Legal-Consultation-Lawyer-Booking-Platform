import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import client from '../services/client';
import { useAuth } from '../context/AuthContext';
import { Calendar, Clock, Award, Star, Mail, ShieldAlert, Sparkles } from 'lucide-react';

const LawyerDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [lawyer, setLawyer] = useState(null);
  const [availabilities, setAvailabilities] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [selectedSlot, setSelectedSlot] = useState(null);
  const [selectedDate, setSelectedDate] = useState('');
  const [notes, setNotes] = useState('');
  
  const [loading, setLoading] = useState(true);
  const [bookingLoading, setBookingLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    fetchLawyerDetails();
  }, [id]);

  const fetchLawyerDetails = async () => {
    setLoading(true);
    try {
      // Fetch details
      const profileRes = await client.get(`/api/auth/lawyers/${id}`);
      setLawyer(profileRes.data);

      // Fetch availabilities
      const availabilityRes = await client.get(`/api/auth/lawyers/${id}/availability`);
      setAvailabilities(availabilityRes.data);

      // Fetch reviews
      const reviewsRes = await client.get(`/api/auth/lawyers/${id}/reviews`);
      setReviews(reviewsRes.data);
    } catch (err) {
      console.warn("Backend offline or error. Simulating details:", err);
      // Mock Fallback
      setLawyer({
        id: parseInt(id),
        name: "Sarah Jenkins",
        specialization: "Labor & Employment Law specialist",
        experienceYears: 10,
        hourlyRate: 150.0,
        categoryName: "Labor Law",
        averageRating: 4.8,
        bio: "Experienced attorney with over 10 years representing clients in wage claims, severance negotiations, and workplace disputes."
      });

      setAvailabilities([
        { id: 101, dayOfWeek: "Monday", startTime: "09:00:00", endTime: "10:00:00", isAvailable: true },
        { id: 102, dayOfWeek: "Wednesday", startTime: "14:00:00", endTime: "15:00:00", isAvailable: true },
        { id: 103, dayOfWeek: "Friday", startTime: "11:00:00", endTime: "12:00:00", isAvailable: true }
      ]);

      setReviews([
        { id: 1, clientName: "Alice Smith", rating: 5, comment: "Sarah helped me negotiate a fantastic severance package. Highly recommended!" },
        { id: 2, clientName: "Robert Green", rating: 4, comment: "Very thorough explanation of contract terms." }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleBook = async (e) => {
    e.preventDefault();
    if (!user) {
      navigate('/login');
      return;
    }
    if (user.role !== 'CLIENT') {
      setErrorMsg("Only Client accounts can book consultations.");
      return;
    }
    if (!selectedSlot || !selectedDate) {
      setErrorMsg("Please select a slot and date.");
      return;
    }

    setBookingLoading(true);
    setErrorMsg('');
    setSuccessMsg('');
    try {
      const payload = {
        lawyerId: lawyer.id,
        appointmentDate: selectedDate,
        startTime: selectedSlot.startTime,
        endTime: selectedSlot.endTime,
        notes: notes
      };

      await client.post('/api/appointments', payload);
      setSuccessMsg("Consultation booked successfully! Status is pending lawyer confirmation.");
      setSelectedSlot(null);
      setNotes('');
    } catch (err) {
      setErrorMsg(err.response?.data?.message || "Booking failed. Slot may already be reserved.");
    } finally {
      setBookingLoading(false);
    }
  };

  if (loading) {
    return <div style={{ textAlign: 'center', padding: '100px' }}>Loading lawyer profile details...</div>;
  }

  return (
    <div style={{ maxWidth: '1200px', margin: '30px auto', display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '30px' }}>
      
      {/* Profile & Reviews details */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
        
        {/* Profile Card */}
        <div className="glass-panel" style={{ padding: '40px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div>
              <span className="badge badge-success">{lawyer.categoryName}</span>
              <h1 style={{ fontSize: '2.5rem', marginTop: '8px' }}>{lawyer.name}</h1>
              <strong style={{ color: 'var(--accent-secondary)', fontSize: '1.1rem' }}>{lawyer.specialization}</strong>
            </div>
            
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--accent-amber)', fontSize: '1.2rem', fontWeight: 700 }}>
              <Star size={20} fill="var(--accent-amber)" />
              <span>{lawyer.averageRating ? lawyer.averageRating.toFixed(1) : '4.8'} / 5.0</span>
            </div>
          </div>

          <p style={{ color: 'var(--text-secondary)', fontSize: '1.05rem', lineHeight: 1.7 }}>
            {lawyer.bio}
          </p>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '20px', marginTop: '16px', borderTop: '1px solid var(--border-glass)', paddingTop: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Award size={20} style={{ color: 'var(--accent-primary)' }} />
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Experience</div>
                <strong style={{ fontSize: '1rem' }}>{lawyer.experienceYears} Years</strong>
              </div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Clock size={20} style={{ color: 'var(--accent-primary)' }} />
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Consulting Rate</div>
                <strong style={{ fontSize: '1rem' }}>${lawyer.hourlyRate} / Hour</strong>
              </div>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Mail size={20} style={{ color: 'var(--accent-primary)' }} />
              <div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Email</div>
                <strong style={{ fontSize: '0.9rem', wordBreak: 'break-all' }}>{lawyer.email || 'lawyer@legalplatform.com'}</strong>
              </div>
            </div>
          </div>
        </div>

        {/* Reviews */}
        <div className="glass-panel" style={{ padding: '30px' }}>
          <h2 style={{ fontSize: '1.4rem', marginBottom: '24px' }}>Client Reviews</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {reviews.length > 0 ? (
              reviews.map((rev) => (
                <div key={rev.id} style={{ paddingBottom: '20px', borderBottom: '1px solid var(--border-glass)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <strong style={{ color: 'var(--text-primary)' }}>{rev.clientName}</strong>
                    <div style={{ display: 'flex', gap: '2px', color: 'var(--accent-amber)' }}>
                      {[...Array(5)].map((_, i) => (
                        <Star key={i} size={14} fill={i < rev.rating ? "var(--accent-amber)" : "transparent"} />
                      ))}
                    </div>
                  </div>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>{rev.comment}</p>
                </div>
              ))
            ) : (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>No reviews recorded yet for this lawyer.</p>
            )}
          </div>
        </div>

      </div>

      {/* Booking Slot Selection Card */}
      <div className="glass-panel" style={{ padding: '30px', height: 'fit-content' }}>
        <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.2rem', marginBottom: '24px' }}>
          <Calendar size={20} className="text-gradient" /> Book Consultation
        </h3>

        {successMsg && <div className="badge-success" style={{ padding: '12px', borderRadius: '8px', marginBottom: '20px', width: '100%', fontSize: '0.85rem' }}>{successMsg}</div>}
        {errorMsg && <div className="badge-danger" style={{ padding: '12px', borderRadius: '8px', marginBottom: '20px', width: '100%', fontSize: '0.85rem' }}>{errorMsg}</div>}

        <form onSubmit={handleBook} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Select Date</label>
            <input 
              type="date" 
              required
              className="input-field"
              min={new Date().toISOString().split('T')[0]}
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
            />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Select Availability Slot</label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {availabilities.length > 0 ? (
                availabilities.map((slot) => (
                  <button
                    key={slot.id}
                    type="button"
                    style={{
                      padding: '12px',
                      borderRadius: '8px',
                      background: selectedSlot?.id === slot.id ? 'rgba(99,102,241,0.2)' : 'rgba(255,255,255,0.02)',
                      border: selectedSlot?.id === slot.id ? '1px solid var(--accent-primary)' : '1px solid var(--border-glass)',
                      color: 'var(--text-primary)',
                      cursor: 'pointer',
                      textAlign: 'left',
                      fontSize: '0.85rem',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      transition: 'var(--transition-smooth)'
                    }}
                    onClick={() => setSelectedSlot(slot)}
                  >
                    <span>{slot.dayOfWeek}: {slot.startTime.substring(0, 5)} - {slot.endTime.substring(0, 5)}</span>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Slot #{slot.id}</span>
                  </button>
                ))
              ) : (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>No active slots configured by lawyer.</p>
              )}
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Notes for Consultation</label>
            <textarea
              className="input-field"
              style={{ minHeight: '80px', resize: 'vertical' }}
              placeholder="Provide a brief explanation of your issue or case context..."
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />
          </div>

          <button type="submit" className="btn-primary" disabled={bookingLoading} style={{ width: '100%', justifyContent: 'center' }}>
            {bookingLoading ? 'Requesting Appointment...' : `Book Slot ($${lawyer.hourlyRate})`}
          </button>
        </form>
      </div>

    </div>
  );
};

export default LawyerDetail;
