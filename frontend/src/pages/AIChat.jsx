import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import client from '../services/client';
import { MessageSquare, Sparkles, ClipboardList, BookOpen, UserCheck, Send, History, CheckSquare, Square } from 'lucide-react';

const AIChat = () => {
  const [query, setQuery] = useState('');
  const [history, setHistory] = useState([]);
  const [currentResponse, setCurrentResponse] = useState(null);
  const [loading, setLoading] = useState(false);
  const [checkedItems, setCheckedItems] = useState({});
  const navigate = useNavigate();

  useEffect(() => {
    fetchHistory();
  }, []);

  const fetchHistory = async () => {
    try {
      const response = await client.get('/api/ai/history');
      setHistory(response.data);
    } catch (error) {
      console.error("Failed to load chat history:", error);
    }
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setCurrentResponse(null);
    setCheckedItems({});
    try {
      const response = await client.post('/api/ai/chat', { message: query });
      setCurrentResponse(response.data);
      fetchHistory();
      setQuery('');
    } catch (error) {
      console.error("AI chat error:", error);
    } finally {
      setLoading(false);
    }
  };

  const selectHistoryItem = (item) => {
    // Reconstruct AIChatResponse structure from historical item
    setCurrentResponse({
      responseText: item.response,
      category: 'Consultation History',
      checklists: [
        "Consult your recommended lawyer matching the category.",
        "Gather all related files and correspondence.",
        "Submit a summary of details to the lawyer before your slot."
      ],
      requiredDocuments: [
        "ID Verification Proof",
        "Correspondence timeline logs"
      ],
      explainedTerms: [
        "Legal Counsel: Professional advice given by a licensed attorney."
      ],
      recommendedLawyers: []
    });
  };

  const toggleChecklist = (index) => {
    setCheckedItems(prev => ({
      ...prev,
      [index]: !prev[index]
    }));
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '30px auto', display: 'grid', gridTemplateColumns: '300px 1fr', gap: '30px', minHeight: '75vh' }}>
      
      {/* Sidebar for History */}
      <div className="glass-panel" style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.15rem' }}>
          <History size={18} className="text-gradient" /> Consultation History
        </h3>
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', overflowY: 'auto', maxHeight: '60vh' }}>
          {history.length > 0 ? (
            history.map((item) => (
              <div 
                key={item.id} 
                onClick={() => selectHistoryItem(item)}
                style={{
                  padding: '12px',
                  borderRadius: '8px',
                  background: 'rgba(255,255,255,0.02)',
                  border: '1px solid var(--border-glass)',
                  cursor: 'pointer',
                  fontSize: '0.85rem',
                  transition: 'var(--transition-smooth)'
                }}
                className="nav-link"
              >
                <div style={{ fontWeight: 600, color: 'var(--text-primary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                  {item.query}
                </div>
                <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '4px' }}>
                  {new Date(item.createdAt).toLocaleDateString()}
                </div>
              </div>
            ))
          ) : (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', textAlign: 'center', marginTop: '20px' }}>No past consultations found.</p>
          )}
        </div>
      </div>

      {/* Main Consultation Window */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
        
        {/* Chat prompt form */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
            <Sparkles className="text-gradient" size={20} />
            <h2 style={{ fontSize: '1.4rem' }}>Ask LexiFlow Legal Assistant</h2>
          </div>
          
          <form onSubmit={handleSend} style={{ display: 'flex', gap: '12px' }}>
            <input
              type="text"
              required
              className="input-field"
              placeholder="e.g. My employer has not paid my salary for two months..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              disabled={loading}
            />
            <button type="submit" className="btn-primary" disabled={loading} style={{ padding: '12px 24px' }}>
              <Send size={18} /> {loading ? 'Analyzing...' : 'Analyze'}
            </button>
          </form>
        </div>

        {/* AI Analysis Result */}
        {currentResponse && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            
            {/* Direct advice */}
            <div className="glass-card" style={{ borderLeft: '4px solid var(--accent-primary)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                <span className="badge badge-success">{currentResponse.category}</span>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>AI-Generated Consultation Report</span>
              </div>
              <p style={{ color: 'var(--text-primary)', fontSize: '1.05rem', whiteSpace: 'pre-line' }}>{currentResponse.responseText}</p>
            </div>

            {/* Checklists & Jargon */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
              
              {/* Action Checklists */}
              <div className="glass-panel" style={{ padding: '24px' }}>
                <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.1rem', marginBottom: '16px' }}>
                  <ClipboardList size={18} style={{ color: 'var(--accent-secondary)' }} /> Action Checklist
                </h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {currentResponse.checklists?.map((item, idx) => (
                    <div 
                      key={idx} 
                      onClick={() => toggleChecklist(idx)}
                      style={{ 
                        display: 'flex', 
                        alignItems: 'flex-start', 
                        gap: '10px', 
                        cursor: 'pointer',
                        color: checkedItems[idx] ? 'var(--text-muted)' : 'var(--text-primary)',
                        textDecoration: checkedItems[idx] ? 'line-through' : 'none',
                        transition: 'var(--transition-smooth)'
                      }}
                    >
                      {checkedItems[idx] ? (
                        <CheckSquare size={18} style={{ color: 'var(--accent-emerald)', marginTop: '2px', flexShrink: 0 }} />
                      ) : (
                        <Square size={18} style={{ color: 'var(--text-secondary)', marginTop: '2px', flexShrink: 0 }} />
                      )}
                      <span style={{ fontSize: '0.9rem' }}>{item}</span>
                    </div>
                  ))}
                </div>
              </div>

              {/* Explained Jargon Terms */}
              <div className="glass-panel" style={{ padding: '24px' }}>
                <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.1rem', marginBottom: '16px' }}>
                  <BookOpen size={18} style={{ color: 'var(--accent-emerald)' }} /> Explained Legal Jargon
                </h3>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {currentResponse.explainedTerms?.map((term, idx) => {
                    const [title, desc] = term.split(':');
                    return (
                      <div key={idx} style={{ fontSize: '0.9rem' }}>
                        <strong style={{ color: 'var(--text-primary)' }}>{title}</strong>
                        <p style={{ color: 'var(--text-secondary)', marginTop: '4px', fontSize: '0.85rem' }}>{desc || 'Term details and implications.'}</p>
                      </div>
                    );
                  })}
                </div>
              </div>

            </div>

            {/* Document checklist */}
            <div className="glass-panel" style={{ padding: '24px' }}>
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.1rem', marginBottom: '16px' }}>
                <ClipboardList size={18} style={{ color: 'var(--accent-amber)' }} /> Required Documents to Gather
              </h3>
              <ul style={{ paddingLeft: '20px', color: 'var(--text-secondary)', fontSize: '0.9rem', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {currentResponse.requiredDocuments?.map((doc, idx) => (
                  <li key={idx}>{doc}</li>
                ))}
              </ul>
            </div>

            {/* Recommended Lawyers */}
            {currentResponse.recommendedLawyers && currentResponse.recommendedLawyers.length > 0 && (
              <div className="glass-panel" style={{ padding: '24px' }}>
                <h3 style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.1rem', marginBottom: '16px' }}>
                  <UserCheck size={18} style={{ color: 'var(--accent-primary)' }} /> Matching Local Lawyers
                </h3>
                
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {currentResponse.recommendedLawyers.map((lawyer) => (
                    <div 
                      key={lawyer.id} 
                      style={{ 
                        display: 'flex', 
                        justifyContent: 'space-between', 
                        alignItems: 'center',
                        padding: '16px', 
                        borderRadius: '12px', 
                        background: 'rgba(255,255,255,0.01)',
                        border: '1px solid var(--border-glass)'
                      }}
                    >
                      <div>
                        <strong style={{ fontSize: '1.05rem', color: 'var(--text-primary)' }}>Sarah Jenkins</strong>
                        <div style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginTop: '2px' }}>{lawyer.specialization}</div>
                        <div style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '4px' }}>Rate: ${lawyer.hourlyRate}/hr | Exp: {lawyer.experienceYears} Years</div>
                      </div>
                      <button onClick={() => navigate(`/lawyers/${lawyer.id}`)} className="btn-primary" style={{ padding: '8px 16px', fontSize: '0.85rem' }}>
                        View Slots & Book
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

          </div>
        )}

        {/* Loading Spinner */}
        {loading && (
          <div className="glass-panel" style={{ padding: '60px', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '16px' }}>
            <div style={{
              width: '40px',
              height: '40px',
              border: '4px solid rgba(99, 102, 241, 0.2)',
              borderTopColor: 'var(--accent-primary)',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite'
            }} />
            <span style={{ color: 'var(--text-secondary)', fontWeight: 500 }}>LexiFlow is analyzing your inquiry...</span>
            <style>{`
              @keyframes spin {
                to { transform: rotate(360deg); }
              }
            `}</style>
          </div>
        )}

      </div>

    </div>
  );
};

export default AIChat;
