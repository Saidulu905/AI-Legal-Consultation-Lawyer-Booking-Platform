import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { MessageSquare, ShieldAlert, Users, Scale, FileText, Sparkles, CheckCircle2 } from 'lucide-react';
import client from '../services/client';

const Home = () => {
  const [categories, setCategories] = useState([]);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await client.get('/api/auth/categories'); // Public categories list
        setCategories(response.data);
      } catch (error) {
        console.error("Failed to load categories:", error);
      }
    };
    fetchCategories();
  }, []);

  return (
    <div style={{ maxWidth: '1200px', margin: '40px auto', display: 'flex', flexDirection: 'column', gap: '80px' }}>
      
      {/* Hero Section */}
      <section className="glass-panel" style={{
        padding: '80px 40px',
        textAlign: 'center',
        background: 'radial-gradient(circle at top right, rgba(99, 102, 241, 0.15), transparent 70%), var(--bg-glass)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: '24px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '6px 16px', borderRadius: '999px', background: 'rgba(99, 102, 241, 0.1)', border: '1px solid rgba(99, 102, 241, 0.2)' }}>
          <Sparkles size={16} className="text-gradient" />
          <span style={{ fontSize: '0.85rem', fontWeight: 600, letterSpacing: '0.05em', textTransform: 'uppercase', color: 'var(--text-primary)' }}>LexiFlow 2.0 AI Legal Assistant</span>
        </div>
        
        <h1 style={{ fontSize: '3.5rem', lineHeight: 1.1, maxWidth: '800px', fontWeight: 800 }}>
          Navigate Complex Legal Issues with <span className="text-gradient">AI Precision</span>
        </h1>
        
        <p style={{ color: 'var(--text-secondary)', fontSize: '1.2rem', maxWidth: '600px', fontWeight: 400 }}>
          Get instant, secure legal issue classification, actionable checklists, document summaries, and direct lawyer bookings on a unified glassmorphic platform.
        </p>

        <div style={{ display: 'flex', gap: '16px', marginTop: '16px' }}>
          <Link to="/ai-chat" className="btn-primary" style={{ textDecoration: 'none', padding: '14px 32px', fontSize: '1.05rem' }}>
            <MessageSquare size={20} /> Ask AI Assistant
          </Link>
          <Link to="/lawyers" className="btn-secondary" style={{ textDecoration: 'none', padding: '14px 32px', fontSize: '1.05rem' }}>
            <Users size={20} /> Browse Lawyers
          </Link>
        </div>
      </section>

      {/* Feature Cards Grid */}
      <section style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
        <div style={{ textAlign: 'center' }}>
          <h2 style={{ fontSize: '2.2rem', fontWeight: 700 }}>Intelligent Platform Features</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>Designed for modern corporate and personal legal navigation</p>
        </div>

        <div className="grid-container">
          <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ padding: '12px', width: 'fit-content', borderRadius: '12px', background: 'rgba(99, 102, 241, 0.15)', color: 'var(--accent-primary)' }}>
              <Scale size={28} />
            </div>
            <h3 style={{ fontSize: '1.3rem' }}>AI Consultation & Advice</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
              Describe your legal issue to our AI Assistant. Receive instant category classifications, explained jargon terms, and customized pre-consultation steps.
            </p>
          </div>

          <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ padding: '12px', width: 'fit-content', borderRadius: '12px', background: 'rgba(139, 92, 246, 0.15)', color: 'var(--accent-secondary)' }}>
              <Users size={28} />
            </div>
            <h3 style={{ fontSize: '1.3rem' }}>Integrated Booking System</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
              Explore certified lawyer profiles, ratings, hourly rates, and verified experience. Select available dates and secure real-time booking slots instantly.
            </p>
          </div>

          <div className="glass-card" style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ padding: '12px', width: 'fit-content', borderRadius: '12px', background: 'rgba(16, 185, 129, 0.15)', color: 'var(--accent-emerald)' }}>
              <FileText size={28} />
            </div>
            <h3 style={{ fontSize: '1.3rem' }}>Document Summarizer</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
              Upload contracts, leases, or NDAs to receive comprehensive summaries listing parties, key obligations, critical deadlines, and potential red flags.
            </p>
          </div>
        </div>
      </section>

      {/* Categories List */}
      <section style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
        <div>
          <h2 style={{ fontSize: '2rem', fontWeight: 700 }}>Legal Specializations</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>Get help across multiple legal domains from specialized professionals</p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px' }}>
          {categories.length > 0 ? (
            categories.map((cat) => (
              <div key={cat.id} className="glass-panel glass-panel-hover" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <strong style={{ color: 'var(--text-primary)', fontSize: '1.15rem' }}>{cat.name}</strong>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>{cat.description}</p>
              </div>
            ))
          ) : (
            // Fallbacks if backend is not started yet
            ["Labor Law", "Criminal Law", "Family Law", "Corporate Law", "Intellectual Property"].map((name, i) => (
              <div key={i} className="glass-panel glass-panel-hover" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <strong style={{ color: 'var(--text-primary)', fontSize: '1.15rem' }}>{name}</strong>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>Expert legal advice and specialized representation.</p>
              </div>
            ))
          )}
        </div>
      </section>
      
    </div>
  );
};

export default Home;
